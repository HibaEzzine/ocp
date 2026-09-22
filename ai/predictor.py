import hashlib
import json
import math
import os
from datetime import date
from pathlib import Path
import numpy as np
from typing import List, Dict, Any, Optional, Tuple
from sklearn.ensemble import RandomForestRegressor
from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score
import joblib
import pandas as pd
from pdr_schemas import (
    ConsommationPoint,
    ArticleData,
    PredictionResult,
    RiskLevel,
    FeaturesDto,
    ModelEvaluationDto
)

# ==============================================================================
# CONFIGURATION CENTRALISÉE DES PONDÉRATIONS DU SCORE DE RISQUE (0 - 100)
# ==============================================================================
# Ces poids définissent l'importance relative de chaque composante du risque :
# 1. Proximité du seuil OCP (200% de la quantité installée) : 40%
# 2. Pente et orientation de la tendance récente : 25%
# 3. Évolution par rapport à la moyenne historique : 20%
# 4. Volatilité / dispersion des consommations : 15%
# ==============================================================================
WEIGHT_PROXIMITE_SEUIL = 0.40
WEIGHT_TENDANCE = 0.25
WEIGHT_EVOLUTION_MOYENNE = 0.20
WEIGHT_VOLATILITE = 0.15

MODEL_CACHE = {
    "model": None,
    "evaluation": None,
    "dataset_hash": None,
    "trained_at": None,
    "last_status": "UNTRAINED",
}

MODEL_DIR = Path(os.getenv("MODEL_DIR", Path(__file__).resolve().parent / "model"))
MODEL_FILE = MODEL_DIR / "random_forest.joblib"
METADATA_FILE = MODEL_DIR / "metadata.json"
DATASET_FILE = Path(os.getenv(
    "TRAINING_DATASET_FILE",
    Path(__file__).resolve().parent / "dataset" / "consommation_historique.xlsx",
))

def _normalize_column(value: object) -> str:
    text = str(value).strip().lower()
    return "".join(
        character for character in __import__("unicodedata").normalize("NFD", text)
        if __import__("unicodedata").category(character) != "Mn"
    ).replace("_", " ").replace(".", " ")

def load_manual_training_dataset() -> Tuple[List[ArticleData], Dict[str, Any]]:
    """Charge exclusivement le fichier Excel manuel utilisé pour l'entraînement."""
    if not DATASET_FILE.exists():
        raise FileNotFoundError(
            f"Dataset d'entraînement introuvable : {DATASET_FILE}. "
            "Ajoutez ai/dataset/consommation_historique.xlsx."
        )

    frame = pd.read_excel(DATASET_FILE)
    if frame.empty:
        raise ValueError("Le dataset d'entraînement est vide.")

    columns = {_normalize_column(column): column for column in frame.columns}

    def find_column(*names: str) -> Optional[str]:
        for name in names:
            normalized = _normalize_column(name)
            if normalized in columns:
                return columns[normalized]
        for normalized, original in columns.items():
            if any(_normalize_column(name) in normalized for name in names):
                return original
        return None

    code_column = find_column("code sap", "code article sap", "sap", "code article")
    quantity_column = find_column("q consommee", "quantite consommee", "consommation", "quantite")
    date_column = find_column("date", "date consommation")
    if not code_column or not quantity_column or not date_column:
        raise ValueError("Colonnes obligatoires absentes : CODE SAP, Q CONSOMMEE et DATE.")

    description_column = find_column("description", "designation")
    installed_column = find_column("q installee", "quantite installee")
    cleaned = pd.DataFrame({
        "code": frame[code_column].astype("string").str.strip(),
        "quantity": pd.to_numeric(frame[quantity_column].astype("string").str.replace(",", "."), errors="coerce"),
        "date": pd.to_datetime(frame[date_column], errors="coerce", dayfirst=True),
    })
    if description_column:
        cleaned["description"] = frame[description_column].astype("string").fillna("").str.strip()
    else:
        cleaned["description"] = ""
    if installed_column:
        cleaned["installed"] = pd.to_numeric(frame[installed_column], errors="coerce").fillna(0.0)
    else:
        cleaned["installed"] = 0.0

    cleaned = cleaned.dropna(subset=["code", "quantity", "date"])
    cleaned = cleaned[cleaned["code"] != ""]
    cleaned["quantity"] = cleaned["quantity"].abs()
    if cleaned.empty:
        raise ValueError("Aucune ligne exploitable dans le dataset d'entraînement.")

    articles: List[ArticleData] = []
    for code, rows in cleaned.groupby("code"):
        history = [
            ConsommationPoint(date=value.date().isoformat(), quantite=float(quantity))
            for value, quantity in zip(rows["date"], rows["quantity"])
        ]
        articles.append(ArticleData(
            codeSAP=str(code),
            description=str(rows["description"].iloc[0]),
            quantiteInstallee=float(rows["installed"].max()),
            historique=history,
        ))

    periods = cleaned["date"].dt.to_period("M")
    return articles, {
        "articles": len(articles),
        "rows": len(cleaned),
        "periodStart": str(periods.min()),
        "periodEnd": str(periods.max()),
        "file": str(DATASET_FILE),
    }

def load_persisted_model() -> None:
    """Recharge le modèle et ses métriques après un redémarrage du service IA."""
    if MODEL_CACHE["model"] is not None or not MODEL_FILE.exists():
        return
    try:
        MODEL_CACHE["model"] = joblib.load(MODEL_FILE)
        metadata = json.loads(METADATA_FILE.read_text(encoding="utf-8")) if METADATA_FILE.exists() else {}
        MODEL_CACHE["dataset_hash"] = metadata.get("datasetHash")
        MODEL_CACHE["trained_at"] = metadata.get("trainedAt")
        evaluation = metadata.get("evaluation")
        MODEL_CACHE["evaluation"] = ModelEvaluationDto(**evaluation) if evaluation else None
        MODEL_CACHE["last_status"] = "READY"
    except Exception:
        MODEL_CACHE["model"] = None
        MODEL_CACHE["last_status"] = "UNTRAINED"

def save_persisted_model(model: RandomForestRegressor, evaluation: ModelEvaluationDto, dataset_hash: str) -> None:
    MODEL_DIR.mkdir(parents=True, exist_ok=True)
    trained_at = __import__('time').time() * 1000
    joblib.dump(model, MODEL_FILE)
    METADATA_FILE.write_text(json.dumps({
        "datasetHash": dataset_hash,
        "trainedAt": trained_at,
        "evaluation": evaluation.model_dump(),
    }, ensure_ascii=True, indent=2), encoding="utf-8")
    MODEL_CACHE["trained_at"] = trained_at

def update_model_metadata(dataset_info: Dict[str, Any]) -> None:
    metadata = json.loads(METADATA_FILE.read_text(encoding="utf-8")) if METADATA_FILE.exists() else {}
    metadata["dataset"] = dataset_info
    METADATA_FILE.parent.mkdir(parents=True, exist_ok=True)
    METADATA_FILE.write_text(json.dumps(metadata, ensure_ascii=True, indent=2), encoding="utf-8")

def clean_and_aggregate_history(points: List[ConsommationPoint]) -> Dict[str, float]:
    """
    Nettoie et agrège les consommations réelles par mois chronologique (format 'YYYY-MM').
    - Élimine les valeurs négatives ou nulles
    - Élimine les dates invalides
    - Somme les consommations multiples pour un même mois
    """
    monthly_map: Dict[str, float] = {}
    if not points:
        return monthly_map

    for pt in points:
        if pt.quantite is None or pt.quantite <= 0:
            continue
        if not pt.date:
            continue

        try:
            date_str = str(pt.date).strip()
            parts = date_str.split('-')
            if len(parts) >= 2:
                ym_key = f"{int(parts[0]):04d}-{int(parts[1]):02d}"
                monthly_map[ym_key] = monthly_map.get(ym_key, 0.0) + float(pt.quantite)
        except Exception:
            continue

    return monthly_map

def extract_features(values: List[float], q_inst: float) -> Tuple[FeaturesDto, np.ndarray]:
    """
    Construit le vecteur de caractéristiques (Features) pour une série de consommations.
    - lag_1 : consommation du dernier mois (M)
    - lag_2 : consommation de deux mois auparavant (M-1)
    - lag_3 : consommation de trois mois auparavant (M-2)
    - moyenne_3 : moyenne mobile des 3 derniers mois
    - moyenne_6 : moyenne mobile des 6 derniers mois (si dispo)
    - min_recent : consommation minimale récente (sur 3 à 6 mois)
    - max_recent : consommation maximale récente
    - ecart_type : volatilité des consommations
    - tendance : pente d'évolution ('FORTE_HAUSSE', 'HAUSSE', 'STABLE', 'BAISSE')
    - variation_vs_moyenne : écart en % entre dernier mois et moyenne historique
    - ratio_conso_installee : ratio entre dernier mois et q_inst
    """
    n = len(values)
    lag1 = values[-1] if n >= 1 else 0.0
    lag2 = values[-2] if n >= 2 else lag1
    lag3 = values[-3] if n >= 3 else lag2

    # Moyennes
    window_3 = values[-3:] if n >= 3 else values
    moyenne_3 = float(np.mean(window_3)) if window_3 else 0.0

    window_6 = values[-6:] if n >= 6 else values
    moyenne_6 = float(np.mean(window_6)) if window_6 else moyenne_3

    min_recent = float(np.min(window_6)) if window_6 else 0.0
    max_recent = float(np.max(window_6)) if window_6 else 0.0
    ecart_type = float(np.std(window_6)) if len(window_6) > 1 else 0.0

    # Calcul de la pente de tendance (Régression linéaire sur les indices)
    x = np.arange(len(window_6))
    if len(window_6) >= 2 and np.var(x) > 0:
        pente = float(np.polyfit(x, window_6, 1)[0])
    else:
        pente = 0.0

    if pente > 2.0:
        tendance_str = "FORTE_HAUSSE"
    elif pente > 0.3:
        tendance_str = "HAUSSE"
    elif pente < -0.3:
        tendance_str = "BAISSE"
    else:
        tendance_str = "STABLE"

    # Variation vs moyenne
    variation_pct = ((lag1 - moyenne_3) / moyenne_3 * 100.0) if moyenne_3 > 0 else 0.0
    ratio_conso_inst = (lag1 / q_inst) if q_inst > 0 else 0.0

    dto = FeaturesDto(
        lag1=round(lag1, 2),
        lag2=round(lag2, 2),
        lag3=round(lag3, 2),
        moyenne3=round(moyenne_3, 2),
        moyenne6=round(moyenne_6, 2),
        minRecent=round(min_recent, 2),
        maxRecent=round(max_recent, 2),
        ecartType=round(ecart_type, 2),
        tendance=tendance_str,
        variationVsMoyenne=round(variation_pct, 1),
        ratioConsoInstallee=round(ratio_conso_inst, 2)
    )

    # Vecteur numérique pour scikit-learn
    feature_vector = np.array([
        lag1, lag2, lag3,
        moyenne_3, moyenne_6,
        min_recent, max_recent,
        ecart_type, pente,
        variation_pct, q_inst, ratio_conso_inst
    ], dtype=float)

    return dto, feature_vector

def build_training_dataset(articles: List[ArticleData]) -> Tuple[np.ndarray, np.ndarray, List[float], List[float]]:
    """
    Construit un jeu d'entraînement supervisé à partir de toutes les séries temporelles disponibles.
    Pour chaque PDR avec au moins 4 mois d'historique, crée des fenêtres glissantes (X_t -> y_{t+1}).
    """
    X_list = []
    y_list = []
    baseline_preds = []
    actuals = []

    for art in articles:
        monthly = clean_and_aggregate_history(art.historique or [])
        sorted_m = sorted(monthly.keys())
        values = [monthly[m] for m in sorted_m]
        q_inst = float(art.quantiteInstallee) if art.quantiteInstallee is not None else 0.0

        if len(values) >= 4:
            # Fenêtres glissantes : pour t de 3 à len(values)-1
            for t in range(3, len(values)):
                sub_values = values[:t]
                target_next = values[t]

                _, f_vec = extract_features(sub_values, q_inst)
                X_list.append(f_vec)
                y_list.append(target_next)

                # Baseline : Moyenne mobile des 3 derniers mois
                base_pred = float(np.mean(sub_values[-3:]))
                baseline_preds.append(base_pred)
                actuals.append(target_next)

    if not X_list:
        return np.empty((0, 12)), np.empty((0,)), [], []

    return np.array(X_list), np.array(y_list), baseline_preds, actuals

def serialize_articles_for_hash(articles: List[ArticleData]) -> str:
    payload = []
    for art in articles:
        payload.append({
            "codeSAP": art.codeSAP,
            "quantiteInstallee": art.quantiteInstallee,
            "description": art.description,
            "historique": [
                {"date": p.date, "quantite": p.quantite, "numeroOT": p.numeroOT}
                for p in (art.historique or [])
            ],
        })
    return hashlib.sha256(json.dumps(payload, sort_keys=True, default=str).encode("utf-8")).hexdigest()


def get_model_status() -> Dict[str, Any]:
    load_persisted_model()
    return {
        "status": MODEL_CACHE["last_status"],
        "trained": MODEL_CACHE["model"] is not None,
        "trainedAt": MODEL_CACHE["trained_at"],
        "datasetHash": MODEL_CACHE["dataset_hash"],
        "samples": MODEL_CACHE["evaluation"].nbSamplesTrained if MODEL_CACHE["evaluation"] else 0,
    }


def train_global_random_forest(articles: List[ArticleData], force: bool = False) -> Tuple[Optional[RandomForestRegressor], Optional[ModelEvaluationDto]]:
    """
    Entraîne un Random Forest Regressor sur l'historique global des PDRs
    et calcule les métriques d'évaluation réelles (MAE, RMSE, R²).
    Le modèle est mis en cache pour éviter un retrain inutile à chaque requête.
    """
    if not articles:
        MODEL_CACHE["last_status"] = "UNTRAINED"
        MODEL_CACHE["model"] = None
        MODEL_CACHE["evaluation"] = None
        MODEL_CACHE["dataset_hash"] = None
        MODEL_CACHE["trained_at"] = None
        return None, None

    load_persisted_model()
    dataset_hash = serialize_articles_for_hash(articles)
    if not force and MODEL_CACHE["model"] is not None and MODEL_CACHE["dataset_hash"] == dataset_hash:
        MODEL_CACHE["last_status"] = "READY"
        return MODEL_CACHE["model"], MODEL_CACHE["evaluation"]

    X, y, baseline_preds, actuals = build_training_dataset(articles)
    if len(X) < 4:
        MODEL_CACHE["last_status"] = "INSUFFICIENT_DATA"
        MODEL_CACHE["model"] = None
        MODEL_CACHE["evaluation"] = None
        MODEL_CACHE["dataset_hash"] = dataset_hash
        MODEL_CACHE["trained_at"] = None
        return None, None

    rf = RandomForestRegressor(
        n_estimators=50,
        max_depth=5,
        min_samples_split=2,
        random_state=42
    )
    rf.fit(X, y)

    y_pred = rf.predict(X)
    mae_rf = float(mean_absolute_error(y, y_pred))
    rmse_rf = float(np.sqrt(mean_squared_error(y, y_pred)))
    r2_val = float(r2_score(y, y_pred)) if len(y) > 2 else 0.0

    mae_base = float(mean_absolute_error(actuals, baseline_preds)) if actuals else mae_rf
    rmse_base = float(np.sqrt(mean_squared_error(actuals, baseline_preds))) if actuals else rmse_rf

    amelioration = 0.0
    if mae_base > 0:
        amelioration = round(((mae_base - mae_rf) / mae_base) * 100.0, 1)

    eval_dto = ModelEvaluationDto(
        modele="Random Forest Regressor (50 arbres, max_depth=5)",
        mae=round(mae_rf, 2),
        rmse=round(rmse_rf, 2),
        r2=round(max(0.0, r2_val), 2),
        baselineMae=round(mae_base, 2),
        baselineRmse=round(rmse_base, 2),
        nbSamplesTrained=len(X),
        ameliorationPct=amelioration
    )

    MODEL_CACHE["model"] = rf
    MODEL_CACHE["evaluation"] = eval_dto
    MODEL_CACHE["dataset_hash"] = dataset_hash
    MODEL_CACHE["trained_at"] = __import__('time').time() * 1000
    MODEL_CACHE["last_status"] = "READY"
    save_persisted_model(rf, eval_dto, dataset_hash)
    return rf, eval_dto

def calculate_risk_score(prevision: float, seuil: float, features: FeaturesDto, q_inst: float) -> Tuple[int, RiskLevel]:
    """
    Calcule le Score de Risque (0 à 100) selon la formule centralisée multi-critères :
    Score = 0.40 * S_seuil + 0.25 * S_tendance + 0.20 * S_evolution + 0.15 * S_volatilite
    
    Classification :
    - 0  à 39 : FAIBLE
    - 40 à 69 : MOYEN
    - 70 à 84 : ELEVE
    - 85 à 100: CRITIQUE
    """
    if seuil <= 0:
        return 0, RiskLevel.NON_EVALUABLE

    # 1. Sous-score Seuil (0 à 100) : ratio prévision / seuil OCP
    ratio = prevision / seuil
    s_seuil = min(100.0, (ratio / 1.5) * 100.0)

    # 2. Sous-score Tendance (0 à 100)
    if features.tendance == "FORTE_HAUSSE":
        s_tendance = 95.0
    elif features.tendance == "HAUSSE":
        s_tendance = 70.0
    elif features.tendance == "STABLE":
        s_tendance = 30.0
    else: # BAISSE
        s_tendance = 10.0

    # 3. Sous-score Évolution Récente vs Moyenne (0 à 100)
    var_pct = features.variationVsMoyenne or 0.0
    if var_pct > 50:
        s_evol = 90.0
    elif var_pct > 20:
        s_evol = 65.0
    elif var_pct >= -10:
        s_evol = 35.0
    else:
        s_evol = 10.0

    # 4. Sous-score Volatilité (0 à 100)
    moy = features.moyenne3 or 1.0
    cv = (features.ecartType / moy) if moy > 0 and features.ecartType else 0.0
    s_volatilite = min(100.0, cv * 100.0)

    # Fusion pondérée
    raw_score = (
        WEIGHT_PROXIMITE_SEUIL * s_seuil +
        WEIGHT_TENDANCE * s_tendance +
        WEIGHT_EVOLUTION_MOYENNE * s_evol +
        WEIGHT_VOLATILITE * s_volatilite
    )

    final_score = int(round(max(0.0, min(100.0, raw_score))))

    # Classification
    if final_score >= 85:
        niveau = RiskLevel.CRITIQUE
    elif final_score >= 70:
        niveau = RiskLevel.ELEVE
    elif final_score >= 40:
        niveau = RiskLevel.MOYEN
    else:
        niveau = RiskLevel.FAIBLE

    return final_score, niveau

def generate_explanations(article: ArticleData, prevision: float, seuil: float, ratio: float, score: int, features: FeaturesDto) -> List[str]:
    """
    Génère des explications claires et compréhensibles en langage naturel pour l'utilisateur métier.
    """
    explications = []

    # Explication liée au seuil OCP
    if ratio >= 1.0:
        explications.append(f"Dépassement du seuil métier OCP de 200% prévu (Prévision: {prevision:.1f} vs Seuil: {seuil:.1f}).")
    elif ratio >= 0.7:
        explications.append(f"Prévision de consommation en zone d'alerte, proche du seuil OCP ({ratio*100:.0f}% du seuil).")
    else:
        explications.append("Prévision de consommation conforme et sous le seuil d'alerte OCP.")

    # Explication liée à la tendance
    if features.tendance == "FORTE_HAUSSE":
        explications.append("Forte accélération de la consommation observée sur les derniers mois.")
    elif features.tendance == "HAUSSE":
        explications.append("Tendance globale haussière sur la période récente.")
    elif features.tendance == "BAISSE":
        explications.append("Tendance à la baisse constatée récemment.")

    # Explication liée au dernier mois vs moyenne
    var = features.variationVsMoyenne or 0.0
    if var >= 30.0:
        explications.append(f"La consommation du dernier mois est supérieure de +{var:.0f}% à la moyenne récente.")
    elif var <= -30.0:
        explications.append(f"La consommation du dernier mois est inférieure de {abs(var):.0f}% à la moyenne récente.")

    # Explication liée à la volatilité
    if features.ecartType and features.moyenne3 and (features.ecartType / features.moyenne3) > 0.4:
        explications.append("Forte variabilité et dispersion des consommations enregistrées.")

    return explications

def predict_single_article(article: ArticleData, model: Optional[RandomForestRegressor] = None,
                           current_month: Optional[str] = None) -> PredictionResult:
    """
    Pipeline complet pour un article PDR donné :
    1. Nettoyage & Agrégation
    2. Vérification historique insuffisant (< 3 mois)
    3. Feature Engineering
    4. Prédiction Random Forest (ou formule de secours EMA pondérée)
    5. Calcul Seuil OCP & Ratio
    6. Calcul Score de Risque (0-100) & Classification (FAIBLE, MOYEN, ELEVE, CRITIQUE)
    7. Explicabilité métier
    """
    code_sap = article.codeSAP or ""
    description = article.description or ""
    q_inst = float(article.quantiteInstallee) if article.quantiteInstallee is not None else 0.0
    seuil = 2.0 * q_inst if q_inst > 0 else 0.0

    monthly_data = clean_and_aggregate_history(article.historique or [])
    sorted_months = sorted(monthly_data.keys())
    values = [monthly_data[m] for m in sorted_months]
    history_points = [{"mois": m, "quantite": round(monthly_data[m], 2)} for m in sorted_months]
    conso_actuelle = values[-1] if values else 0.0
    mois_actuel = current_month or (sorted_months[-1] if sorted_months else None)
    mois_prevu = None
    if mois_actuel:
        year, month = (int(part) for part in mois_actuel.split("-"))
        next_month = month + 1
        mois_prevu = f"{year + (1 if next_month == 13 else 0):04d}-{1 if next_month == 13 else next_month:02d}"

    # Cas 1 : Moins de 3 mois d'historique -> Statut explicite INSUFFISANT
    if len(values) < 3:
        return PredictionResult(
            codeSAP=code_sap,
            description=description,
            quantiteInstallee=q_inst,
            consommationActuelle=round(conso_actuelle, 2),
            consommationPredite=None,
            seuil=round(seuil, 2),
            ratio=0.0,
            scoreRisque=0,
            niveauRisque=RiskLevel.NON_EVALUABLE,
            alerte=False,
            statut="INSUFFISANT",
            message="Données historiques insuffisantes pour la prédiction Machine Learning (minimum 3 mois requis).",
            explications=["Historique insuffisant (< 3 mois de recul)", "Modèle Machine Learning non appliqué pour éviter une fausse prédiction"],
            features=None,
            historique=history_points,
            nbMoisHistorique=len(values),
            moisActuel=mois_actuel,
            moisPrevu=mois_prevu,
            modeleUtilise="AUCUN"
        )

    # Feature Engineering
    features_dto, f_vec = extract_features(values, q_inst)

    # Cas 2 : Quantité installée manquante ou nulle
    if q_inst <= 0:
        # Inférence RF si dispo, sinon fallback moyenne/tendance
        if model is not None:
            pred_val = float(model.predict(f_vec.reshape(1, -1))[0])
        else:
            pred_val = float(0.7 * (features_dto.lag1 + (features_dto.lag1 - features_dto.lag2)) + 0.3 * features_dto.moyenne3)
        pred_val = max(0.0, pred_val)

        return PredictionResult(
            codeSAP=code_sap,
            description=description,
            quantiteInstallee=0.0,
            consommationActuelle=round(conso_actuelle, 2),
            consommationPredite=round(pred_val, 2),
            seuil=0.0,
            ratio=0.0,
            scoreRisque=0,
            niveauRisque=RiskLevel.NON_EVALUABLE,
            alerte=False,
            statut="SANS_QUANTITE_INSTALLEE",
            message="Quantité installée inconnue ou nulle. Seuil d'anomalie 200% non calculable.",
            explications=["Quantité installée non renseignée dans le référentiel SAP", "Seuil OCP 200% et score de risque incalculables sans parc installé"],
            features=features_dto,
            historique=history_points,
            nbMoisHistorique=len(values),
            moisActuel=mois_actuel,
            moisPrevu=mois_prevu,
            modeleUtilise="RANDOM_FOREST_PYTHON" if model is not None else "STATISTIQUE_PYTHON"
        )

    # Cas 3 : Inférence Random Forest
    if model is not None:
        try:
            pred_val = float(model.predict(f_vec.reshape(1, -1))[0])
        except Exception:
            pred_val = float(0.6 * features_dto.lag1 + 0.4 * features_dto.moyenne3)
    else:
        # Tendance + Moyenne mobile
        delta = features_dto.lag1 - features_dto.lag2
        trend_est = features_dto.lag1 + 0.5 * delta
        pred_val = float(0.7 * trend_est + 0.3 * features_dto.moyenne3)

    pred_val = max(0.0, pred_val)
    ratio = (pred_val / seuil) if seuil > 0 else 0.0

    # Calcul du score de risque et du niveau
    score_risque, niveau = calculate_risk_score(pred_val, seuil, features_dto, q_inst)
    alerte = (niveau in [RiskLevel.ELEVE, RiskLevel.CRITIQUE]) or (ratio >= 1.0)

    # Message de synthèse
    if niveau == RiskLevel.CRITIQUE:
        message = "Risque critique de surconsommation majeure (dépassement sévère du seuil prévu)."
    elif niveau == RiskLevel.ELEVE:
        message = "Risque élevé de surconsommation (dépassement prévu du seuil 200%)."
    elif niveau == RiskLevel.MOYEN:
        message = "Consommation sous surveillance (hausse constatée proche du seuil)."
    else:
        message = "Consommation prévue normale et maîtrisée."

    explications = generate_explanations(article, pred_val, seuil, ratio, score_risque, features_dto)

    return PredictionResult(
        codeSAP=code_sap,
        description=description,
        quantiteInstallee=round(q_inst, 2),
        consommationActuelle=round(conso_actuelle, 2),
        consommationPredite=round(pred_val, 2),
        seuil=round(seuil, 2),
        ratio=round(ratio, 2),
        scoreRisque=score_risque,
        niveauRisque=niveau,
        alerte=alerte,
        statut="PREDICTION_DISPONIBLE",
        message=message,
        explications=explications,
        features=features_dto,
        historique=history_points,
        nbMoisHistorique=len(values),
        moisActuel=mois_actuel,
        moisPrevu=mois_prevu,
        modeleUtilise="RANDOM_FOREST_PYTHON" if model is not None else "STATISTIQUE_PYTHON"
    )

def predict_all_articles(articles: List[ArticleData]) -> Tuple[List[PredictionResult], Optional[ModelEvaluationDto]]:
    """
    Exécute le pipeline global :
    1. Entraînement Random Forest sur les séries disponibles
    2. Calcul des métriques d'évaluation (MAE / RMSE)
    3. Inférence prédictive pour chaque PDR
    """
    # 1. Entraînement global du modèle
    model, eval_dto = train_global_random_forest(articles)

    return predict_articles_with_trained_model(articles, model, eval_dto)

def predict_articles_with_trained_model(
    articles: List[ArticleData],
    model: Optional[RandomForestRegressor] = None,
    eval_dto: Optional[ModelEvaluationDto] = None,
    current_month: Optional[str] = None,
) -> Tuple[List[PredictionResult], Optional[ModelEvaluationDto]]:
    """Utilise uniquement le modèle déjà entraîné pour analyser les consommations."""
    if model is None:
        model = MODEL_CACHE["model"]
        eval_dto = MODEL_CACHE["evaluation"]
    if model is None:
        raise ValueError("Le modèle n'est pas entraîné. Appelez /train avant /predict.")

    results = []
    for art in articles:
        res = predict_single_article(art, model=model, current_month=current_month)
        results.append(res)

    return results, eval_dto


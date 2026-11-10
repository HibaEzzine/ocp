import numpy as np
from typing import List, Dict
from pdr_schemas import (
    AnomalyArticleInput,
    AnomalyResult,
    AnomalyLevel,
    ConsommationPoint
)

def clean_and_aggregate_monthly_consumption(points: List[ConsommationPoint]) -> Dict[str, float]:
    """
    Regroupe et somme les consommations réelles par mois chronologique (format 'YYYY-MM').
    Règles de gestion :
    - Consommations négatives -> valeur absolue (abs)
    - Somme des consommations pour le même mois
    - Ignorer les valeurs nulles ou manquantes
    - Ignorer les dates invalides
    """
    monthly: Dict[str, float] = {}
    if not points:
        return monthly

    for pt in points:
        qty = pt.quantite
        if qty is None:
            continue
        try:
            qty_val = abs(float(qty))
        except (ValueError, TypeError):
            continue

        if not pt.date:
            continue

        try:
            date_str = str(pt.date).strip()
            # Traiter les séparateurs courants : '-', '/', '.'
            if '-' in date_str:
                parts = date_str.split('-')
                if len(parts) >= 2 and len(parts[0]) == 4:
                    year = int(parts[0])
                    month = int(parts[1])
                elif len(parts) >= 3 and len(parts[2]) == 4:
                    year = int(parts[2])
                    month = int(parts[1])
                else:
                    continue
            elif '/' in date_str:
                parts = date_str.split('/')
                if len(parts) >= 3 and len(parts[2]) == 4:
                    year = int(parts[2])
                    month = int(parts[1])
                elif len(parts) >= 2 and len(parts[0]) == 4:
                    year = int(parts[0])
                    month = int(parts[1])
                else:
                    continue
            elif '.' in date_str:
                parts = date_str.split('.')
                if len(parts) >= 3 and len(parts[2]) == 4:
                    year = int(parts[2])
                    month = int(parts[1])
                elif len(parts) >= 2 and len(parts[0]) == 4:
                    year = int(parts[0])
                    month = int(parts[1])
                else:
                    continue
            else:
                from dateutil.parser import parse
                dt = parse(date_str, dayfirst=True)
                year, month = dt.year, dt.month

            if 1 <= month <= 12 and 1900 <= year <= 2100:
                ym_key = f"{year:04d}-{month:02d}"
                monthly[ym_key] = monthly.get(ym_key, 0.0) + qty_val
        except Exception:
            continue

    return monthly


def detect_anomaly_single_article(article: AnomalyArticleInput) -> AnomalyResult:
    """
    Détecte automatiquement les anomalies de consommation d'un article PDR
    selon la méthode statistique : Moyenne + écart-type.
    """
    code_sap = str(article.codeSAP or "").strip()
    description = str(article.description or "").strip()

    monthly_map = clean_and_aggregate_monthly_consumption(article.historique or [])
    sorted_months = sorted(monthly_map.keys())
    values = [monthly_map[m] for m in sorted_months]

    # Cas 4 : Historique insuffisant (< 3 mois de recul)
    if len(values) < 3:
        conso_recente = round(float(values[-1]), 2) if values else 0.0
        moyenne = round(float(np.mean(values)), 2) if values else 0.0
        std = round(float(np.std(values)), 2) if values else 0.0
        seuil = round(moyenne + 2.0 * std, 2)
        return AnomalyResult(
            codeSAP=code_sap,
            description=description,
            consommationRecente=conso_recente,
            moyenneHistorique=moyenne,
            ecartType=std,
            seuil=seuil,
            augmentationPourcentage=0.0,
            niveau=AnomalyLevel.NON_EVALUABLE,
            anomalie=False,
            justification="Historique insuffisant pour l'évaluation statistique (minimum 3 mois requis)."
        )

    # Analyse statistique sur l'historique (>= 3 mois)
    conso_recente = float(values[-1])
    moyenne = float(np.mean(values))
    std = float(np.std(values))
    seuil = moyenne + 2.0 * std
    seuil_critique = moyenne + 3.0 * std

    # Calcul de l'augmentation en pourcentage avec gestion sécurisée de moyenne == 0
    if moyenne > 0:
        augmentation_pct = ((conso_recente - moyenne) / moyenne) * 100.0
    else:
        augmentation_pct = 0.0

    # Classification selon les 3 niveaux
    if conso_recente > seuil_critique:
        niveau = AnomalyLevel.CRITIQUE
        anomalie = True
        justification = (
            f"La consommation récente ({conso_recente:.2f}) dépasse sévèrement le seuil critique "
            f"(moyenne + 3 × écart-type = {seuil_critique:.2f})."
        )
    elif conso_recente > seuil:
        niveau = AnomalyLevel.INHABITUELLE
        anomalie = True
        justification = (
            "La consommation récente dépasse le seuil statistique calculé à partir de l'historique."
        )
    else:
        niveau = AnomalyLevel.NORMAL
        anomalie = False
        justification = (
            f"La consommation récente ({conso_recente:.2f}) est conforme et inférieure ou égale au seuil statistique ({seuil:.2f})."
        )

    return AnomalyResult(
        codeSAP=code_sap,
        description=description,
        consommationRecente=round(conso_recente, 2),
        moyenneHistorique=round(moyenne, 2),
        ecartType=round(std, 2),
        seuil=round(seuil, 2),
        augmentationPourcentage=round(augmentation_pct, 2),
        niveau=niveau,
        anomalie=anomalie,
        justification=justification
    )


def detect_anomalies_for_articles(articles: List[AnomalyArticleInput]) -> List[AnomalyResult]:
    """
    Applique la détection d'anomalies statistiques sur une liste d'articles PDR.
    """
    results: List[AnomalyResult] = []
    for art in articles:
        results.append(detect_anomaly_single_article(art))
    return results

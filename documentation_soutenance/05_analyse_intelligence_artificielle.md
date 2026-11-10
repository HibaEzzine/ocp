# 05 — Analyse du module d'intelligence artificielle

## Ce qui est réellement implémenté

Le répertoire `ai/` ne contient pas un unique modèle qui fait tout. Il héberge :

1. **Prévision de consommation** par `RandomForestRegressor` scikit-learn : apprentissage supervisé sur fenêtres mensuelles glissantes, inférence M+1.
2. **Détection d'anomalies** par des seuils statistiques calculés sur l'historique : moyenne + 2 écarts-types, puis moyenne + 3 écarts-types.
3. **Score de risque** déterministe pondéré (seuil, tendance, variation, volatilité), rattaché à la réponse de prévision.

La détection n'est pas un Isolation Forest, un autoencodeur, un SVM ou une deuxième Random Forest. Aucun de ces algorithmes n'apparaît dans le pipeline d'anomalie.

## API FastAPI

Définie dans `ai/main.py` :

| Méthode/route | Rôle réel |
|---|---|
| `GET /health` | Réponse de santé du service. |
| `POST /train` | Lit `ai/dataset/consommation_historique.xlsx` (ou `TRAINING_DATASET_FILE`), construit les fenêtres et réentraîne la forêt. |
| `GET /status` | Statut du modèle chargé et nombre d'échantillons enregistré. |
| `GET /metrics` | Renvoie le statut du modèle, pas les métriques d'un nouveau jeu test. |
| `POST /predict` | Prédit à partir des historiques fournis dans le JSON; exige que le modèle soit entraîné/chargé au niveau du service. |
| `POST /detect-anomalies` | Calcule les anomalies statistiques pour chaque article reçu. |

Schémas de requête/réponse : `ai/pdr_schemas.py`. L'application FastAPI autorise actuellement CORS depuis `*`.

## Données d'entrée et prétraitement

### Entraînement manuel

`load_manual_training_dataset()` dans `ai/predictor.py` lit une feuille par défaut avec `pandas.read_excel`. Il cherche des colonnes correspondant à code SAP, quantité consommée et date; description et quantité installée sont optionnelles. Les noms sont normalisés (minuscules, accents et ponctuation). Les quantités sont converties en nombres, virgules remplacées par un point, dates analysées avec `dayfirst=True`; lignes invalides sont écartées; les quantités sont converties en valeur absolue; les mouvements sont regroupés par code SAP.

Dans le fichier fourni, les colonnes attendues sont en première feuille. La quantité installée n'y est pas présente : le chargeur utilise donc `0.0`. Le fait que le chargeur utilise `pandas.read_excel` signifie que `openpyxl`, déclaré dans `ai/requirements.txt`, est requis pour ce classeur XLSX.

### Historique reçu pour la prédiction

`clean_and_aggregate_history()` ignore les quantités absentes, négatives ou nulles, les dates invalides, puis somme par mois `YYYY-MM`. Pour les entrées issues du backend, `IaPredictionService.construirePayload()` convertit préalablement les quantités en valeur absolue.

Pour la détection d'anomalies, `clean_and_aggregate_monthly_consumption()` convertit chaque quantité reçue en valeur absolue, ignore les valeurs manquantes/dates invalides et somme par mois chronologique.

## Features de prévision

La fonction `extract_features()` fabrique un vecteur numérique de **12 variables** :

1. `lag1` : dernière consommation mensuelle observée.
2. `lag2` : consommation mensuelle précédente.
3. `lag3` : consommation trois mois avant la fin (réutilise la plus proche disponible si court).
4. `moyenne3` : moyenne des trois dernières valeurs disponibles.
5. `moyenne6` : moyenne des six dernières valeurs disponibles (ou tout l'historique si plus court).
6. `minRecent` : minimum de la fenêtre récente de six valeurs.
7. `maxRecent` : maximum de la même fenêtre.
8. `ecartType` : écart-type de la fenêtre récente.
9. Pente de régression linéaire sur les indices de cette fenêtre.
10. `variationVsMoyenne` : variation du dernier mois par rapport à la moyenne 3 mois, en pourcentage.
11. `quantiteInstallee`.
12. `ratioConsoInstallee` : dernière consommation / quantité installée, ou zéro si non disponible.

Les features explicatives incluent également une catégorie textuelle de tendance (FORTE_HAUSSE, HAUSSE, STABLE, BAISSE) dans le DTO; la forêt reçoit la pente numérique et non cette chaîne.

## Construction et entraînement

`build_training_dataset()` prend pour chaque article au moins quatre mois positifs distincts, puis crée une fenêtre pour chaque mois suivant les trois premiers : entrées avant le mois cible → consommation du mois suivant. La baseline est la moyenne des trois dernières consommations. Le modèle global est un `RandomForestRegressor` à 50 arbres, `max_depth=5`, `min_samples_split=2`, `random_state=42`. Il est sauvegardé dans `ai/model/random_forest.joblib` et ses informations dans `ai/model/metadata.json`; un cache mémoire évite de répéter l'entraînement sur le même hash.

`/train` vérifie dans `main.py` qu'au moins quatre fenêtres sont produites avant d'appeler l'entraînement forcé.

### Pas de vrai découpage train/test

Dans `train_global_random_forest()`, la forêt est ajustée sur `(X, y)`, puis les métriques sont calculées par `rf.predict(X)` sur **les mêmes lignes**. Aucun `train_test_split`, aucune séparation temporelle et aucune validation croisée ne sont présents. Les métriques mesurent donc l'ajustement sur les données d'entraînement et ne permettent pas de conclure à la qualité de généralisation en production.

## Prédiction et score

- La prévision porte sur une valeur mensuelle suivante (`moisPrevu`), calculée après `moisActuel`.
- Minimum trois mois disponibles pour évaluer un article; sinon `INSUFFISANT`, sans valeur prédite.
- Quand la quantité installée est absente/nulle, le service peut produire une consommation prédite, mais le seuil 200 % et le risque sont `NON_EVALUABLE`.
- Une prédiction disponible avec modèle utilise la Random Forest. Dans la fonction unitaire, certaines erreurs d'inférence peuvent utiliser une formule de secours à base de dernière valeur et moyenne; mais l'API de prédiction globale exige un modèle entraîné et le backend Java échoue si FastAPI n'est pas joignable.
- Le seuil métier utilisé par la prévision est `2 × quantité installée`.
- Le score 0–100 combine : proximité au seuil (40 %), tendance (25 %), variation vs moyenne récente (20 %), volatilité (15 %). Les tranches sont FAIBLE (0–39), MOYEN (40–69), ELEVE (70–84), CRITIQUE (85–100). Alerte si le niveau est ELEVE/CRITIQUE ou si le ratio prévision/seuil atteint 1.

Ce score de risque est une formule métier codée; ce n'est pas la probabilité calibrée de la Random Forest. Les valeurs de pondération ne sont pas présentées comme validées par des experts dans le dépôt.

## Détection d'anomalies

Pour un article, le dernier mois agrégé est comparé aux statistiques de tous les mois agrégés disponibles :

- moins de trois mois : `NON_EVALUABLE`;
- consommation récente strictement supérieure à moyenne + 3×écart-type : `CRITIQUE`;
- sinon supérieure à moyenne + 2×écart-type : `INHABITUELLE`;
- sinon `NORMAL`.

Le calcul utilise l'écart-type de population NumPy (`np.std` par défaut), avec règles particulières si moyenne nulle. Il donne une justification, seuil, moyenne, écart-type et augmentation en pourcentage. Il n'apprend pas un modèle et ne prédit pas un événement futur.

## Métriques présentes et interprétation prudente

Le fichier observé `ai/model/metadata.json` contient :

| Métrique annoncée | Valeur |
|---|---:|
| MAE | 0,29 |
| RMSE | 0,33 |
| R² | 0,91 |
| MAE baseline | 2,0 |
| RMSE baseline | 2,0 |
| `nbSamplesTrained` | 4 |
| Amélioration calculée | 85,5 % |

Ce sont les valeurs déclarées dans les métadonnées présentes, pas un test de performance indépendant. Le source calcule MAE/RMSE/R² sur les échantillons d'entraînement et borne artificiellement R² à zéro au minimum; l'« amélioration » compare à la baseline sur ces mêmes fenêtres. L'écart entre 4 échantillons metadata et environ 314 fenêtres estimées de la feuille historique actuelle est une incohérence qui peut indiquer des artefacts périmés ou un autre jeu de données. Le hash et l'horodatage ne prouvent pas la provenance du binaire sérialisé.

Les tests Python valident des cas fonctionnels (historique insuffisant, modèle, seuil, persistance, nettoyage), pas la qualité statistique hors échantillon.

## Limites à reconnaître

1. Pas de split temporel/test indépendant ni de comparaison documentée sur un jeu de validation.
2. Peu d'échantillons du modèle enregistré; possible divergence entre fichier de données et artefact.
3. La feature quantité installée vaut zéro à l'entraînement avec le classeur fourni, mais peut être positive à l'inférence.
4. Gestion des mois absents : l'agrégation n'ajoute pas explicitement les mois à consommation nulle. Ainsi, les retards mensuels entre mouvements sont assimilés à des positions consécutives.
5. Le choix de prendre la valeur absolue transforme les mouvements négatifs en consommations positives; ce choix doit correspondre à la sémantique métier du flux.
6. La détection utilise l'historique complet, y compris le mois récent qu'elle évalue, ce qui peut déplacer sa moyenne et son seuil.
7. Une saisonnalité, les arrêts planifiés, la criticité équipement, les délais de livraison, les fournisseurs, les prix et les OT futurs ne sont pas des features.
8. Le frontend ne donne pas de preuve de suivi du drift, d'approbation des alertes ou de recalibrage statistique.

## Sources principales

`ai/main.py`; `ai/pdr_schemas.py`; `ai/predictor.py`; `ai/anomaly_detector.py`; `ai/tests/test_predictor.py`; `ai/tests/test_anomaly_detector.py`; `ai/model/metadata.json`; `ai/dataset/consommation_historique.xlsx`; `backend/src/main/java/com/ocp/pdr/service/IaPredictionService.java`.

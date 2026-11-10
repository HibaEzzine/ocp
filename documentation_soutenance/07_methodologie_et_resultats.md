# 07 — Méthodologie, validation et résultats

## Méthodologie suivie pour présenter le projet

1. **Cadrer le besoin** : disponibilité des PDR, variabilité de consommation, processus d'import et d'aide à la décision.
2. **Examiner le dépôt** : différencier interface, API, persistance, moteur métier, microservice IA et données livrées.
3. **Suivre les flux réels** : routes UI → services frontend → contrôleurs → services Java → repositories/MySQL ou HTTP vers FastAPI.
4. **Inspecter les données** : lire les feuilles, en-têtes et dimensions des classeurs présents; ne pas assimiler des noms de fonctionnalités à des fichiers existants.
5. **Vérifier l'IA dans le code** : identifier modèles, features, nettoyage, entraînement, inférence, métriques, tests.
6. **Évaluer les preuves** : distinguer une fonctionnalité codée, un test unitaire, un artefact de modèle et une mesure validée sur données inconnues.

Pour une démarche de soutenance scientifique plus solide, les étapes de validation futures devraient inclure : définition du KPI avec le métier, déduplication et contrôle qualité SAP, séparation temporelle entraînement/validation/test, comparaison à une baseline naïve, suivi par classes de pièces et rapport d'erreurs avant mise en production.

## État des tests présents

### Python

`ai/tests/test_predictor.py` couvre notamment risque faible/élevé/moyen, historique insuffisant, quantité installée nulle, nettoyage, prédiction non négative, entraînement global, modèle entraîné, passage décembre-janvier, persistance et chargement du dataset.

`ai/tests/test_anomaly_detector.py` couvre consommation normale/inhabituelle/critique, historique insuffisant, moyenne nulle, agrégation mensuelle et valeurs négatives.

Ces tests confirment des cas programmés, pas la qualité réelle de prévision sur un jeu test.

### Java

Des tests sont présents dans `backend/src/test/java`, notamment pour le moteur d'approvisionnement, les règles, import Excel, service IA, anomalies, demandes d'achat et analyse. Des rapports préexistants se trouvent sous `backend/target/surefire-reports`; leur présence n'est pas une preuve d'exécution sur l'état courant du code et des données.

### Interface

Le dépôt définit `npm run build` (TypeScript puis Vite) et `npm run lint` (`oxlint`) dans `frontend/package.json`; aucun résultat de ces commandes n'est revendiqué ici.

## Résultats factuels observés dans les fichiers

### Données

- `ai/dataset/consommation_historique.xlsx`: deux feuilles, historiques datés et totaux annuels.
- Première feuille : 4 392 lignes avec code/quantité/date parseables, 1 834 codes distincts, 2 819 couples article-mois et dates observées de janvier 2025 à août 2026.
- Deuxième feuille : 12 438 lignes de données avec consommation annuelle 2021–2025, sans date de mouvement.
- `backend/temp-stock.xlsx`: modèle de quatre colonnes avec une ligne sans article exploitable.

### Modèle

`ai/model/metadata.json` déclare Random Forest Regressor (50 arbres, profondeur max 5), MAE 0,29, RMSE 0,33, R² 0,91, baseline MAE/RMSE 2,0, quatre échantillons et amélioration 85,5 %. Le fichier `random_forest.joblib` existe dans le dépôt courant.

Ces valeurs sont à rapporter en citant leur origine comme **métadonnées d'un artefact**, et jamais comme performance démontrée en production. Le code ajuste la Random Forest sur les fenêtres puis prédit ces mêmes fenêtres pour calculer MAE/RMSE/R². Il ne définit pas de test indépendant. En plus, le fichier courant suggère environ 314 fenêtres possibles après agrégation, alors que le metadata en indique 4 : provenance et synchronisation à clarifier.

## Limites de mesure et de démonstration

- Aucun résultat de déploiement, volume de requêtes, latence, utilisateur pilote ou KPI avant/après n'est dans le dépôt.
- Pas de split train/test temporel; pas de mesures d'erreur sur données non vues.
- Un classeur d'entraînement ne possède pas `Q INSTALLEE`, alors que la feature est employée par le modèle.
- Les résultats statistiques d'anomalie ne sont pas comparés à un étiquetage expert réel.
- Pas de mesures de taux de faux positifs/faux négatifs, rappel, précision ou matrice de confusion.
- Pas de comparaison documentée de scénarios de stock ou de coûts.
- La nature du fichier `random_forest.joblib` ne suffit pas à prouver qu'il est cohérent avec le code et les données de l'état local.
- L'import de toutes les feuilles n'a pas été exécuté dans un environnement MySQL complet; les diagnostics de correspondance sont des constats statiques.

## Formulations honnêtes pour la soutenance

À dire : « Le code implémente une prévision par Random Forest et une évaluation fonctionnelle par tests; les métriques présentes dans les métadonnées sont calculées sur les données d'entraînement, donc elles ne constituent pas une estimation fiable de la généralisation. La prochaine étape est un découpage temporel et une validation métier. »

À éviter : « Le modèle prédit avec 91 % de précision », « l'algorithme optimise automatiquement les achats », « les résultats ont réduit les ruptures » ou « l'anomalie a été validée par l'usine » : ces résultats ne sont pas établis par les fichiers.

## Sources principales

`ai/tests/`; `backend/src/test/java/`; `backend/target/surefire-reports/`; `frontend/package.json`; `ai/model/metadata.json`; `ai/predictor.py`; `ai/dataset/consommation_historique.xlsx`; `backend/temp-stock.xlsx`.

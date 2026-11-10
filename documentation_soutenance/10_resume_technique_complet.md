# 10 — Résumé technique complet et index des sources

## Résumé en une phrase

Application web React / Spring Boot / MySQL pour gérer articles, imports et demandes d'achat, avec règles déterministes de réapprovisionnement, un microservice FastAPI utilisant Random Forest pour la prévision mensuelle et des seuils statistiques pour signaler des consommations inhabituelles.

## Cartographie des modules et classes

### Backend Java

| Package / fichiers | Responsabilité |
|---|---|
| `backend/src/main/java/com/ocp/pdr/PdrApplication.java` | Point de démarrage Spring Boot. |
| `.../controller/AuthController.java` | Login, refresh token et santé auth. |
| `.../controller/ArticlePDRController.java` | CRUD, recherche et résumé articles. |
| `.../controller/DemandeAchatController.java` | CRUD DA/reportings, import/export et statistiques. |
| `.../controller/AnalyseController.java` | Routes analyse, résultats, anomalies, ML et résumé. |
| `.../controller/ImportExportController.java` | Import multi-fichiers, validation et exports Excel. |
| `.../controller/HistoriqueController.java` | Lecture des historiques DA. |
| `.../controller/UtilisateurController.java` | Opérations utilisateurs et profil. |
| `.../service/MoteurApprovisionnementService.java` | Orchestration de l'analyse globale et persistance. |
| `.../service/RegleMinMaxService.java` | Prédicat et quantité de la règle min/max. |
| `.../service/ReglePlanifieService.java` | Prédicat et quantité planifiée. |
| `.../service/RegleSurDemandeService.java` | Prédicat et quantité à la demande. |
| `.../service/AnalyseConsommationService.java` | Détection statistique métier persistée. |
| `.../service/IaPredictionService.java` | Construction de payload et appel FastAPI; fallback Java pour anomalies. |
| `.../service/ExcelImportService.java` | Validation, streaming et mapping des référentiels. |
| `.../service/ExcelExportService.java` | Génération d'exports Excel. |
| `.../service/DemandeAchatService.java` | Métier DA, import/export et historique. |
| `.../model/` | Entités JPA listées dans le document 04. |
| `.../repository/` | Accès MySQL via Spring Data JPA. |
| `.../security/` | JWT, filtre, chargement user et chaîne Spring Security. |
| `backend/src/main/resources/application.properties` | Configuration MySQL, taille upload, ports, service IA et Swagger. |
| `backend/pom.xml` | Dépendances et versions Java/Spring. |

Les chemins précédés de `.../` se complètent par `backend/src/main/java/com/ocp/pdr/`.

### Service Python

| Fichier | Responsabilité |
|---|---|
| `ai/main.py` | Endpoints FastAPI et conversion des erreurs HTTP. |
| `ai/pdr_schemas.py` | Contrats Pydantic, niveaux de risque/anomalie et DTO features/métriques. |
| `ai/predictor.py` | Lecture Excel, agrégation mensuelle, features, fenêtres, Random Forest, métriques, score et prédiction. |
| `ai/anomaly_detector.py` | Agrégation mensuelle et seuils moyenne + écart-type. |
| `ai/requirements.txt` | Dépendances Python déclarées. |
| `ai/tests/test_predictor.py` | Tests fonctionnels prévision et entraînement. |
| `ai/tests/test_anomaly_detector.py` | Tests fonctionnels des seuils statistiques. |
| `ai/dataset/consommation_historique.xlsx` | Jeu de données Excel utilisé par l'entraînement manuel. |
| `ai/model/random_forest.joblib` | Artefact de modèle sérialisé présent à l'analyse. |
| `ai/model/metadata.json` | Hash, date et métriques annoncées. |

### Frontend

| Fichier/dossier | Responsabilité |
|---|---|
| `frontend/src/main.tsx`, `App.tsx` | Initialisation React et racine d'application. |
| `frontend/src/routes/AppRoutes.tsx`, `paths.ts` | Routes applicatives et restrictions d'écran. |
| `frontend/src/context/AuthContext.tsx` | Session utilisateur/token. |
| `frontend/src/components/ProtectedRoute.tsx` | Contrôle d'accès d'interface. |
| `frontend/src/pages/dashboard/` | Tableau de bord. |
| `frontend/src/pages/reporting/` | Demandes d'achat/reportings. |
| `frontend/src/pages/approvisionnement/` | Résultats et lancement du moteur. |
| `frontend/src/pages/anomalies/` | Anomalies statistiques, prédictions et alertes de règles. |
| `frontend/src/pages/import/` | Téléversement Excel. |
| `frontend/src/pages/historique/` | Historique. |
| `frontend/src/pages/auth/` | Login et profil. |
| `frontend/src/services/` | Clients REST Axios et contrats TypeScript. |
| `frontend/package.json` | Dépendances et scripts build/lint/dev. |

## Contrats de l'IA

### Requête type de prévision

```json
{
  "articles": [{
    "codeSAP": "EXEMPLE",
    "description": "Pièce exemple",
    "quantiteInstallee": 10,
    "historique": [
      {"date": "2026-01-01", "quantite": 2},
      {"date": "2026-02-01", "quantite": 3},
      {"date": "2026-03-01", "quantite": 2}
    ]
  }],
  "currentMonth": "2026-03"
}
```

Exemple pédagogique, pas une ligne de production. `POST /predict` retourne la consommation actuelle/prédite, le seuil, ratio, score, niveau, explications, features et historique agrégé. Trois mois minimum sont requis pour une prévision.

### Requête type d'anomalie

Même objet article/historique envoyé à `POST /detect-anomalies`; réponse avec consommation récente, moyenne, écart-type, seuil, niveau, booléen anomalie et justification. La détection demande au moins trois mois distincts.

## Données analysées

- `ai/dataset/consommation_historique.xlsx`: Feuil1 mouvements de consommation avec code, quantité, OT et date; Feuil2 agrégats annuels 2021–2025 sans dates mensuelles.
- `backend/temp-stock.xlsx`: modèle stock incomplet.
- Absents : fichiers autonomes de stock hebdomadaire, backlog OT, BOM par secteur/site, besoins en cours.
- Le service d'entraînement lit l'Excel, alors que le backend transmet des historiques MySQL pour prédire. Ce sont deux chemins distincts.

## Résumé des statuts de livraison

| Fonction | Statut | Explication |
|---|---|---|
| Frontend de gestion et appels API | Réalisé / à valider en exécution | Les pages et appels réseau existent. |
| Gestion articles et DA | Réalisé dans le code | Contrôleurs, services, tables et écrans présents. |
| Imports Excel multi-données | Réalisé mais dépendant des formats | Streaming et mapping présents; toutes les structures ne sont pas livrées/testées. |
| Règles de réapprovisionnement | Réalisé | Trois prédicats ordonnés et proposition de quantité. |
| Optimisation mathématique des stocks | Non implémentée | Pas de fonction objectif/solveur/contraintes de coût. |
| Détection d'anomalies | Réalisée en méthode statistique | Moyenne + 2/3 écarts-types; repli Java. |
| Prévision Random Forest | Implémentée, preuve qualité partielle | Apprentissage/inférence présents, dataset et artifact incohérents, évaluation in-sample seulement. |
| Test indépendant du modèle | Non implémenté | Aucun split temporel/jeu holdout dans le code. |
| Émission automatique de DA/SAP | Non démontrée | Les résultats sont des suggestions persistées/exportables. |
| Autorisation par rôle sur API | À compléter/vérifier | Le frontend filtre les écrans; règles backend par rôle non repérées. |

## Incohérences majeures à présenter avec transparence

1. Les métadonnées du modèle déclarent quatre échantillons, alors que le fichier Excel courant laisse estimer bien davantage de fenêtres (environ 314).
2. Le chargement d'entraînement met la quantité installée à zéro faute de colonne dans le fichier courant.
3. Les métriques sont calculées sur les données d'entraînement, sans séparation de test.
4. Le frontend déclare quelques endpoints IA/anomalie absents du contrôleur ou non raccordés aux pages.
5. Le statut ML backend est une constante, et l'endpoint metrics backend déclenche des prédictions.
6. Le titre parle d'optimisation, mais les règles calculent une quantité simple basée sur seuil max et stock; coûts, délais et besoins nets ne sont pas intégrés.
7. L'interface suggère des fichiers PDR absents du dépôt; le gabarit stock fourni est vide.
8. Les rôles UI sont plus précis que le contrôle d'accès serveur démontré.
9. Le README et le `DataSeeder` ne présentent pas les mêmes identifiants/mots de passe de démonstration; il faut assainir et confirmer les accès de démo sans réutiliser ces valeurs en production.
10. Les répertoires contiennent des changements locaux non commités; l'état observé peut ne pas correspondre au commit de référence.

## Trame technique à retenir

**Entrées Excel et référentiels → tables JPA/MySQL → règles métier et appels IA → résultats REST → interface et exports → validation de la décision par l'utilisateur.**

À l'oral, distinguer trois mécanismes : (1) règles de stock, (2) modèle Random Forest de prévision, (3) détection statistique d'anomalies. Ne pas les présenter comme un unique « algorithme IA d'optimisation ».

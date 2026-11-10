# 01 — Présentation générale et diagnostic global

## Diagnostic global du dépôt

Le dépôt contient une application de gestion des pièces de rechange (PDR) organisée en trois parties : une interface React, une API Spring Boot et un microservice FastAPI. Le backend gère des articles, stocks, consommations, OT, demandes d'achat, imports Excel et résultats de règles d'approvisionnement. Le service Python met en œuvre une prévision mensuelle par Random Forest (si un modèle entraîné existe) et une détection statistique des consommations inhabituelles.

Le projet est **un outil d'aide à la décision**, pas un système qui émet automatiquement des commandes d'achat. Le moteur calcule des besoins indicatifs à partir des seuils, du stock et d'une condition de backlog. Le titre mentionne l'optimisation, mais aucun solveur d'optimisation mathématique, coût d'achat, délai fournisseur, contrainte budgétaire ou intégration SAP d'émission de DA n'a été identifié dans les sources examinées.

| Domaine | Diagnostic factuel |
|---|---|
| Interface web | Présente : dashboard, demandes/reporting, import, approvisionnement, anomalies/IA, historique, profil. Plusieurs pages appellent bien les API. |
| Backend REST | Présent : contrôleurs Spring MVC, services métier, repositories Spring Data JPA, validation et export Excel. |
| Base de données | MySQL par défaut, schéma géré par Hibernate avec `ddl-auto=update`; aucune migration versionnée de schéma visible dans le dépôt. |
| Approvisionnement | Trois règles déterministes MIN_MAX, PLANIFIE et SUR_DEMANDE réellement codées. |
| IA de prévision | RandomForestRegressor de scikit-learn, fenêtres mensuelles et features construites dans Python. L'entraînement est séparé du calcul de prédiction et utilise un fichier Excel local. |
| Anomalies | Seuils statistiques moyenne + 2 écarts-types et moyenne + 3 écarts-types. Ce n'est pas un modèle d'apprentissage automatique entraîné. |
| Données fournies | Un historique de consommation Excel avec deux feuilles; un fichier `temp-stock.xlsx` presque vide. Aucun fichier stock hebdomadaire complet, backlog OT, BOM Ammoniac/Soufre/Export ou besoins en cours autonome n'a été trouvé. |
| Validation | Des tests unitaires Java et Python existent. Cette analyse statique ne prouve pas l'exécution réussie de l'application en production. |
| Risques/écarts | Le modèle de risque annoncé dans les réponses et le fichier de métadonnées repose sur 4 échantillons, et les métriques calculées dans le code sont mesurées sur les mêmes données que l'entraînement. Le contrôle des rôles n'est établi que côté interface dans les sources consultées. |

### État local à la date de l'analyse

Le dépôt avait des modifications locales non validées dans des fichiers pertinents, notamment `ai/main.py`, `ai/pdr_schemas.py`, `ai/anomaly_detector.py`, `backend/.../AnalyseController.java`, `backend/.../IaPredictionService.java`, `frontend/.../AnomaliesPage.tsx` et `frontend/.../analyseService.ts`. Des fichiers de modèle et tests étaient également non suivis. Ils ont été considérés comme faisant partie de l'état observé et n'ont pas été modifiés. Les conclusions décrivent donc cet état local, pas nécessairement celui du dernier commit.

## Contexte industriel et besoin métier

Dans une activité industrielle, les équipements de production et de maintenance requièrent des pièces disponibles au bon moment. Une rupture peut retarder une intervention; un surstock mobilise du capital et de l'espace. Les équipes doivent rapprocher les stocks, les historiques de sorties, les ordres de travail (OT), les nomenclatures (BOM) et les règles de réapprovisionnement.

Le contexte OCP / JPH I/J est celui déclaré par le demandeur et suggéré par les noms et données du dépôt. Le code ne documente pas précisément les processus réels du service, les règles officielles SAP ni les indicateurs métier validés par les responsables. Ces éléments doivent être confirmés en soutenance avant de les présenter comme des règles organisationnelles.

## Problématique reformulée

Comment consolider les données disponibles sur les pièces de rechange, aider l'utilisateur à repérer les consommations qui s'écartent de l'historique, estimer la consommation du mois suivant et proposer des quantités de réapprovisionnement explicables, tout en gardant la décision finale à l'utilisateur métier?

## Objectifs

1. Centraliser les référentiels PDR et les informations de demandes d'achat.
2. Faciliter l'import/export des données Excel et leur consultation.
3. Appliquer des règles simples de réapprovisionnement MIN_MAX, PLANIFIE et SUR_DEMANDE.
4. Calculer des tendances et des prévisions mensuelles à partir d'historiques disponibles.
5. Signaler les consommations statistiques inhabituelles avec justification et niveau.
6. Tracer les opérations et présenter les résultats par interface web.
7. Préserver une validation humaine avant toute action d'achat.

## Périmètre réalisé, partiel et absent

### Réalisé dans le code

- CRUD des articles et demandes d'achat, authentification JWT, profils et rôles applicatifs.
- Import Excel de DA et import des référentiels PDR; exports de tableaux et résultats.
- Calcul de propositions de réapprovisionnement et stockage des résultats.
- Algorithme statistique d'anomalie et pipeline de prévision Random Forest Python.
- Interface utilisateur qui consomme les API backend.

### Partiel / à confirmer

- Le moteur Python est externe au backend et doit être démarré à part. La prévision échoue si ce service est indisponible; la détection d'anomalies, elle, dispose d'un repli Java.
- La qualité métier et la complétude des historiques réels importés ne sont pas prouvées. Le classeur de consommation présente seulement 4 392 lignes avec les trois champs code, quantité et date, malgré une dimension déclarée supérieure à un million de lignes.
- Le fichier de métadonnées du modèle annonce `nbSamplesTrained = 4`; ce chiffre ne constitue pas un résultat de test indépendant.
- Les fichiers Excel du dépôt ne permettent pas de démontrer l'intégration effective de tous les référentiels montrés par l'écran d'import.

### Non implémenté ou non démontré

- Optimisation quantitative globale de stock (EOQ, programmation linéaire, contraintes de service ou de coût).
- Envoi automatique d'une demande d'achat, connexion SAP/ERP, fournisseurs, budgets ou délais d'approvisionnement.
- Évaluation hors échantillon ou validation croisée, et métriques métier mesurées en exploitation.
- BOM dédiées Ammoniac/Soufre/Export, stock hebdomadaire, backlog OT et besoins en cours livrés sous forme de classeurs dans ce dépôt.

## Architecture de présentation conseillée

Présenter le projet comme un prototype applicatif d'aide à la décision : ingestion de données Excel → stockage MySQL → règles métier backend et calcul IA Python → API REST → interface web → validation humaine. Dire explicitement que la détection est statistique, que la Random Forest est la prévision apprise et que la quantité d'achat proposée découle de règles, pas d'un optimiseur.

## Sources principales

- `README.md`
- `backend/pom.xml`
- `backend/src/main/resources/application.properties`
- `ai/main.py`, `ai/predictor.py`, `ai/anomaly_detector.py`, `ai/pdr_schemas.py`
- `frontend/src/routes/AppRoutes.tsx`
- `backend/src/main/java/com/ocp/pdr/service/MoteurApprovisionnementService.java`
- `ai/dataset/consommation_historique.xlsx`
- `backend/temp-stock.xlsx`

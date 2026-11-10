# 02 — Architecture technique

## Vue logique

```text
Navigateur
  React 19 + TypeScript + Vite
  Axios, React Router, Recharts
       │ HTTP / JSON, Bearer JWT
       ▼
Backend Spring Boot 3.4.2 (Java 21), port 8080
  Contrôleurs REST → Services → Repositories Spring Data JPA
       │                                      │
       │ HTTP JSON vers AI_SERVICE_URL        ▼
       ▼                                MySQL pdr_db
Microservice FastAPI, port 8000          Entités JPA
  /train /predict /detect-anomalies
  scikit-learn, pandas, NumPy
  Excel local + modèle joblib
```

Cette architecture est constatée dans les fichiers de configuration et les appels réseau. Elle n'établit pas à elle seule une disponibilité, une capacité ou un déploiement industriel. Le README décrit un démarrage local, sans Docker.

## Technologies et rôle des modules

| Couche | Technologies présentes | Rôle |
|---|---|---|
| Frontend | React, TypeScript, Vite, Axios, React Router, Recharts, Lucide | Écrans, formulaires, navigation, graphiques et appels REST. |
| Backend | Java 21, Spring Boot 3.4.2, Spring Web, Spring Security, Spring Data JPA, Validation, Actuator | API, règles métier, accès SQL, sécurité et orchestration. |
| Base | MySQL Connector/J | Persistance relationnelle. H2 est déclaré pour les tests. |
| Excel Java | Apache POI 5.2.3 | Lecture/validation et génération de fichiers Excel. |
| Authentification | JJWT 0.12.6, BCrypt | JWT sans session côté serveur, hachage du mot de passe. |
| Documentation API | springdoc OpenAPI 2.8.5 | Swagger UI et document OpenAPI. |
| Microservice IA | FastAPI, Pydantic, pandas, NumPy, scikit-learn, joblib, openpyxl | API Python pour l'entraînement/prévision et détection statistique. |

Références : `frontend/package.json`, `backend/pom.xml`, `ai/requirements.txt`, `README.md`.

## Flux essentiels

### Authentification et navigation

1. L'utilisateur se connecte par `POST /api/auth/login`.
2. Le backend authentifie le compte, renvoie JWT et refresh token.
3. `AuthContext` conserve l'identité et le token dans `localStorage`.
4. L'intercepteur Axios ajoute `Authorization: Bearer ...`.
5. `ProtectedRoute` filtre les écrans par rôle côté navigateur.

Références : `frontend/src/context/AuthContext.tsx`, `frontend/src/services/api.ts`, `frontend/src/components/ProtectedRoute.tsx`, `backend/src/main/java/com/ocp/pdr/controller/AuthController.java`, `backend/src/main/java/com/ocp/pdr/security/`.

### Import des données PDR

Le frontend transmet les fichiers en `multipart/form-data` à `POST /api/excel/import-multi` (alias `/api/import-export/import/multi`). Le backend les traite dans l'ordre BOM, stock, consommation, backlog, crée/complète les entités puis enregistre des informations d'import et d'historique. L'import DA est séparé : `POST /api/reportings/import` (alias `/api/demandes/import`).

Références : `frontend/src/pages/import/ImportPage.tsx`, `frontend/src/services/importExportService.ts`, `backend/src/main/java/com/ocp/pdr/controller/ImportExportController.java`, `backend/src/main/java/com/ocp/pdr/service/ExcelImportService.java`, `backend/src/main/java/com/ocp/pdr/service/DemandeAchatService.java`.

### Règles d'approvisionnement

L'écran demande `POST /api/analyse/executer`. `MoteurApprovisionnementService` charge les articles, applique les trois règles et enregistre les résultats. L'écran récupère ensuite le résumé avec `GET /api/analyse/summary` et les propositions avec `GET /api/analyse/resultats`.

### Prévision et anomalies

Le service Java construit un JSON à partir des articles et consommations de la base MySQL, puis appelle FastAPI à l'adresse `AI_SERVICE_URL` (par défaut `http://localhost:8000`). Le prévisionnel passe par `/predict`; la détection statistique passe par `/detect-anomalies`. L'entraînement passe par `/train`, lequel lit le fichier Excel de l'IA et non la base MySQL.

Références : `backend/src/main/java/com/ocp/pdr/service/IaPredictionService.java`, `ai/main.py`, `ai/predictor.py`.

## API REST principales du backend

Toutes les routes backend sont préfixées `/api` sauf Swagger/Actuator.

| Ressource | Routes principales |
|---|---|
| Auth | `GET /auth/health`, `POST /auth/login`, `POST /auth/refresh` |
| Articles | `GET /articles`, `/articles/search`, `/articles/groupe/{groupe}`, `/articles/{id}`, `/articles/code/{codeSAP}`, `/articles/summary`; `POST`, `PUT /{id}`, `DELETE /{id}` |
| Approvisionnement/analyse | `POST /analyse/executer` (alias `/lancer`, `/calculer`); `GET /analyse/resultats`, `/summary`, `/statistiques`, `/etat`, `/anomalies`, `/consommations-inhabituelles`, `/predictions-ia`, `/ml/status`, `/ml/metrics`; `POST /analyse/consommations-inhabituelles/detecter`, `/calculer`, `/predictions-ia/calculer`, `/ml/train`, `/ml/predict` |
| Import/export PDR | Préfixes `/excel` et `/import-export`; `POST /import-multi` et alias, `/validate`, `/import`; exports Excel globaux et par mode |
| DA/reportings | Préfixes `/demandes` et `/reportings`; liste avec filtres, détail, création, modification, suppression, statistiques, export et import |
| Historique DA | `/historiques` et `/historique`, liste et historique d'une demande |
| Utilisateurs | `/utilisateurs`: liste, détail, création/inscription, profil, changement mot de passe et administration |

Les signatures exactes figurent dans les contrôleurs `backend/src/main/java/com/ocp/pdr/controller/`.

## Configuration et limites d'exploitation

- Backend : `server.port=8080`, URL MySQL locale par défaut et `ai.service.url` paramétrable dans `backend/src/main/resources/application.properties`.
- Schéma Hibernate : `spring.jpa.hibernate.ddl-auto=update`. C'est pratique en prototype, mais aucune stratégie de migrations contrôlées n'est visible.
- Taille de téléversement configurée jusqu'à 500 Mo par fichier et 600 Mo par requête.
- FastAPI : CORS autorise `*` avec credentials dans `ai/main.py`; à restreindre pour un déploiement.
- Backend : CORS configuré aussi avec toutes les origines et credentials dans `SecurityConfig.java`.
- Les routes backend exigent un JWT sauf `/api/auth/**`, Actuator, Swagger et OPTIONS. Le filtrage ADMINISTRATEUR/CONSULTANT est clairement implémenté dans les routes frontend, mais aucune annotation `@PreAuthorize` n'a été retrouvée dans les sources backend : un contrôle d'accès côté serveur par rôle n'est donc pas démontré.
- Le backend échoue pour la prévision si Python ne répond pas; le traitement d'anomalies possède un repli statistique Java.
- Le jeton est gardé dans `localStorage`, exposé aux scripts exécutés dans la page. Pour un déploiement, prévoir une configuration sécurisée et une stratégie de gestion du secret JWT.

## Écarts entre services et routes

- `GET /api/analyse/ml/status` renvoie un état constant `available=true`; ce n'est pas un contrôle de disponibilité FastAPI ni une preuve que le modèle est chargé.
- `GET /api/analyse/ml/metrics` exécute la génération des prédictions plutôt que de retourner seulement les métriques persistées.
- Le frontend déclare aussi des appels à `/analyse/anomalies/{id}` et `/analyse/ia/predictions/{codeSAP}` qui ne correspondent pas aux mappings trouvés dans `AnalyseController`; ils ne sont pas utilisés par les pages repérées.
- `GET /api/analyse/anomalies` côté backend ne reçoit pas les paramètres de pagination que le service frontend propose dans son type.

## Sources principales

`README.md`; `backend/pom.xml`; `backend/src/main/resources/application.properties`; `backend/src/main/java/com/ocp/pdr/controller/`; `backend/src/main/java/com/ocp/pdr/security/`; `backend/src/main/java/com/ocp/pdr/service/IaPredictionService.java`; `ai/main.py`; `frontend/src/routes/AppRoutes.tsx`; `frontend/src/services/api.ts`.

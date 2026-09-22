# Plateforme Unifiée de Gestion des Pièces de Rechange (PDR) & Demandes d'Achat (DA) - Groupe OCP

Bienvenue dans le dépôt unifié **OCP-PDR**. Ce projet fusionne en une architecture unique, moderne et cohérente l'ensemble des modules de suivi des Demandes d'Achat (DA), le moteur de règles d'approvisionnement des pièces de rechange (PDR) et le microservice d'intelligence artificielle prédictive.

---

## Architecture Globale

```
ocp2/
├── ai/                       # Microservice IA (FastAPI, Scikit-Learn, Random Forest)
│   ├── main.py               # Endpoints REST (/train, /predict, /health)
│   ├── predictor.py          # Préparation, Random Forest, risques
│   ├── pdr_schemas.py        # Modèles Pydantic de validation
│   ├── tests/                # Tests unitaires IA
│   ├── dataset/              # Dataset manuel consommation_historique.xlsx
│   ├── model/                # Modèle persistant et métadonnées
│   ├── requirements.txt      # Dépendances Python
│   └── Dockerfile            # Conteneur Python 3.11-slim
│
├── backend/                  # Application Backend Unique (Java 21 / Spring Boot 3.4.2)
│   ├── pom.xml               # Configuration Maven unifiée
│   ├── src/main/java/com/ocp/pdr/
│   │   ├── controller/       # Contrôleurs REST (/api/demandes, /api/analyse, /api/excel, /api/auth)
│   │   ├── model/            # Modèles JPA unifiés (Utilisateur, DemandeAchat, ArticlePDR, etc.)
│   │   ├── repository/       # Repositories Spring Data JPA
│   │   ├── service/          # Logique métier, Moteur d'approvisionnement, Traitement Excel POI
│   │   ├── security/         # Sécurité JWT & RBAC (ADMINISTRATEUR, CONSULTANT)
│   │   └── mapper/           # Mappers DTO <-> Entités
│   └── src/main/resources/   # application.properties (Base MySQL: pdr_db)
│
├── frontend/                 # Application Frontend Unique (React 19 / TypeScript / Vite)
│   ├── src/
│   │   ├── components/       # Composants UI (Sidebar, Navbar, PdrTrendChart, Tables)
│   │   ├── pages/
│   │   │   ├── dashboard/    # Tableau de bord unifié (Indicateurs DA & PDR)
│   │   │   ├── reporting/    # Gestion et suivi du workflow des Demandes d'Achat
│   │   │   ├── approvisionnement/ # Moteur de règles (Min-Max, Planifié, Sur Demande)
│   │   │   ├── anomalies/    # Surveillance des anomalies & Prédictions IA
│   │   │   ├── import/       # Centre d'importation Excel (DA & PDR Multi-Fichiers)
│   │   │   ├── historique/   # Traçabilité et audit logs
│   │   │   └── auth/         # Authentification et Profil utilisateur
│   │   ├── services/         # Clients API Axios (api.ts, analyseService, reportingService)
│   │   └── routes/           # Routage React Router v7 & Guards de sécurité
│   ├── package.json
│   └── vite.config.ts
│
├── docker/                   # Configurations Docker
│   ├── backend.Dockerfile
│   ├── frontend.Dockerfile
│   └── nginx.conf
│
└── docker-compose.yml        # Orchestration complète (MySQL, Backend, Frontend, IA)
```

---

## 🚀 Démarrage Rapide

### 1. Via Docker Compose (Recommandé)

Lancez l'ensemble des 4 services en une seule commande :

```bash
docker-compose up --build
```

Les services sont alors disponibles sur :
- **Frontend Web** : [http://localhost](http://localhost)
- **Backend API Spring Boot** : [http://localhost:8080](http://localhost:8080)
- **Documentation Swagger / API** : [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Microservice IA FastAPI** : [http://localhost:8000/docs](http://localhost:8000/docs)
- **Base de données MySQL** : Port `3306`, Base `pdr_db`

---

### 2. Démarrage Local Développement

#### A. Microservice IA (`ai/`)
```bash
cd ai
python -m venv venv
venv\Scripts\activate  # ou source venv/bin/activate
pip install -r requirements.txt
python -m unittest discover -s tests -p "test_*.py"  # Validation des tests
uvicorn main:app --reload --port 8000
```

#### B. Backend Spring Boot (`backend/`)
```bash
cd backend
mvn clean spring-boot:run
```

#### C. Frontend React (`frontend/`)
```bash
cd frontend
npm install
npm run dev
```

---

## 🔐 Identifiants par Défaut (Seeding Automatique)

À l'initialisation de la base, le composant `DataSeeder` crée automatiquement les comptes de démonstration :

| Email | Mot de passe | Rôle | Périmètre |
|---|---|---|---|
| `admin@ocp.ma` | `admin123` | `ADMINISTRATEUR` | Accès complet (CRUD, Imports, Gestion Utilisateurs) |
| `attouchi@ocp.ma` | `admin123` | `ADMINISTRATEUR` | Responsable DA (Achats) |
| `consultant@ocp.ma` | `consultant123` | `CONSULTANT` | Consultation (Lecture seule, Dashboard, Graphiques) |

---

## 📊 Fonctionnalités Métier Unifiées

1. **Tableau de Bord Unifié** : Indicateurs clés (KPIs) en temps réel, volume global des DA, répartition par secteur et par statut, alertes de surconsommation.
2. **Gestion des Demandes d'Achat (DA)** : Suivi bout-en-bout du cycle d'achat (dossiers, codes SAP/Oracle, fournisseurs, délais, statuts de livraison), filtrage multicritère et exports Excel.
3. **Moteur d'Approvisionnement PDR** : Calcul automatique des besoins selon les 3 règles OCP :
   - *Règle 1 (Min-Max)* : Seuil de réapprovisionnement pour les pièces du Groupe Homogène G1.
   - *Règle 2 (Planifié)* : Besoins issus des nomenclatures (BOM) et du backlog d'ordres de travail (OT).
   - *Règle 3 (Sur Demande)* : Commandes ponctuelles sans consommation historique régulière.
4. **Détection d'Anomalies & Prédictions IA** :
   - Règle métier OCP : Alerte si Consommation Mensuelle > 2 × Quantité Installée.
   - Modèle Random Forest : Prédiction de la consommation M+1 avec indice de risque (0-100%) et courbe interactive `PdrTrendChart`.
5. **Centre d'Import Excel** :
   - Importation des reportings DA (18 colonnes avec audit automatique).
   - Importation multi-fichiers des référentiels PDR (Stocks, Consommations, BOM, Backlog OT).
6. **Historique & Traçabilité** : Journalisation intégrale des modifications avec date, utilisateur et valeurs avant/après.

## 🤖 Workflow IA : prédiction du mois prochain

Le dataset d’entraînement est placé manuellement dans `ai/dataset/consommation_historique.xlsx`. Il n’est jamais lu depuis la base de données et aucun bouton d’import n’est nécessaire dans l’interface.

1. **Entraînement** : `POST /train` charge le fichier Excel manuel, le nettoie et entraîne le Random Forest.
2. **Sauvegarde** : le modèle est écrit dans `ai/model/random_forest.joblib` et ses métadonnées dans `ai/model/metadata.json`.
3. **Prédiction** : `POST /predict` charge le modèle sauvegardé et utilise les consommations existantes en base pour trouver le dernier mois disponible et prédire le suivant. Le passage `2026-12` → `2027-01` est géré automatiquement.

### Fichiers Excel acceptés

Les colonnes obligatoires du dataset manuel sont :

- `CODE SAP` ou `CODE ARTICLE SAP` (un alias `SAP` est également accepté) ;
- `Q CONSOMMEE` ou `QUANTITE CONSOMMEE` ;
- `DATE`.

Les colonnes facultatives sont `DESCRIPTION`, `UDM` et `N OT`. Les dates peuvent être au format Excel, `DD.MM.YYYY`, `DD/MM/YYYY` ou `YYYY-MM-DD`. Les quantités négatives sont importées en valeur absolue.

### API IA

- `POST /api/analyse/ml/train` : entraîne explicitement le modèle historique ;
- `POST /api/analyse/ml/predict` : utilise le modèle sauvegardé sans réentraînement ;
- `GET /api/analyse/predictions-ia` : récupère les prédictions disponibles ;
- `GET /api/analyse/ml/status` : statut exposé par le backend.

Le modèle est sauvegardé dans `ai/model/random_forest.joblib` avec ses métriques dans `ai/model/metadata.json`. En Docker, le dossier est monté dans le volume `ai_model` pour survivre au redémarrage du conteneur.

### Démarrage et vérification

```bash
docker compose up --build
```

Ou en local :

```bash
cd ai
pip install -r requirements.txt
uvicorn main:app --reload --port 8000

cd backend
./mvnw spring-boot:run

cd frontend
npm run dev
```

Placez d’abord le fichier manuel dans `ai/dataset/consommation_historique.xlsx`, cliquez sur **Entraîner le modèle**, puis utilisez **Prédire le mois suivant**. Les consommations récentes utilisées par la prédiction proviennent de l’import Excel PDR existant. Les articles `ÉLEVÉ` et `CRITIQUE` sont les anomalies prioritaires.
#   o c p  
 
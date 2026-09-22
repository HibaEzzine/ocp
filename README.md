# OCP PDR - Projet local sans Docker

Ce dépôt contient l’application OCP PDR en mode exécution native sur Windows, sans conteneur Docker.

## Architecture

- `ai/` : microservice Python FastAPI pour les prédictions IA
- `backend/` : API Java Spring Boot sur port 8080
- `frontend/` : application React + Vite sur port 5173

## Prérequis

- Java 21
- Maven 3.9+
- MySQL local (base `pdr_db`)
- Python 3.11+
- Node.js 18+ / npm

## Démarrage local

### 1) Base de données MySQL

Créer la base locale :

```sql
CREATE DATABASE IF NOT EXISTS pdr_db;
```

Le backend utilise par défaut :

- URL : `jdbc:mysql://127.0.0.1:3306/pdr_db`
- utilisateur : `root`
- mot de passe : vide

Variables d’environnement possibles :

```bash
set SPRING_DATASOURCE_URL=jdbc:mysql://127.0.0.1:3306/pdr_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8&useUnicode=true
set SPRING_DATASOURCE_USERNAME=root
set SPRING_DATASOURCE_PASSWORD=
```

### 2) Microservice IA

```bash
cd ai
python -m venv venv
venv\Scripts\activate
pip install -r requirements.txt
python -m unittest discover -s tests -p "test_*.py"
uvicorn main:app --reload --port 8000
```

Le service est ensuite accessible sur :

- `http://localhost:8000/docs`
- `http://localhost:8000/health`

### 3) Backend Spring Boot

```bash
cd backend
mvn clean
mvn test
mvn spring-boot:run
```

L’API est accessible sur :

- `http://localhost:8080`
- `http://localhost:8080/swagger-ui.html`

### 4) Frontend React

```bash
cd frontend
npm install
npm run build
npm run dev
```

Le frontend est accessible sur :

- `http://localhost:5173`

## Comptes de démonstration

Le seeding automatique crée des comptes par défaut lors du démarrage du backend.

- `admin@ocp.ma` / `admin123` / `ADMINISTRATEUR`
- `attouchi@ocp.ma` / `admin123` / `ADMINISTRATEUR`
- `consultant@ocp.ma` / `consultant123` / `CONSULTANT`

## Points clés du projet

- Import Excel DA/PDR
- Moteur d’approvisionnement
- Gestion des demandes d’achat
- Historique et traçabilité
- Analyse d’anomalies
- Prédictions IA via FastAPI

## Important

- Le projet n’utilise pas Docker.
- Les fichiers Docker et les artefacts de conteneur ont été retirés du dépôt.
- Les URLs applicatives et les services restent configurés pour le mode local (`localhost`).

## Vérification finale recommandée

```bash
cd backend
mvn clean
mvn test
mvn spring-boot:run

cd ../frontend
npm install
npm run build
npm run dev
```

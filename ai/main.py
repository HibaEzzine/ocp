import time
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pdr_schemas import PredictionRequest, PredictionResponse, RiskLevel
from predictor import (
    build_training_dataset,
    get_model_status,
    load_manual_training_dataset,
    predict_articles_with_trained_model,
    train_global_random_forest,
    update_model_metadata,
)

app = FastAPI(
    title="Service IA Prédiction Consommation PDR OCP (Random Forest)",
    description="Microservice Machine Learning pour la prédiction des consommations et la détection anticipée des anomalies de pièces de rechange.",
    version="2.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/health")
def health_check():
    return {
        "status": "UP",
        "service": "PDR-IA-Prediction-Service",
        "modele": "Random Forest Regressor",
        "version": "2.0.0",
        "timestamp": int(time.time() * 1000)
    }

@app.post("/train")
def train_model():
    try:
        articles, dataset_info = load_manual_training_dataset()
        if len(build_training_dataset(articles)[0]) < 4:
            raise ValueError("Données historiques insuffisantes pour entraîner le modèle.")
        model, eval_dto = train_global_random_forest(articles, force=True)
        update_model_metadata(dataset_info)
        return {
            "success": model is not None,
            "status": get_model_status(),
            "evaluation": eval_dto.model_dump() if eval_dto is not None else None,
            "dataset": dataset_info,
            "timestamp": int(time.time() * 1000),
        }
    except (FileNotFoundError, ValueError) as e:
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Erreur interne lors de l'entraînement du modèle: {str(e)}")


@app.get("/metrics")
def get_metrics():
    try:
        status = get_model_status()
        return {
            "success": True,
            "status": status,
            "timestamp": int(time.time() * 1000),
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Erreur interne lors de la récupération des métriques: {str(e)}")


@app.get("/status")
def get_status():
    try:
        return {
            "success": True,
            "status": get_model_status(),
            "timestamp": int(time.time() * 1000),
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Erreur interne lors de la récupération du statut du modèle: {str(e)}")


@app.post("/predict", response_model=PredictionResponse)
def predict_consumptions(request: PredictionRequest):
    try:
        articles = request.articles or []
        predictions, eval_dto = predict_articles_with_trained_model(articles, current_month=request.currentMonth)

        nb_critique = sum(1 for p in predictions if p.niveauRisque == RiskLevel.CRITIQUE)
        nb_eleve = sum(1 for p in predictions if p.niveauRisque == RiskLevel.ELEVE)
        nb_alertes = nb_critique + nb_eleve
        nb_moyen = sum(1 for p in predictions if p.niveauRisque == RiskLevel.MOYEN)
        nb_normal = sum(1 for p in predictions if p.niveauRisque == RiskLevel.FAIBLE)
        nb_insuffisant = sum(1 for p in predictions if p.statut == "INSUFFISANT" or p.niveauRisque == RiskLevel.NON_EVALUABLE)

        return PredictionResponse(
            success=True,
            totalArticles=len(predictions),
            nbCritique=nb_critique,
            nbAlertes=nb_alertes,
            nbRisqueMoyen=nb_moyen,
            nbNormal=nb_normal,
            nbInsuffisant=nb_insuffisant,
            predictions=predictions,
            evaluation=eval_dto,
            timestamp=int(time.time() * 1000)
        )
    except ValueError as e:
        raise HTTPException(status_code=409, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Erreur interne dans le modèle prédictif IA: {str(e)}")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)


from enum import Enum
from typing import List, Optional, Any, Dict
from pydantic import BaseModel

class RiskLevel(str, Enum):
    FAIBLE = "FAIBLE"         # Score 0 - 39
    MOYEN = "MOYEN"           # Score 40 - 69
    ELEVE = "ELEVE"           # Score 70 - 84
    CRITIQUE = "CRITIQUE"     # Score 85 - 100
    NON_EVALUABLE = "NON_EVALUABLE"

class ConsommationPoint(BaseModel):
    date: Optional[str] = None
    quantite: Optional[float] = None
    numeroOT: Optional[str] = None

class ArticleData(BaseModel):
    codeSAP: str
    description: Optional[str] = ""
    quantiteInstallee: Optional[float] = 0.0
    categorie: Optional[str] = ""
    historique: Optional[List[ConsommationPoint]] = []

class PredictionRequest(BaseModel):
    articles: List[ArticleData]
    currentMonth: Optional[str] = None

class FeaturesDto(BaseModel):
    lag1: Optional[float] = None
    lag2: Optional[float] = None
    lag3: Optional[float] = None
    moyenne3: Optional[float] = None
    moyenne6: Optional[float] = None
    minRecent: Optional[float] = None
    maxRecent: Optional[float] = None
    ecartType: Optional[float] = None
    tendance: Optional[str] = "STABLE" # "FORTE_HAUSSE", "HAUSSE", "STABLE", "BAISSE"
    variationVsMoyenne: Optional[float] = None
    ratioConsoInstallee: Optional[float] = None

class ModelEvaluationDto(BaseModel):
    modele: str = "Random Forest Regressor"
    mae: float = 0.0
    rmse: float = 0.0
    r2: Optional[float] = None
    baselineMae: float = 0.0
    baselineRmse: float = 0.0
    nbSamplesTrained: int = 0
    ameliorationPct: float = 0.0

class PredictionResult(BaseModel):
    codeSAP: str
    description: str
    quantiteInstallee: float
    consommationActuelle: float
    consommationPredite: Optional[float] = None
    seuil: float
    ratio: float
    scoreRisque: int # 0 à 100
    niveauRisque: RiskLevel
    alerte: bool
    statut: str
    message: str
    explications: List[str] = []
    features: Optional[FeaturesDto] = None
    historique: List[Dict[str, Any]] = []
    nbMoisHistorique: int = 0
    moisActuel: Optional[str] = None
    moisPrevu: Optional[str] = None
    modeleUtilise: str = "RANDOM_FOREST_PYTHON"

class PredictionResponse(BaseModel):
    success: bool
    totalArticles: int
    nbCritique: int = 0
    nbAlertes: int = 0
    nbRisqueMoyen: int = 0
    nbNormal: int = 0
    nbInsuffisant: int = 0
    predictions: List[PredictionResult]
    evaluation: Optional[ModelEvaluationDto] = None
    timestamp: Optional[int] = None


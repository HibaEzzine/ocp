import api from './api';

export interface AnalysisSummary {
  totalArticles: number;
  minMaxCount: number;
  planifieCount: number;
  surDemandeCount: number;
  anomaliesCount: number;
  articlesEnStock: number;
  minMaxRate: number;
  planifieRate: number;
  surDemandeRate: number;
  stockRate: number;
}

export interface AnomalieItem {
  id: number;
  codeSAP: string;
  designation: string;
  secteur: string;
  consommationMensuelle: number;
  quantiteInstallee: number;
  tauxConsommation: number;
  seuil: number;
  dateDetection: string;
  statut: string;
  commentaire?: string;
  article?: {
    codeSAP?: string;
    description?: string;
    secteur?: { nom?: string };
  };
}

export interface PredictionResult {
  codeSAP: string;
  description: string;
  quantiteInstallee: number;
  consommationActuelle: number;
  consommationPredite: number;
  seuil: number;
  ratio: number;
  scoreRisque: number;
  niveauRisque: 'FAIBLE' | 'MOYEN' | 'ELEVE' | 'CRITIQUE' | 'NON_EVALUABLE';
  alerte: boolean;
  statut: string;
  message: string;
  explications: string[];
  features?: Record<string, unknown>;
  historique?: Array<{ mois: string; quantite: number }>;
  nbMoisHistorique?: number;
  moisActuel?: string;
  moisPrevu?: string;
  modeleUtilise?: string;
}

export interface ModelEvaluation {
  modele: string;
  mae: number;
  rmse: number;
  r2?: number;
  baselineMae?: number;
  baselineRmse?: number;
  nbSamplesTrained?: number;
  ameliorationPct?: number;
}

export interface PredictionResponse {
  success: boolean;
  totalArticles: number;
  nbCritique: number;
  nbAlertes: number;
  nbRisqueMoyen: number;
  nbNormal: number;
  nbInsuffisant: number;
  predictions: PredictionResult[];
  evaluation?: ModelEvaluation;
  message?: string;
}

export interface ResultatAppro {
  id: number;
  codeSAP: string;
  designation: string;
  mode: 'MIN_MAX' | 'PLANIFIE' | 'SUR_DEMANDE';
  quantiteALancer: number;
  priorite: number;
  justification: string;
  dateAnalyse: string;
}

export const analyseService = {
  getSummary: () => api.get<AnalysisSummary>('/analyse/summary'),

  getAnomalies: (params?: { page?: number; size?: number; search?: string; statut?: string }) =>
    api.get<AnomalieItem[] | { content: AnomalieItem[]; totalElements: number; totalPages: number }>('/analyse/anomalies', { params }),

  traiterAnomalie: (id: number, statut: string, commentaire?: string) =>
    api.put(`/analyse/anomalies/${id}`, { statut, commentaire }),

  getPredictionsIa: () =>
    api.get<PredictionResponse>('/analyse/predictions-ia'),

  trainModel: () => api.post<PredictionResponse>('/analyse/ml/train'),

  predictModel: () => api.post<PredictionResponse>('/analyse/ml/predict'),

  getModelMetrics: () => api.get<PredictionResponse>('/analyse/ml/metrics'),

  getModelStatus: () => api.get<{ service: string; available: boolean; message: string }>('/analyse/ml/status'),

  getPredictionIa: (codeSAP: string) =>
    api.get<PredictionResult>(`/analyse/ia/predictions/${codeSAP}`),

  getHistoriqueArticle: (codeSAP: string) =>
    api.get<Array<{ mois: string; quantite: number }>>(`/analyse/ia/predictions/${codeSAP}/historique`),

  getResultatsAppro: (params?: { page?: number; size?: number; mode?: string; search?: string }) =>
    api.get<ResultatAppro[] | { content: ResultatAppro[]; totalElements: number; totalPages: number }>('/analyse/resultats', { params }),

  lancerRegles: () => api.post('/analyse/executer'),
};

export default analyseService;

import api from './api';
import type { HistoriqueDto } from '../types/historique';

const historiqueService = {
  getAll: () => api.get<HistoriqueDto[]>('/historiques'),
};

export default historiqueService;

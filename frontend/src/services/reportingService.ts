import api from './api';
import type { ReportingDto } from '../types/reporting';

const reportingService = {
  getAll: (params?: Record<string, string>) => api.get<ReportingDto[]>('/reportings', { params }),
  getById: (id: number) => api.get<ReportingDto>(`/reportings/${id}`),
  create: (payload: ReportingDto) => api.post<ReportingDto>('/reportings', payload),
  update: (id: number, payload: ReportingDto) => api.put<ReportingDto>(`/reportings/${id}`, payload),
  remove: (id: number) => api.delete(`/reportings/${id}`),
  exportReportings: (params?: Record<string, string>) => api.get('/reportings/export', { params, responseType: 'blob' }),
  importFile: (formData: FormData) => api.post('/reportings/import', formData),
};

export default reportingService;

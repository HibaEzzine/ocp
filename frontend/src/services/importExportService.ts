import api from './api';

export const importExportService = {
  validateExcel: (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post('/excel/validate', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  importMultiFiles: (formData: FormData) =>
    api.post('/excel/import-multi', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }),


  importSingleFile: (file: File, type: string) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('type', type);
    return api.post('/excel/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  importExcelDA: (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post('/demandes/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  exportAnomalies: () =>
    api.get('/excel/export/anomalies', { responseType: 'blob' }),

  exportResultatsAppro: () =>
    api.get('/excel/export/resultats', { responseType: 'blob' }),

  exportMinMax: () =>
    api.get('/excel/export/min-max', { responseType: 'blob' }),

  exportPlanifie: () =>
    api.get('/excel/export/planifie', { responseType: 'blob' }),

  exportSurDemande: () =>
    api.get('/excel/export/sur-demande', { responseType: 'blob' }),

  exportDemandesAchat: () =>
    api.get('/demandes/export', { responseType: 'blob' }),
};

export default importExportService;

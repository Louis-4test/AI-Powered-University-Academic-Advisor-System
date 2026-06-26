import api from '../axios';

export const predictRisk = (gradeId) =>
  api.get(`/ai/risk-prediction/grade/${gradeId}`);

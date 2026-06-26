import api from '../axios';

export const analyzeDocument = (file) => {
  const formData = new FormData();
  formData.append('file', file);
  return api.post('/ai/research-assistant/analyze', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

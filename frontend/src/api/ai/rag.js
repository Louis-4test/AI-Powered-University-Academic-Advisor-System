import api from '../axios';

export const ingestDocument = (file) => {
  const formData = new FormData();
  formData.append('file', file);
  return api.post('/api/ai/rag/ingest', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

export const queryRag = (question) => {
  return api.post('/api/ai/rag/query', { question });
};

import api from '../axios';

export const ingestDocument = (file) => {
  const formData = new FormData();
  formData.append('file', file);
  return api.post('/ai/rag/ingest', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

export const queryRag = (question) => {
  return api.post('/ai/rag/query', { question });
};

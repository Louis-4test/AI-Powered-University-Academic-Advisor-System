import api from '../axios';

export const generateExam = (topic, difficulty, questionCount, questionTypes) =>
  api.post('/ai/exam-generator/generate', { topic, difficulty, questionCount, questionTypes });

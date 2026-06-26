import api from '../axios';

export const askChatbot = (question, studentId) =>
  api.post('/ai/chatbot/ask', { question, studentId });

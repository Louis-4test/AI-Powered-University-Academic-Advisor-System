import api from '../axios';

export const getCareerRecommendations = (studentId, additionalSkills) =>
  api.post('/ai/career-recommendation/recommend', { studentId, additionalSkills });

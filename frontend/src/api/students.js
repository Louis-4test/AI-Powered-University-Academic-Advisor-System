import api from './axios';

export const getStudents = () => api.get('/students');
export const getStudent = (id) => api.get(`/students/${id}`);
export const getMyProfile = () => api.get('/students/me');
export const searchStudents = (keyword) => api.get('/students/search', { params: { keyword } });
export const createStudent = (data) => api.post('/students', data);
export const updateStudent = (id, data) => api.put(`/students/${id}`, data);
export const deleteStudent = (id) => api.delete(`/students/${id}`);
export const getHighAchievers = (minCgpa = 3.5) => api.get('/students/high-achievers', { params: { minCgpa } });
export const getStatsByLevel = () => api.get('/students/stats/by-level');
export const getStatsByStatus = () => api.get('/students/stats/by-status');

import api from './axios';

export const getLecturers = () => api.get('/lecturers');
export const getLecturer = (id) => api.get(`/lecturers/${id}`);
export const getMyLecturerProfile = () => api.get('/lecturers/me');
export const getMyStudents = () => api.get('/lecturers/me/students');
export const searchLecturers = (keyword) => api.get('/lecturers/search', { params: { keyword } });
export const createLecturer = (data) => api.post('/lecturers', data);
export const updateLecturer = (id, data) => api.put(`/lecturers/${id}`, data);
export const deleteLecturer = (id) => api.delete(`/lecturers/${id}`);

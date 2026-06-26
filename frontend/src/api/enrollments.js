import api from './axios';

export const enrollStudent = (data) => api.post('/enrollments', data);
export const dropEnrollment = (id) => api.patch(`/enrollments/${id}/drop`);
export const completeEnrollment = (id) => api.patch(`/enrollments/${id}/complete`);
export const getEnrollmentsByStudent = (studentId) => api.get(`/enrollments/student/${studentId}`);
export const getEnrollmentsByCourse = (courseId) => api.get(`/enrollments/course/${courseId}`);

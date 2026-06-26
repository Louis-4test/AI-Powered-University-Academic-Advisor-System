import api from './axios';

export const getCourses = () => api.get('/courses');
export const getCourse = (id) => api.get(`/courses/${id}`);
export const searchCourses = (keyword) => api.get('/courses/search', { params: { keyword } });
export const createCourse = (data) => api.post('/courses', data);
export const updateCourse = (id, data) => api.put(`/courses/${id}`, data);
export const deleteCourse = (id) => api.delete(`/courses/${id}`);
export const assignLecturer = (courseId, lecturerId) =>
  api.patch(`/courses/${courseId}/assign-lecturer/${lecturerId}`);
export const getCoursesByStudent = (studentId) => api.get(`/courses/student/${studentId}`);
export const getCoursesByLecturer = (lecturerId) => api.get(`/courses/lecturer/${lecturerId}`);

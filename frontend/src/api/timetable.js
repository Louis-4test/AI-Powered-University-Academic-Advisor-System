import api from './axios';

export const getTimetable = () => api.get('/timetable');
export const getTimetableById = (id) => api.get(`/timetable/${id}`);
export const getTimetableByDepartment = (deptId) => api.get(`/timetable/department/${deptId}`);
export const getTimetableByLecturer = (lecturerId) => api.get(`/timetable/lecturer/${lecturerId}`);
export const getTimetableByStudent = (studentId) => api.get(`/timetable/student/${studentId}`);
export const getTimetableByCourse = (courseId) => api.get(`/timetable/course/${courseId}`);
export const createTimetableEntry = (data) => api.post('/timetable', data);
export const updateTimetableEntry = (id, data) => api.put(`/timetable/${id}`, data);
export const deleteTimetableEntry = (id) => api.delete(`/timetable/${id}`);
export const generateTimetable = (data) => api.post('/timetable/generate', data);

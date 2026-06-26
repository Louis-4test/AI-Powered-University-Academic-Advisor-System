import api from './axios';

export const recordGrade = (data) => api.post('/grades', data);
export const updateGrade = (id, data) => api.put(`/grades/${id}`, data);
export const getStudentGrades = (studentId) => api.get(`/grades/student/${studentId}`);
export const getCgpa = (studentId) => api.get(`/grades/student/${studentId}/cgpa`);
export const getSemesterGpa = (studentId, academicYear, semester) =>
  api.get(`/grades/student/${studentId}/gpa`, { params: { academicYear, semester } });
export const getPerformanceTrend = (studentId) => api.get(`/grades/student/${studentId}/trend`);
export const getClassRanking = () => api.get('/grades/ranking');

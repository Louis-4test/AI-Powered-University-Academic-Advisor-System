import api from './axios';

export const login = (email, password) =>
  api.post('/auth/login', { email, password });

export const register = (fullName, email, password, role) =>
  api.post('/auth/register', { fullName, email, password, role });

export const forgotPassword = (email) =>
  api.post('/auth/forgot-password', { email });

export const resetPassword = (token, newPassword) =>
  api.post('/auth/reset-password', { token, newPassword });

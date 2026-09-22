import api from './api';
import type { User } from '../types/user';

const authService = {
  login: async (email: string, password: string) => {
    const response = await api.post<{ token: string }>('/auth/login', { email, password });
    const token = response.data.token;
    localStorage.setItem('authToken', token);
    return token;
  },
  logout: () => {
    localStorage.removeItem('authToken');
  },
  getCurrentUser: () => api.get<User>('/utilisateurs/me'),
};

export default authService;

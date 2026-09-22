import api from './api';
import type { User, ProfileUpdatePayload, PasswordUpdatePayload } from '../types/user';

const userService = {
  login: () => api.get<User>('/utilisateurs/me'),
  getCurrent: () => api.get<User>('/utilisateurs/me'),
  updateProfile: (payload: ProfileUpdatePayload) => api.put<User>('/utilisateurs/me', payload),
  updatePassword: (payload: PasswordUpdatePayload) => api.put('/utilisateurs/me/mot-de-passe', payload),
};

export default userService;

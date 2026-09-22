export interface User {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  role: string;
}

export interface ProfileUpdatePayload {
  nom: string;
  prenom: string;
  email: string;
  role?: string;
}

export interface PasswordUpdatePayload {
  currentPassword: string;
  newPassword: string;
}

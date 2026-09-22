export type UserRole = 'ADMINISTRATEUR' | 'CONSULTANT' | 'UTILISATEUR';

export interface AuthState {
  token: string | null;
  user: {
    id: number;
    nom: string;
    prenom: string;
    email: string;
    role: UserRole;
  } | null;
}

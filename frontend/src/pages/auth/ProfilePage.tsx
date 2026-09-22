import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import userService from '../../services/userService';
import type { ProfileUpdatePayload } from '../../types/user';
import Alert from '../../components/common/Alert';
import { User, Lock, Save, Shield, Key } from 'lucide-react';

interface ProfileForm {
  nom: string;
  prenom: string;
  email: string;
}

interface PasswordForm {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

const ProfilePage: React.FC = () => {
  const navigate = useNavigate();
  const { user, updateUser, logout } = useAuth();
  const [profile, setProfile] = useState<ProfileForm>({ nom: '', prenom: '', email: '' });
  const [password, setPassword] = useState<PasswordForm>({ currentPassword: '', newPassword: '', confirmPassword: '' });
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (user) {
      setProfile({ nom: user.nom, prenom: user.prenom, email: user.email });
    }
  }, [user]);

  const handleProfileSave = async (event: React.FormEvent) => {
    event.preventDefault();
    setError(null);
    setMessage(null);
    setLoading(true);

    try {
      const payload: ProfileUpdatePayload = {
        nom: profile.nom,
        prenom: profile.prenom,
        email: profile.email,
        role: user?.role,
      };
      const response = await userService.updateProfile(payload);
      if (response.data) {
        if (response.data.email !== user?.email) {
          setMessage('Profil mis à jour avec succès. Veuillez vous reconnecter avec votre nouvel email.');
          logout();
          navigate('/login');
          return;
        }
        updateUser(response.data);
      }
      setMessage('Informations du profil mises à jour avec succès.');
    } catch (err) {
      setError('Impossible de mettre à jour le profil.');
    } finally {
      setLoading(false);
    }
  };

  const handlePasswordSave = async (event: React.FormEvent) => {
    event.preventDefault();
    setError(null);
    setMessage(null);

    if (password.newPassword !== password.confirmPassword) {
      setError('Les nouveaux mots de passe ne correspondent pas.');
      return;
    }

    setLoading(true);
    try {
      await userService.updatePassword({
        currentPassword: password.currentPassword,
        newPassword: password.newPassword,
      });
      setMessage('Mot de passe modifié avec succès. Veuillez vous reconnecter.');
      setPassword({ currentPassword: '', newPassword: '', confirmPassword: '' });
      logout();
      navigate('/login');
    } catch (err) {
      setError('Impossible de modifier le mot de passe.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="app-content" style={{ maxWidth: '840px', margin: '0 auto', display: 'flex', flexDirection: 'column', gap: '24px' }}>
      <div>
        <h2 style={{ fontSize: '22px', fontWeight: 700, color: 'var(--ocp-text)' }}>Profil Utilisateur</h2>
      </div>

      {message && <Alert variant="success">{message}</Alert>}
      {error && <Alert variant="danger">{error}</Alert>}

      <div className="card-ocp" style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
        <div className="app-navbar__avatar" style={{ width: '64px', height: '64px', fontSize: '24px' }}>
          {user?.nom ? user.nom.charAt(0).toUpperCase() : 'U'}
        </div>
        <div>
          <h3 style={{ fontSize: '18px', fontWeight: 700, color: 'var(--ocp-text)' }}>
            {user?.prenom} {user?.nom}
          </h3>
          <div style={{ fontSize: '13px', color: 'var(--ocp-text-muted)', marginTop: '2px', display: 'flex', alignItems: 'center', gap: '12px' }}>
            <span>{user?.email}</span>
            <span className="badge-ocp badge-ocp--livre">
              <Shield size={12} />
              {user?.role ? user.role.replace('ROLE_', '') : 'UTILISATEUR'}
            </span>
          </div>
        </div>
      </div>

      <div className="card-ocp">
        <h3 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--ocp-primary)', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
          <User size={18} />
          Informations Personnelles
        </h3>

        <form onSubmit={handleProfileSave} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <div className="form-grid-2">
            <div className="form-group-ocp">
              <label>Nom</label>
              <input
                type="text"
                className="form-control-ocp"
                value={profile.nom}
                onChange={(e) => setProfile({ ...profile, nom: e.target.value })}
                required
              />
            </div>
            <div className="form-group-ocp">
              <label>Prénom</label>
              <input
                type="text"
                className="form-control-ocp"
                value={profile.prenom}
                onChange={(e) => setProfile({ ...profile, prenom: e.target.value })}
                required
              />
            </div>
            <div className="form-group-ocp" style={{ gridColumn: '1 / -1' }}>
              <label>Adresse Email</label>
              <input
                type="email"
                className="form-control-ocp"
                value={profile.email}
                onChange={(e) => setProfile({ ...profile, email: e.target.value })}
                required
              />
            </div>
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '8px' }}>
            <button type="submit" className="btn-ocp btn-ocp-primary" disabled={loading}>
              <Save size={16} />
              <span>Enregistrer le Profil</span>
            </button>
          </div>
        </form>
      </div>

      <div className="card-ocp">
        <h3 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--ocp-primary)', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Key size={18} />
          Sécurité & Mot de passe
        </h3>

        <form onSubmit={handlePasswordSave} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <div className="form-group-ocp">
            <label>Mot de passe actuel</label>
            <input
              type="password"
              className="form-control-ocp"
              value={password.currentPassword}
              onChange={(e) => setPassword({ ...password, currentPassword: e.target.value })}
              required
            />
          </div>

          <div className="form-grid-2">
            <div className="form-group-ocp">
              <label>Nouveau mot de passe</label>
              <input
                type="password"
                className="form-control-ocp"
                value={password.newPassword}
                onChange={(e) => setPassword({ ...password, newPassword: e.target.value })}
                required
              />
            </div>
            <div className="form-group-ocp">
              <label>Confirmer le nouveau mot de passe</label>
              <input
                type="password"
                className="form-control-ocp"
                value={password.confirmPassword}
                onChange={(e) => setPassword({ ...password, confirmPassword: e.target.value })}
                required
              />
            </div>
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '8px' }}>
            <button type="submit" className="btn-ocp btn-ocp-primary" disabled={loading}>
              <Lock size={16} />
              <span>Changer le Mot de passe</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ProfilePage;

import React from 'react';
import { Moon, Sun, LogOut } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

interface NavbarProps {
  userName: string;
  onToggleTheme: () => void;
  theme: 'light' | 'dark';
  onLogout: () => void;
}

const Navbar: React.FC<NavbarProps> = ({ userName, onToggleTheme, theme, onLogout }) => {
  const { user } = useAuth();
  const roleName = user?.role ? user.role.replace('ROLE_', '') : 'UTILISATEUR';

  return (
    <header className="app-navbar">
      <div className="app-navbar__left">
        <h1 className="app-navbar__title">Système de Reporting PDR</h1>
      </div>

      <div className="app-navbar__actions">
        <button type="button" className="app-navbar__icon-btn" onClick={onToggleTheme} title="Changer de thème">
          {theme === 'light' ? <Moon size={18} /> : <Sun size={18} />}
        </button>

        <div className="app-navbar__profile">
          <div className="app-navbar__avatar">{userName ? userName.charAt(0).toUpperCase() : 'U'}</div>
          <div className="app-navbar__user">
            <strong>{userName}</strong>
            <span>{roleName}</span>
          </div>
        </div>

        <button type="button" className="app-navbar__logout-btn" onClick={onLogout} title="Déconnexion">
          <LogOut size={16} />
          <span>Déconnexion</span>
        </button>
      </div>
    </header>
  );
};

export default Navbar;

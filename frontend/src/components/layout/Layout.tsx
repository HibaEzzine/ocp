import React, { useEffect, useMemo, useState } from 'react';
import { Outlet } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import Sidebar from './Sidebar';
import Navbar from './Navbar';

const Layout: React.FC = () => {
  const { user, logout } = useAuth();
  const [theme, setTheme] = useState<'light' | 'dark'>(() => {
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'light' || savedTheme === 'dark') {
      return savedTheme;
    }

    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  });

  const handleLogout = () => {
    logout();
    window.location.href = '/login';
  };

  const toggleTheme = () => {
    setTheme((currentTheme) => (currentTheme === 'dark' ? 'light' : 'dark'));
  };

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('theme', theme);
  }, [theme]);

  const userName = useMemo(() => {
    if (!user) return 'Utilisateur';
    return `${user.prenom} ${user.nom}`;
  }, [user]);

  return (
    <div className="app-shell">
      <Sidebar isConsultant={user?.role === 'CONSULTANT'} />
      <div className="app-main">
        <Navbar
          theme={theme}
          onToggleTheme={toggleTheme}
          userName={userName}
          onLogout={handleLogout}
        />
        <main className="app-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default Layout;

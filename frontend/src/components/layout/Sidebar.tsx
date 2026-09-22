import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import {
  LayoutDashboard,
  FileSpreadsheet,
  History,
  BarChart3,
  User,
  Boxes,
  BrainCircuit,
} from 'lucide-react';
import ocpLogo from '../../assets/ocp-logo.png';

interface MenuItem {
  label: string;
  path: string;
  icon: React.ComponentType<{ size?: number; className?: string }>;
}

interface SidebarProps {
  isConsultant: boolean;
}

const Sidebar: React.FC<SidebarProps> = ({ isConsultant }) => {
  const location = useLocation();

  const menuItems: MenuItem[] = [
    { label: 'Tableau de bord', path: '/', icon: LayoutDashboard },
    { label: 'Demandes Achat (DA)', path: '/reporting', icon: BarChart3 },
    { label: 'Approvisionnement', path: '/approvisionnement', icon: Boxes },
    { label: 'Anomalies & IA', path: '/anomalies', icon: BrainCircuit },
    { label: 'Importer Données', path: '/import', icon: FileSpreadsheet },
    { label: 'Historique & Audit', path: '/historique', icon: History },
    { label: 'Mon Profil', path: '/profile', icon: User },
  ];

  const visibleItems = isConsultant
    ? menuItems.filter((item) =>
        item.path === '/' ||
        item.path === '/profile'
      )
    : menuItems;

  return (
    <aside className="app-sidebar">
      <div className="app-sidebar__brand">
        <div className="app-sidebar__logo">
          <img src={ocpLogo} alt="OCP Logo" />
        </div>
        <div className="app-sidebar__brand-text">
          <span>GROUPE OCP</span>
          <strong>PDR & ACHATS</strong>
        </div>
      </div>

      <nav className="app-sidebar__nav">
        {visibleItems.map((item) => {
          const active = item.path === location.pathname;
          const Icon = item.icon;
          return (
            <Link
              key={item.label}
              to={item.path}
              className={`app-sidebar__item ${active ? 'active' : ''}`}
            >
              <Icon size={18} className="app-sidebar__item-icon" />
              <span>{item.label}</span>
            </Link>
          );
        })}
      </nav>
    </aside>
  );
};

export default Sidebar;

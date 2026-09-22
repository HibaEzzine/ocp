import React from 'react';

interface BadgeProps {
  label: string;
  variant?: 'default' | 'success' | 'warning' | 'danger' | 'info' | 'livre' | 'attente' | 'retard' | 'encours' | 'ecarte' | 'litige' | 'adjuge';
  className?: string;
}

const Badge: React.FC<BadgeProps> = ({ label, variant = 'default', className = '' }) => {
  const getBadgeClass = () => {
    switch (variant) {
      case 'livre':
      case 'success':
        return 'badge-ocp--livre';
      case 'attente':
      case 'warning':
        return 'badge-ocp--attente';
      case 'retard':
      case 'ecarte':
      case 'litige':
      case 'danger':
        return 'badge-ocp--retard';
      case 'encours':
      case 'info':
        return 'badge-ocp--encours';
      case 'adjuge':
        return 'badge-ocp--adjuge';
      default:
        return 'badge-ocp--encours';
    }
  };

  return <span className={`badge-ocp ${getBadgeClass()} ${className}`}>{label}</span>;
};

export default Badge;

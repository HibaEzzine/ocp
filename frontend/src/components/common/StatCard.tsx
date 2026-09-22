import React from 'react';

interface StatCardProps {
  title: string;
  value: number | string;
  subtitle?: string;
  icon?: React.ReactNode;
  variant?: 'primary' | 'success' | 'warning' | 'danger' | 'info';
  className?: string;
}

const StatCard: React.FC<StatCardProps> = ({
  title,
  value,
  subtitle,
  icon,
  variant = 'primary',
  className = '',
}) => {
  const getIconBg = () => {
    switch (variant) {
      case 'success':
        return { bg: 'var(--ocp-success-bg)', color: 'var(--ocp-success)' };
      case 'warning':
        return { bg: 'var(--ocp-warning-bg)', color: 'var(--ocp-warning)' };
      case 'danger':
        return { bg: 'var(--ocp-danger-bg)', color: 'var(--ocp-danger)' };
      case 'info':
        return { bg: 'var(--ocp-info-bg)', color: 'var(--ocp-info)' };
      default:
        return { bg: 'var(--ocp-primary-soft)', color: 'var(--ocp-primary)' };
    }
  };

  const style = getIconBg();

  return (
    <div className={`stat-card-ocp ${className}`}>
      <div className="stat-card-ocp__info">
        <span>{title}</span>
        <h3>{value}</h3>
        {subtitle && <div style={{ fontSize: '12px', color: 'var(--ocp-text-muted)', marginTop: '4px' }}>{subtitle}</div>}
      </div>
      {icon && (
        <div className="stat-card-ocp__icon" style={{ backgroundColor: style.bg, color: style.color }}>
          {icon}
        </div>
      )}
    </div>
  );
};

export default StatCard;

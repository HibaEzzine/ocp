import React from 'react';

interface AlertProps {
  variant?: 'success' | 'danger' | 'warning' | 'info';
  className?: string;
  children: React.ReactNode;
}

const Alert: React.FC<AlertProps> = ({ variant = 'info', className = '', children }) => (
  <div className={`ui-alert ui-alert--${variant} ${className}`} role="alert">
    {children}
  </div>
);

export default React.memo(Alert);

import React from 'react';

interface CardProps {
  children: React.ReactNode;
  className?: string;
  style?: React.CSSProperties;
}

const Card: React.FC<CardProps> = ({ children, className = '', style }) => (
  <div className={`ui-card ${className}`} style={style}>
    {children}
  </div>
);

export default Card;

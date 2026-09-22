import React from 'react';

interface TypographyProps {
  title: string;
  subtitle?: string;
}

const Typography: React.FC<TypographyProps> = ({ title, subtitle }) => (
  <div className="ui-typography">
    <p className="ui-typography__subtitle">{subtitle}</p>
    <h2 className="ui-typography__title">{title}</h2>
  </div>
);

export default React.memo(Typography);

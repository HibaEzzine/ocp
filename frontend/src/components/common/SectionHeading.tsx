import React from 'react';

interface SectionHeadingProps {
  title: string;
  subtitle?: string;
}

const SectionHeading: React.FC<SectionHeadingProps> = ({ title, subtitle }) => (
  <div className="section-heading">
    <div>
      <p className="section-heading__subtitle">{subtitle}</p>
      <h2 className="section-heading__title">{title}</h2>
    </div>
  </div>
);

export default SectionHeading;

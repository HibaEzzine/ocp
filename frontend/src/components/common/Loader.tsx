import React from 'react';

interface LoaderProps {
  type?: 'spinner' | 'skeleton-card' | 'skeleton-table';
  rows?: number;
}

const Loader: React.FC<LoaderProps> = ({ type = 'spinner', rows = 4 }) => {
  if (type === 'skeleton-card') {
    return (
      <div className="card-ocp" style={{ padding: '24px' }}>
        <div className="skeleton" style={{ width: '40%', height: '16px', marginBottom: '12px' }} />
        <div className="skeleton" style={{ width: '70%', height: '32px', marginBottom: '8px' }} />
        <div className="skeleton" style={{ width: '30%', height: '14px' }} />
      </div>
    );
  }

  if (type === 'skeleton-table') {
    return (
      <div className="table-responsive-ocp" style={{ padding: '16px' }}>
        {Array.from({ length: rows }).map((_, idx) => (
          <div key={idx} style={{ display: 'flex', gap: '16px', marginBottom: '12px' }}>
            <div className="skeleton" style={{ flex: 1, height: '24px' }} />
            <div className="skeleton" style={{ flex: 2, height: '24px' }} />
            <div className="skeleton" style={{ flex: 1, height: '24px' }} />
            <div className="skeleton" style={{ flex: 1, height: '24px' }} />
          </div>
        ))}
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '40px', gap: '12px', color: 'var(--ocp-primary)' }} role="status" aria-live="polite">
      <div className="skeleton" style={{ width: '28px', height: '28px', borderRadius: '50%' }} />
      <span style={{ fontWeight: 600, fontSize: '14px' }}>Chargement des données...</span>
    </div>
  );
};

export default React.memo(Loader);

import React from 'react';
import { Link } from 'react-router-dom';
import { RoutePaths } from '../routes/paths';

const NotFoundPage: React.FC = () => (
  <div className="ui-page-center ui-text-center">
    <div style={{ width: '100%', maxWidth: 540 }}>
      <div style={{ marginBottom: 24 }}>
        <h1 style={{ fontSize: 'clamp(4rem, 8vw, 6rem)', margin: 0 }}>404</h1>
        <p className="ui-text-muted">Page introuvable.</p>
      </div>
      <Link className="ui-button ui-button--primary ui-button--block" to={RoutePaths.root}>
        Retour à l'accueil
      </Link>
    </div>
  </div>
);

export default NotFoundPage;

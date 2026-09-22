import React from 'react';

const Loading: React.FC = () => (
  <div className="ui-loading">
    <div className="ui-loading__spinner" />
    <span>Chargement...</span>
  </div>
);

export default Loading;

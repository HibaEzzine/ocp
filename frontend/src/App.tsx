import React from 'react';
import { useAuth } from './context/AuthContext';
import { Navigate } from 'react-router-dom';
import Loading from './components/common/Loading';

const App: React.FC = () => {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div className="ui-page-center">
        <Loading />
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return <Navigate to="/" replace />;
};

export default App;


import React, { lazy, Suspense } from 'react';
import { Routes, Route } from 'react-router-dom';
import ProtectedRoute from '../components/ProtectedRoute';
import Layout from '../components/layout/Layout';
import Loader from '../components/common/Loader';
import { RoutePaths } from './paths';

const LoginPage = lazy(() => import('../pages/auth/LoginPage'));
const Dashboard = lazy(() => import('../pages/dashboard/Dashboard'));
const ImportPage = lazy(() => import('../pages/import/ImportPage'));
const ReportingPage = lazy(() => import('../pages/reporting/ReportingPage'));
const ApprovisionnementPage = lazy(() => import('../pages/approvisionnement/ApprovisionnementPage'));
const AnomaliesPage = lazy(() => import('../pages/anomalies/AnomaliesPage'));
const HistoriquePage = lazy(() => import('../pages/historique/HistoriquePage'));
const ProfilePage = lazy(() => import('../pages/auth/ProfilePage'));
const NotFoundPage = lazy(() => import('../pages/NotFoundPage'));

const AppRoutes: React.FC = () => (
  <Suspense fallback={<Loader />}>
    <Routes>
      <Route path={RoutePaths.login} element={<LoginPage />} />
      <Route path={RoutePaths.root} element={<ProtectedRoute><Layout /></ProtectedRoute>}>
        <Route index element={<ProtectedRoute allowedRoles={['ADMINISTRATEUR', 'CONSULTANT']}><Dashboard /></ProtectedRoute>} />
        <Route path={RoutePaths.reporting.slice(1)} element={<ProtectedRoute allowedRoles={['ADMINISTRATEUR']}><ReportingPage /></ProtectedRoute>} />
        <Route path={RoutePaths.demandes.slice(1)} element={<ProtectedRoute allowedRoles={['ADMINISTRATEUR']}><ReportingPage /></ProtectedRoute>} />
        <Route path={RoutePaths.approvisionnement.slice(1)} element={<ProtectedRoute allowedRoles={['ADMINISTRATEUR']}><ApprovisionnementPage /></ProtectedRoute>} />
        <Route path={RoutePaths.anomalies.slice(1)} element={<ProtectedRoute allowedRoles={['ADMINISTRATEUR']}><AnomaliesPage /></ProtectedRoute>} />
        <Route path={RoutePaths.import.slice(1)} element={<ProtectedRoute allowedRoles={['ADMINISTRATEUR']}><ImportPage /></ProtectedRoute>} />
        <Route path={RoutePaths.historique.slice(1)} element={<ProtectedRoute allowedRoles={['ADMINISTRATEUR']}><HistoriquePage /></ProtectedRoute>} />
        <Route path={RoutePaths.profile.slice(1)} element={<ProtectedRoute allowedRoles={['ADMINISTRATEUR', 'CONSULTANT']}><ProfilePage /></ProtectedRoute>} />
      </Route>
      <Route path={RoutePaths.notFound} element={<NotFoundPage />} />
    </Routes>
  </Suspense>
);

export default AppRoutes;

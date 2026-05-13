import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import LoginPage from './pages/LoginPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import ResetPasswordPage from './pages/ResetPasswordPage';
import DoctorDashboard from './pages/DoctorDashboard';
import SchedulePage from './pages/SchedulePage';
import PatientsPage from './pages/PatientsPage';
import PatientCard from './pages/PatientCard';
import ReferralsPage from './pages/ReferralsPage';
import TemplatesPage from './pages/TemplatesPage';
import ReviewsPage from './pages/ReviewsPage';
import SettingsPage from './pages/SettingsPage';
import NotificationsPage from './pages/NotificationsPage';

const PrivateRoute: React.FC<{ children: React.ReactElement }> = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <div>Загрузка...</div>
      </div>
    );
  }

  return isAuthenticated ? children : <Navigate to="/login" />;
};

function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route path="/reset-password" element={<ResetPasswordPage />} />
      <Route
        path="/doctor/dashboard"
        element={
          <PrivateRoute>
            <DoctorDashboard />
          </PrivateRoute>
        }
      />
      <Route
        path="/doctor/schedule"
        element={
          <PrivateRoute>
            <SchedulePage />
          </PrivateRoute>
        }
      />
      <Route
        path="/doctor/patients"
        element={
          <PrivateRoute>
            <PatientsPage />
          </PrivateRoute>
        }
      />
      <Route
        path="/doctor/patients/:id"
        element={
          <PrivateRoute>
            <PatientCard />
          </PrivateRoute>
        }
      />
      <Route
        path="/doctor/referrals"
        element={
          <PrivateRoute>
            <ReferralsPage />
          </PrivateRoute>
        }
      />
      <Route
        path="/doctor/templates"
        element={
          <PrivateRoute>
            <TemplatesPage />
          </PrivateRoute>
        }
      />
      <Route
        path="/doctor/reviews"
        element={
          <PrivateRoute>
            <ReviewsPage />
          </PrivateRoute>
        }
      />
      <Route
        path="/doctor/settings"
        element={
          <PrivateRoute>
            <SettingsPage />
          </PrivateRoute>
        }
      />
      <Route
        path="/doctor/notifications"
        element={
          <PrivateRoute>
            <NotificationsPage />
          </PrivateRoute>
        }
      />
      <Route path="/" element={<Navigate to="/login" />} />
    </Routes>
  );
}

function App() {
  return (
    <AuthProvider>
      <Router>
        <AppRoutes />
      </Router>
    </AuthProvider>
  );
}

export default App;

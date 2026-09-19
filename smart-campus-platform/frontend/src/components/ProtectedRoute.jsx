import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { HOME_BY_ROLE } from '../utils/constants.js';
import LoadingSpinner from './LoadingSpinner.jsx';

/**
 * Route guard. Sends anonymous visitors to the login page and users with the wrong
 * role back to their own dashboard.
 *
 * This is a convenience for the user, not the security boundary: every endpoint is
 * checked again on the server, so hiding a link is never what keeps data safe.
 */
export default function ProtectedRoute({ allowedRoles, children }) {
  const { user, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return <LoadingSpinner label="Checking your session..." />;
  }

  if (!user) {
    return <Navigate to="/login" state={{ from: location.pathname }} replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to={HOME_BY_ROLE[user.role] || '/login'} replace />;
  }

  return children;
}

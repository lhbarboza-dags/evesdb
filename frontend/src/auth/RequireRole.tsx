import { Navigate, useLocation } from 'react-router-dom';
import { Box, CircularProgress } from '@mui/material';
import type { ReactNode } from 'react';
import { useAuth } from './AuthContext';

/**
 * Route guard replacing the ZK page initiators:
 *  - SearchInit  → minRole USER (login required, else /login)
 *  - DataMgtInit → minRole CURATOR (else /)
 *  - UserMgtInit → minRole ADMIN (else /)
 */
export function RequireRole({ minRole, children }: { minRole: number; children: ReactNode }) {
  const { me, loading, hasRole } = useAuth();
  const loc = useLocation();
  if (loading || !me)
    return (
      <Box sx={{ display: 'grid', placeItems: 'center', py: 10 }}>
        <CircularProgress />
      </Box>
    );
  if (!me.authenticated) return <Navigate to="/login" replace state={{ from: loc.pathname + loc.search }} />;
  if (!hasRole(minRole)) return <Navigate to="/" replace />;
  return <>{children}</>;
}

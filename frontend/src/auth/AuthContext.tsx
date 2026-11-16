import { createContext, useCallback, useContext, useEffect, useMemo, type ReactNode } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { api, onUnauthorized } from '../api/client';
import type { Me } from '../api/types';

interface AuthState {
  me: Me | undefined;
  loading: boolean;
  hasRole: (role: number) => boolean;
  login: (email: string, password: string) => Promise<Me>;
  logout: () => Promise<void>;
}

const Ctx = createContext<AuthState | null>(null);

/** Replaces the ZK session attribute "userCredential" (+ banner.zul / index.zul 10-minute keep-alive timers). */
export function AuthProvider({ children }: { children: ReactNode }) {
  const qc = useQueryClient();
  const { data: me, isLoading } = useQuery({
    queryKey: ['me'],
    queryFn: () => api.get<Me>('/api/auth/me'),
    refetchInterval: 10 * 60 * 1000, // keep-alive, same period as timer#timerKeepAliveSession
    staleTime: 60 * 1000,
  });

  useEffect(() => {
    onUnauthorized(() => {
      qc.setQueryData<Me>(['me'], { authenticated: false, userId: null, email: null, firstName: null, lastName: null, role: null, roleName: null });
    });
  }, [qc]);

  const login = useCallback(
    async (email: string, password: string) => {
      const m = await api.post<Me>('/api/auth/login', { email, password });
      qc.clear();
      qc.setQueryData(['me'], m);
      return m;
    },
    [qc],
  );

  const logout = useCallback(async () => {
    await api.post('/api/auth/logout');
    qc.clear();
    await qc.invalidateQueries({ queryKey: ['me'] });
  }, [qc]);

  const value = useMemo<AuthState>(
    () => ({
      me,
      loading: isLoading,
      hasRole: (role: number) => !!me?.authenticated && (me.role ?? -1) >= role,
      login,
      logout,
    }),
    [me, isLoading, login, logout],
  );
  return <Ctx.Provider value={value}>{children}</Ctx.Provider>;
}

export function useAuth() {
  const c = useContext(Ctx);
  if (!c) throw new Error('useAuth outside AuthProvider');
  return c;
}

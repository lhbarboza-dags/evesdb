import { act, render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { AuthProvider, useAuth } from './AuthContext';

const anon = { authenticated: false, userId: null, email: null, firstName: null, lastName: null, role: null, roleName: null };
const user = { authenticated: true, userId: 1, email: 'a@b.org', firstName: 'A', lastName: 'B', role: 0, roleName: 'User' };

function json(body: unknown) {
  return Promise.resolve(new Response(JSON.stringify(body), { status: 200, headers: { 'Content-Type': 'application/json' } }));
}

let auth: ReturnType<typeof useAuth>;
function Probe() {
  auth = useAuth();
  return <div data-testid="state">{auth.me ? (auth.me.authenticated ? 'in' : 'out') : 'loading'}</div>;
}

describe('AuthProvider', () => {
  afterEach(() => vi.restoreAllMocks());

  it('reflects login and logout immediately (no refetch needed)', async () => {
    document.cookie = 'XSRF-TOKEN=t';
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockImplementation((input) => {
      const url = String(input);
      if (url.endsWith('/api/auth/login')) return json(user);
      if (url.endsWith('/api/auth/logout')) return Promise.resolve(new Response(null, { status: 204 }));
      return json(anon); // /api/auth/me: logged out
    });
    const qc = new QueryClient({ defaultOptions: { queries: { retry: false, refetchOnWindowFocus: false } } });
    qc.setQueryData(['search', 'x'], 'previous user data');
    render(
      <QueryClientProvider client={qc}>
        <AuthProvider>
          <Probe />
        </AuthProvider>
      </QueryClientProvider>,
    );
    await waitFor(() => expect(screen.getByTestId('state').textContent).toBe('out'));
    const meCalls = () => fetchMock.mock.calls.filter(([u]) => String(u).endsWith('/api/auth/me')).length;
    const before = meCalls();

    await act(() => auth.login('a@b.org', 'pw'));
    await waitFor(() => expect(screen.getByTestId('state').textContent).toBe('in'), { timeout: 1000 });
    expect(meCalls()).toBe(before);
    expect(qc.getQueryData(['search', 'x'])).toBeUndefined();

    await act(() => auth.logout());
    await waitFor(() => expect(screen.getByTestId('state').textContent).toBe('out'), { timeout: 1000 });
  });
});

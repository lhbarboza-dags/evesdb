import { Alert, Snackbar } from '@mui/material';
import { createContext, useCallback, useContext, useState, type ReactNode } from 'react';

type Severity = 'success' | 'info' | 'warning' | 'error';
const Ctx = createContext<(msg: string, severity?: Severity) => void>(() => {});

/** Replaces Clients.showNotification / Notification.show toasts. */
export function NotifierProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<{ msg: string; severity: Severity; key: number } | null>(null);
  const notify = useCallback((msg: string, severity: Severity = 'success') => setState({ msg, severity, key: Date.now() }), []);
  return (
    <Ctx.Provider value={notify}>
      {children}
      <Snackbar
        key={state?.key}
        open={!!state}
        autoHideDuration={state?.severity === 'error' ? 8000 : 4000}
        onClose={() => setState(null)}
        anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
      >
        {state ? (
          <Alert severity={state.severity} variant="filled" onClose={() => setState(null)} sx={{ minWidth: 280 }}>
            {state.msg}
          </Alert>
        ) : undefined}
      </Snackbar>
    </Ctx.Provider>
  );
}

export const useNotify = () => useContext(Ctx);

export function errorMessage(e: unknown): string {
  return e instanceof Error ? e.message : 'Something went wrong.';
}

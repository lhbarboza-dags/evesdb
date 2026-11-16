import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle } from '@mui/material';
import { createContext, useCallback, useContext, useRef, useState, type ReactNode } from 'react';

interface Opts {
  title?: string;
  message: string;
  confirmLabel?: string;
  danger?: boolean;
}
const Ctx = createContext<(o: Opts) => Promise<boolean>>(async () => false);

/** Replaces Messagebox.show(..., OK | CANCEL, QUESTION). Resolves true on OK. */
export function ConfirmProvider({ children }: { children: ReactNode }) {
  const [opts, setOpts] = useState<Opts | null>(null);
  const resolver = useRef<(v: boolean) => void>();
  const confirm = useCallback((o: Opts) => {
    setOpts(o);
    return new Promise<boolean>((res) => (resolver.current = res));
  }, []);
  const close = (v: boolean) => {
    resolver.current?.(v);
    setOpts(null);
  };
  return (
    <Ctx.Provider value={confirm}>
      {children}
      <Dialog open={!!opts} onClose={() => close(false)} maxWidth="xs" fullWidth>
        <DialogTitle>{opts?.title ?? 'Please confirm'}</DialogTitle>
        <DialogContent>
          <DialogContentText>{opts?.message}</DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => close(false)}>Cancel</Button>
          <Button variant="contained" color={opts?.danger ? 'error' : 'primary'} onClick={() => close(true)} autoFocus>
            {opts?.confirmLabel ?? 'OK'}
          </Button>
        </DialogActions>
      </Dialog>
    </Ctx.Provider>
  );
}

export const useConfirm = () => useContext(Ctx);

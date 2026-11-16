import { Alert, Button, CircularProgress, Stack, TextField } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { useState, type FormEvent } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { PageCard } from '../../components/AppLayout';
import { api } from '../../api/client';
import type { MessageResponse } from '../../api/types';
import { errorMessage } from '../../components/Notifier';

/** resetpassword.zul + resetpasswordContent.zul → ResetPasswordController. */
export function ResetPasswordPage() {
  const [params] = useSearchParams();
  const token = params.get('token') ?? '';
  const navigate = useNavigate();
  const check = useQuery({
    queryKey: ['reset-check', token],
    queryFn: () => api.get<MessageResponse>(`/api/auth/reset?token=${encodeURIComponent(token)}`),
    retry: false,
  });
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    if (password !== confirm) return setError('Passwords do not match.');
    setBusy(true);
    setError(null);
    try {
      await api.post('/api/auth/reset', { token, password, confirmPassword: confirm });
      navigate('/reset-password/done');
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  return (
    <PageCard title="Change Password" maxWidth={480}>
      {check.isLoading && <CircularProgress />}
      {check.data && !check.data.success && <Alert severity="error" id="messageLayout">{check.data.message}</Alert>}
      {check.data?.success && (
        <form onSubmit={submit} noValidate id="inputLayout">
          <Stack spacing={2}>
            {error && <Alert severity="error">{error}</Alert>}
            <TextField id="txtbox_password" label="New password" type="password" autoComplete="new-password" value={password} onChange={(e) => setPassword(e.target.value)} helperText="At least 6 characters." />
            <TextField id="txtbox_password2" label="Confirm password" type="password" autoComplete="new-password" value={confirm} onChange={(e) => setConfirm(e.target.value)} />
            <Stack direction="row" spacing={1}>
              <Button id="buttonSubmit" type="submit" variant="contained" disabled={busy || password.length < 6}>Change password</Button>
              <Button id="buttonReset" onClick={() => { setPassword(''); setConfirm(''); }}>Clear</Button>
            </Stack>
          </Stack>
        </form>
      )}
    </PageCard>
  );
}

import { Alert, Button, Link, Stack, TextField, Typography } from '@mui/material';
import { useState, type FormEvent } from 'react';
import { Link as RouterLink, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { PageCard } from '../../components/AppLayout';
import { useAuth } from '../../auth/AuthContext';
import { errorMessage } from '../../components/Notifier';

/** login.zul + _login.zul → LoginController (#btn_login) → AuthenticationService.login → redirect to search. */
export function LoginPage() {
  const { me, login } = useAuth();
  const navigate = useNavigate();
  const loc = useLocation();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const from = (loc.state as { from?: string } | null)?.from ?? '/search';

  if (me?.authenticated) return <Navigate to={from} replace />;

  // Enter submits the form (the ZK textboxes had no onOK).
  const submit = async (e: FormEvent) => {
    e.preventDefault();
    if (!email.trim()) return setError('Please enter your email.');
    if (!password) return setError('Please enter your password.');
    setBusy(true);
    setError(null);
    try {
      await login(email.trim(), password);
      navigate(from, { replace: true });
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  return (
    <PageCard title="Login" maxWidth={440}>
      <form onSubmit={submit} noValidate>
        <Stack spacing={2}>
          {error && <Alert severity="error">{error}</Alert>}
          <TextField id="txtboxEmail" label="Email" type="email" autoComplete="username" value={email} onChange={(e) => setEmail(e.target.value)} autoFocus />
          <TextField id="txtbox_password" label="Password" type="password" autoComplete="current-password" value={password} onChange={(e) => setPassword(e.target.value)} />
          <Button id="btn_login" type="submit" variant="contained" size="large" disabled={busy}>Login</Button>
          <Stack direction="row" justifyContent="space-between">
            <Link component={RouterLink} to="/forgot-password">Forgot Password?</Link>
            <Link component={RouterLink} to="/register">Create an Account!</Link>
          </Stack>
          <Typography variant="body2" color="text.secondary">
            You need an account to search the database. Registration is free.
          </Typography>
        </Stack>
      </form>
    </PageCard>
  );
}

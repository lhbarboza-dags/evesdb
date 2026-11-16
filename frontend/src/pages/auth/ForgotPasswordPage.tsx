import { Alert, Button, Stack, TextField } from '@mui/material';
import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { PageCard } from '../../components/AppLayout';
import { Captcha } from '../../components/Captcha';
import { api } from '../../api/client';
import { errorMessage } from '../../components/Notifier';

/** forgotPassword.zul + forgotPasswordContent.zul → forgotPasswordController. */
export function ForgotPasswordPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [captcha, setCaptcha] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      await api.post('/api/auth/forgot', { email: email.trim(), captcha });
      navigate('/forgot-password/sent');
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  };
  return (
    <PageCard title="Forgot Password" maxWidth={480}>
      <form onSubmit={submit} noValidate>
        <Stack spacing={2}>
          {error && <Alert severity="error">{error}</Alert>}
          <TextField id="txtboxEmail" label="Email" type="email" required value={email} onChange={(e) => setEmail(e.target.value)} autoFocus />
          <Captcha onChange={setCaptcha} />
          <Stack direction="row" spacing={1}>
            <Button id="buttonSubmit" type="submit" variant="contained" disabled={busy || !captcha || !email.trim()}>Send reset link</Button>
            <Button id="buttonReset" onClick={() => setEmail('')}>Clear</Button>
          </Stack>
        </Stack>
      </form>
    </PageCard>
  );
}

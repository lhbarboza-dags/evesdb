import { Alert, Button, Grid, Stack, TextField } from '@mui/material';
import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { PageCard } from '../../components/AppLayout';
import { Captcha } from '../../components/Captcha';
import { api, ApiError } from '../../api/client';
import { errorMessage } from '../../components/Notifier';
import { TermsDialog } from './TermsDialog';

const empty = { email: '', lastName: '', firstName: '', institution: '', sector: '', purpose: '', password: '', confirmPassword: '' };
const EMAIL_RE = /.+@.+\..+/;

/** register.zul (RegisterInit opens agreement.zul) + registerContent.zul → RegisterController. */
export function RegisterPage() {
  const navigate = useNavigate();
  const [agreed, setAgreed] = useState(false);
  const [form, setForm] = useState(empty);
  const [captcha, setCaptcha] = useState<string | null>(null);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const set = (k: keyof typeof empty) => (e: { target: { value: string } }) => setForm({ ...form, [k]: e.target.value });

  /** registerContent.zul textbox constraints + txtbox_password2 onChange. */
  const validate = () => {
    const e: Record<string, string> = {};
    if (!EMAIL_RE.test(form.email)) e.email = 'Please enter a valid email address.';
    if (!form.lastName.trim() || form.lastName.length > 25) e.lastName = 'Required, up to 25 characters.';
    if (!form.firstName.trim() || form.firstName.length > 25) e.firstName = 'Required, up to 25 characters.';
    if (form.password.length < 6) e.password = 'At least 6 characters.';
    if (form.password !== form.confirmPassword) e.confirmPassword = 'Passwords do not match.';
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const submit = async (ev: FormEvent) => {
    ev.preventDefault();
    if (!validate()) return;
    setBusy(true);
    setError(null);
    try {
      await api.post('/api/auth/register', { ...form, email: form.email.trim().toLowerCase(), agreedToTerms: agreed, captcha });
      navigate('/register/done');
    } catch (err) {
      if (err instanceof ApiError && err.fields) setErrors(err.fields);
      setError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  return (
    <PageCard title="Create an account" maxWidth={720}>
      <TermsDialog open={!agreed} onAgree={() => setAgreed(true)} onCancel={() => navigate('/')} />
      <form onSubmit={submit} noValidate>
        <Stack spacing={2}>
          {error && <Alert severity="error">{error}</Alert>}
          <Grid container spacing={2}>
            <Grid item xs={12}>
              <TextField id="txtbox_email" label="Email" type="email" required value={form.email} onChange={set('email')} error={!!errors.email} helperText={errors.email} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField id="txtbox_firstname" label="First name" required value={form.firstName} onChange={set('firstName')} error={!!errors.firstName} helperText={errors.firstName} inputProps={{ maxLength: 25 }} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField id="txtbox_lastname" label="Last name" required value={form.lastName} onChange={set('lastName')} error={!!errors.lastName} helperText={errors.lastName} inputProps={{ maxLength: 25 }} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField id="txtbox_institution" label="Organization" value={form.institution} onChange={set('institution')} inputProps={{ maxLength: 500 }} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField id="txtbox_sector" label="Sector" value={form.sector} onChange={set('sector')} inputProps={{ maxLength: 255 }} />
            </Grid>
            <Grid item xs={12}>
              <TextField id="txtbox_purpose" label="Purpose for using the database" multiline minRows={2} value={form.purpose} onChange={set('purpose')} inputProps={{ maxLength: 500 }} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField id="txtbox_password" label="Password" type="password" autoComplete="new-password" required value={form.password} onChange={set('password')} error={!!errors.password} helperText={errors.password} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField id="txtbox_password2" label="Confirm password" type="password" autoComplete="new-password" required value={form.confirmPassword} onChange={set('confirmPassword')}
                onBlur={() => form.confirmPassword && form.password !== form.confirmPassword && setErrors({ ...errors, confirmPassword: 'Passwords do not match.' })}
                error={!!errors.confirmPassword} helperText={errors.confirmPassword} />
            </Grid>
          </Grid>
          <Captcha onChange={setCaptcha} />
          <Stack direction="row" spacing={1}>
            <Button id="buttonSubmit" type="submit" variant="contained" disabled={busy || !captcha || !agreed}>Register</Button>
            <Button id="buttonReset" onClick={() => { setForm(empty); setErrors({}); }}>Clear</Button>
          </Stack>
        </Stack>
      </form>
    </PageCard>
  );
}

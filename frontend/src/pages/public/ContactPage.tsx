import { Button, MenuItem, Stack, TextField } from '@mui/material';
import { useState, type FormEvent } from 'react';
import { PageCard } from '../../components/AppLayout';
import { Captcha } from '../../components/Captcha';
import { errorMessage, useNotify } from '../../components/Notifier';
import { api } from '../../api/client';
import { mailCategoryLabels, type MailCategory } from '../../api/types';

const empty = { category: 'IMPROVEMENT' as MailCategory, name: '', email: '', organization: '', message: '' };

/** contactUs.zul + contactUsContent.zul → SendMailController. */
export function ContactPage() {
  const notify = useNotify();
  const [form, setForm] = useState(empty);
  const [captcha, setCaptcha] = useState<string | null>(null);
  const [captchaKey, setCaptchaKey] = useState(0);
  const [busy, setBusy] = useState(false);
  const set = (k: keyof typeof empty) => (e: { target: { value: string } }) => setForm({ ...form, [k]: e.target.value });

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setBusy(true);
    try {
      await api.post('/api/contact', { ...form, captcha });
      notify('Email sent successfully.');
      setForm(empty);
      setCaptchaKey((k) => k + 1);
    } catch (err) {
      notify(errorMessage(err), 'error');
    } finally {
      setBusy(false);
    }
  };

  return (
    <PageCard title="Contact Us" maxWidth={680}>
      <form onSubmit={submit} noValidate>
        <Stack spacing={2}>
          <TextField id="lst_purpose" select label="Subject / Type" value={form.category} onChange={set('category')}>
            {(Object.keys(mailCategoryLabels) as MailCategory[]).map((k) => (
              <MenuItem key={k} value={k}>{mailCategoryLabels[k]}</MenuItem>
            ))}
          </TextField>
          <TextField id="txtboxName" label="Name" required value={form.name} onChange={set('name')} inputProps={{ maxLength: 100 }} />
          <TextField id="txtboxEmail" label="Email" type="email" required value={form.email} onChange={set('email')} />
          <TextField id="txtboxOrganization" label="Organization" value={form.organization} onChange={set('organization')} inputProps={{ maxLength: 500 }} />
          <TextField id="txtboxMessage" label="Message" required multiline minRows={8} value={form.message} onChange={set('message')} inputProps={{ maxLength: 5000 }} />
          <Captcha key={captchaKey} onChange={setCaptcha} />
          <Stack direction="row" spacing={1}>
            <Button id="buttonSubmit" type="submit" variant="contained" disabled={busy || !captcha || !form.name || !form.email || !form.message}>
              Submit
            </Button>
            <Button id="buttonReset" onClick={() => setForm(empty)}>Clear</Button>
          </Stack>
        </Stack>
      </form>
    </PageCard>
  );
}

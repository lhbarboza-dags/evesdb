import ReCAPTCHA from 'react-google-recaptcha';
import { Alert } from '@mui/material';
import { useEffect } from 'react';
import { usePublicConfig } from '../api/hooks';

/**
 * reCAPTCHA v2 widget (div#recaptcha + afterValidate → onUserRespond). The token is now re-verified by the server on
 * submit. When reCAPTCHA is disabled in the backend config, a placeholder token is passed instead.
 */
export function Captcha({ onChange }: { onChange: (token: string | null) => void }) {
  const { data } = usePublicConfig();
  const disabled = !!data && (!data.recaptchaEnabled || !data.recaptchaSiteKey);
  useEffect(() => {
    if (disabled) onChange('disabled');
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [disabled]);
  if (!data) return null;
  if (disabled) return <Alert severity="info">reCAPTCHA is disabled in this environment.</Alert>;
  return <ReCAPTCHA sitekey={data.recaptchaSiteKey} onChange={onChange} onExpired={() => onChange(null)} />;
}

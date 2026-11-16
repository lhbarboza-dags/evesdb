import { Alert, Button, CircularProgress, Stack } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { Link as RouterLink, useSearchParams } from 'react-router-dom';
import { PageCard } from '../../components/AppLayout';
import { api } from '../../api/client';
import type { MessageResponse } from '../../api/types';

function Message({ title, text }: { title: string; text: string }) {
  return (
    <PageCard title={title}>
      <Stack spacing={2}>
        <Alert severity="success">{text}</Alert>
        <Button component={RouterLink} to="/login" variant="contained" sx={{ alignSelf: 'start' }}>Go to login</Button>
      </Stack>
    </PageCard>
  );
}

/** registerdone.zul + registerdoneContent.zul */
export const RegisterDonePage = () => (
  <Message title="Register" text="Email sent successfully. Please check your inbox and click the verification link to activate your account." />
);

/** message.zul + messageContent.zul */
export const ForgotSentPage = () => (
  <Message title="Forgot Password" text="Email sent successfully. Please check your inbox for the link to change your password." />
);

/** update.zul + updateContent.zul */
export const ResetDonePage = () => <Message title="Change Password" text="Your password has been successfully changed!" />;

function TokenPage({ title, url }: { title: string; url: string }) {
  const [params] = useSearchParams();
  const token = params.get('token') ?? '';
  const { data, isLoading, error } = useQuery({
    queryKey: [url, token],
    queryFn: () => api.get<MessageResponse>(`${url}?token=${encodeURIComponent(token)}`),
    retry: false,
    staleTime: Infinity,
  });
  return (
    <PageCard title={title}>
      {isLoading && <CircularProgress />}
      {error && <Alert severity="error">{(error as Error).message}</Alert>}
      {data && (
        <Stack spacing={2}>
          <Alert severity={data.success ? 'success' : 'error'}>{data.message}</Alert>
          {data.success && <Button component={RouterLink} to="/login" variant="contained" sx={{ alignSelf: 'start' }}>Go to login</Button>}
        </Stack>
      )}
    </PageCard>
  );
}

/** validate.zul + validateContent.zul → ValidateController (link from the registration e-mail). */
export const VerifyEmailPage = () => <TokenPage title="Email validation" url="/api/auth/verify" />;

/** validateEmail.zul + validateEmailContent.zul → ValidateEmailController (Contact-Us recipient invitation). */
export const InvitationPage = () => <TokenPage title="Email validation" url="/api/auth/invitation" />;

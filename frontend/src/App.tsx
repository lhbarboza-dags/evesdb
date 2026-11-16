import { lazy, Suspense, type ReactNode } from 'react';
import { Box, CircularProgress } from '@mui/material';
import { Navigate, Route, Routes } from 'react-router-dom';
import { AppLayout } from './components/AppLayout';
import { RequireRole } from './auth/RequireRole';
import { Role } from './api/types';
import { LandingPage } from './pages/public/LandingPage';
import { AboutPage } from './pages/public/AboutPage';
import { ManualPage } from './pages/public/ManualPage';
import { ContactPage } from './pages/public/ContactPage';
import { LoginPage } from './pages/auth/LoginPage';
import { RegisterPage } from './pages/auth/RegisterPage';
import { ForgotPasswordPage } from './pages/auth/ForgotPasswordPage';
import { ResetPasswordPage } from './pages/auth/ResetPasswordPage';
import { ForgotSentPage, InvitationPage, RegisterDonePage, ResetDonePage, VerifyEmailPage } from './pages/auth/MessagePages';
const SearchPage = lazy(() => import('./pages/search/SearchPage').then((m) => ({ default: m.SearchPage })));
const StudiesPage = lazy(() => import('./pages/studies/StudiesPage').then((m) => ({ default: m.StudiesPage })));
const NewStudyPage = lazy(() => import('./pages/studies/NewStudyPage').then((m) => ({ default: m.NewStudyPage })));
const DictionaryPage = lazy(() => import('./pages/dictionary/DictionaryPage').then((m) => ({ default: m.DictionaryPage })));
const UsersPage = lazy(() => import('./pages/admin/UsersPage').then((m) => ({ default: m.UsersPage })));

/** Route table. Legacy *.zul URLs redirect to their React route so old bookmarks and e-mailed links keep working. */
export function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route index element={<LandingPage />} />
        <Route path="about" element={<AboutPage />} />
        <Route path="manual" element={<ManualPage />} />
        <Route path="contact" element={<ContactPage />} />
        <Route path="login" element={<LoginPage />} />
        <Route path="register" element={<RegisterPage />} />
        <Route path="register/done" element={<RegisterDonePage />} />
        <Route path="verify-email" element={<VerifyEmailPage />} />
        <Route path="forgot-password" element={<ForgotPasswordPage />} />
        <Route path="forgot-password/sent" element={<ForgotSentPage />} />
        <Route path="reset-password" element={<ResetPasswordPage />} />
        <Route path="reset-password/done" element={<ResetDonePage />} />
        <Route path="invitation" element={<InvitationPage />} />
        <Route path="search" element={<Guard min={Role.USER}><SearchPage /></Guard>} />
        <Route path="studies" element={<Guard min={Role.CURATOR}><StudiesPage /></Guard>} />
        <Route path="studies/new" element={<Guard min={Role.CURATOR}><NewStudyPage /></Guard>} />
        <Route path="dictionary" element={<Guard min={Role.MANAGER}><DictionaryPage /></Guard>} />
        <Route path="admin/users" element={<Guard min={Role.ADMIN}><UsersPage /></Guard>} />
        {LEGACY.map(([from, to]) => <Route key={from} path={from} element={<LegacyRedirect to={to} />} />)}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}

/** Old ZK URLs → new routes (query string, e.g. ?token=, is kept). */
const LEGACY: [string, string][] = [
  ['index.zul', '/'], ['purpose.zul', '/about'], ['doc.zul', '/manual'], ['contactUs.zul', '/contact'], ['login.zul', '/login'],
  ['register.zul', '/register'], ['registerdone.zul', '/register/done'], ['validate.zul', '/verify-email'], ['forgotPassword.zul', '/forgot-password'],
  ['message.zul', '/forgot-password/sent'], ['resetpassword.zul', '/reset-password'], ['update.zul', '/reset-password/done'],
  ['validateEmail.zul', '/invitation'], ['search.zul', '/search'], ['study_.zul', '/studies'], ['upload.zul', '/studies/new'],
  ['dictionary.zul', '/dictionary'], ['usermgt.zul', '/admin/users'],
];

function LegacyRedirect({ to }: { to: string }) {
  return <Navigate to={to + window.location.search} replace />;
}

/** Role guard + lazy-loaded page. */
function Guard({ min, children }: { min: number; children: ReactNode }) {
  return (
    <RequireRole minRole={min}>
      <Suspense fallback={<Box sx={{ display: 'grid', placeItems: 'center', py: 10 }}><CircularProgress /></Box>}>{children}</Suspense>
    </RequireRole>
  );
}

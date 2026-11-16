import { AppBar, Box, Button, Container, Divider, IconButton, Link, ListItemIcon, Menu, MenuItem, Toolbar, Typography, useMediaQuery } from '@mui/material';
import { useTheme } from '@mui/material/styles';
import LogoutIcon from '@mui/icons-material/Logout';
import MenuIcon from '@mui/icons-material/Menu';
import AccountCircle from '@mui/icons-material/AccountCircle';
import ExpandMore from '@mui/icons-material/ExpandMore';
import { useState, type MouseEvent, type ReactNode } from 'react';
import { Link as RouterLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { Role } from '../api/types';

interface NavItem {
  label: string;
  to?: string;
  href?: string;
  children?: NavItem[];
  show?: boolean;
}

/** banner.zul (+ BannerController role rules) and footer.zul. */
export function AppLayout() {
  const { me, hasRole, logout } = useAuth();
  const navigate = useNavigate();
  const theme = useTheme();
  const narrow = useMediaQuery(theme.breakpoints.down('md'));
  const [anchor, setAnchor] = useState<{ el: HTMLElement; key: string } | null>(null);
  const loggedIn = !!me?.authenticated;

  const items: NavItem[] = [
    { label: 'Home', to: '/' },
    { label: 'Database', to: '/search' },
    {
      label: 'Data Management',
      show: hasRole(Role.CURATOR),
      children: [
        { label: 'Add Study', to: '/studies/new' },
        { label: 'View/Edit Study', to: '/studies' },
        { label: 'Dictionary', to: '/dictionary', show: hasRole(Role.MANAGER) },
        { label: 'Users', to: '/admin/users', show: hasRole(Role.ADMIN) },
      ],
    },
    { label: 'About SIBOL-EVES', to: '/about' },
    {
      label: 'User Manual',
      children: [
        { label: 'User Manual (web)', to: '/manual' },
        { label: 'PDF Download', href: '/manual/SIBOL-EVES Database Manual.pdf' },
      ],
    },
    { label: 'Contact Us', to: '/contact' },
    { label: 'Register/Login', to: '/login', show: !loggedIn },
  ].filter((i) => i.show !== false);

  const open = (e: MouseEvent<HTMLElement>, key: string) => setAnchor({ el: e.currentTarget, key });
  const close = () => setAnchor(null);
  const go = (i: NavItem) => {
    close();
    if (i.href) window.open(i.href, '_blank', 'noopener');
    else if (i.to) navigate(i.to);
  };

  const doLogout = async () => {
    close();
    await logout();
    navigate('/');
  };

  const menuFor = (key: string, children: ReactNode) => (
    <Menu anchorEl={anchor?.key === key ? anchor.el : null} open={anchor?.key === key} onClose={close}>
      {children}
    </Menu>
  );

  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <AppBar
        position="sticky"
        color="primary"
        elevation={0}
        sx={{
          // frosted glass: semi-transparent forest green + backdrop blur, readable over the landing hero and plain pages
          bgcolor: 'rgba(18, 63, 59, 0.78)',
          backdropFilter: 'blur(12px) saturate(140%)',
          WebkitBackdropFilter: 'blur(12px) saturate(140%)',
          borderBottom: '1px solid rgba(255, 255, 255, 0.12)',
        }}
      >
        <Toolbar sx={{ gap: 1 }}>
          <Box id="navbar-brand" component={RouterLink} to="/" sx={{ display: 'flex', alignItems: 'center', gap: 1.5, color: 'inherit', textDecoration: 'none', mr: 2 }}>
            <Typography variant="h6" component="span" sx={{ fontFamily: '"Source Serif 4", Georgia, serif', letterSpacing: 0.3 }}>
              SIBOL-EVES
            </Typography>
          </Box>
          {narrow ? (
            <>
              <Box sx={{ flex: 1 }} />
              <IconButton color="inherit" aria-label="Open menu" onClick={(e) => open(e, 'mobile')}>
                <MenuIcon />
              </IconButton>
              {menuFor(
                'mobile',
                items.flatMap((i) =>
                  i.children
                    ? [
                        <MenuItem key={i.label} disabled sx={{ opacity: '1 !important', fontWeight: 700 }}>{i.label}</MenuItem>,
                        ...i.children.filter((c) => c.show !== false).map((c) => (
                          <MenuItem key={c.label} sx={{ pl: 4 }} onClick={() => go(c)}>{c.label}</MenuItem>
                        )),
                      ]
                    : [<MenuItem key={i.label} onClick={() => go(i)}>{i.label}</MenuItem>],
                ),
              )}
            </>
          ) : (
            <Box component="nav" sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5, flex: 1, justifyContent: 'flex-end', alignItems: 'center' }}>
              {items.map((i) =>
                i.children ? (
                  <Box key={i.label}>
                    <Button color="inherit" endIcon={<ExpandMore />} onClick={(e) => open(e, i.label)} aria-haspopup="menu">
                      {i.label}
                    </Button>
                    {menuFor(
                      i.label,
                      i.children.filter((c) => c.show !== false).map((c) => (
                        <MenuItem key={c.label} onClick={() => go(c)}>{c.label}</MenuItem>
                      )),
                    )}
                  </Box>
                ) : (
                  <Button key={i.label} color="inherit" onClick={() => go(i)}>{i.label}</Button>
                ),
              )}
            </Box>
          )}
          {loggedIn && (
            <>
              <Button color="inherit" startIcon={<AccountCircle />} endIcon={<ExpandMore />} onClick={(e) => open(e, 'user')} sx={{ maxWidth: 280, ml: 'auto', flexShrink: 0, minWidth: 0 }} aria-label={`Account menu for ${me?.email}`}>
                {/* e-mail text hidden on phones so the bar never overflows; the account icon stays */}
                <Box component="span" sx={{ display: { xs: 'none', sm: 'inline' }, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{me?.email}</Box>
              </Button>
              {menuFor(
                'user',
                [
                  <MenuItem key="role" disabled>{me?.firstName} {me?.lastName} · {me?.roleName}</MenuItem>,
                  <Divider key="d" />,
                  <MenuItem key="logout" onClick={doLogout}>
                    <ListItemIcon><LogoutIcon fontSize="small" /></ListItemIcon>
                    Logout
                  </MenuItem>,
                ],
              )}
            </>
          )}
        </Toolbar>
      </AppBar>
      <Box component="main" sx={{ flex: 1 }}>
        <Outlet />
      </Box>
      <Box component="footer" sx={{ bgcolor: '#14211f', color: '#c7d3d1', py: 2 }}>
        <Container maxWidth="lg" sx={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'space-between', gap: 2, fontSize: 14 }}>
          <span>© 2022 SIBOL EVES - ALL RIGHTS RESERVED</span>
          <span>
            <Link component={RouterLink} to="/about" color="inherit">About</Link>
            {' · '}
            <Link component={RouterLink} to="/contact" color="inherit">Contact</Link>
          </span>
        </Container>
      </Box>
    </Box>
  );
}

/** Centered card used by the small ZK windows (login, register, messages). */
export function PageCard({ title, children, maxWidth = 560 }: { title: string; children: ReactNode; maxWidth?: number }) {
  return (
    <Container sx={{ py: { xs: 3, md: 6 } }}>
      <Box sx={{ maxWidth, mx: 'auto', bgcolor: 'background.paper', border: 1, borderColor: 'divider', borderRadius: 2, p: { xs: 2.5, md: 4 } }}>
        <Typography variant="h4" component="h1" gutterBottom>
          {title}
        </Typography>
        {children}
      </Box>
    </Container>
  );
}

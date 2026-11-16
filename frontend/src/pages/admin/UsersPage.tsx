import {
  Autocomplete, Box, Button, Checkbox, Chip, CircularProgress, Container, MenuItem, Paper, Stack, Tab, Table, TableBody, TableCell,
  TableHead, TablePagination, TableRow, Tabs, TextField, Tooltip, Typography,
} from '@mui/material';
import InfoOutlined from '@mui/icons-material/InfoOutlined';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useEffect, useMemo, useState, type FormEvent } from 'react';
import { api } from '../../api/client';
import { mailCategoryLabels, roleNames, type MailCategory, type Recipient, type UserRow } from '../../api/types';
import { useAuth } from '../../auth/AuthContext';
import { useConfirm } from '../../components/ConfirmDialog';
import { errorMessage, useNotify } from '../../components/Notifier';

/** usermgt.zul (UserMgtInit: Admin) + userMgtContent.zul → UserMgtController: "Users" and "Email Config" tabs. */
export function UsersPage() {
  const [tab, setTab] = useState(0);
  return (
    <Container maxWidth="xl" sx={{ py: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>Users</Typography>
      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ borderBottom: 1, borderColor: 'divider', mb: 2 }}>
        <Tab label="Users" />
        <Tab label="Email Config" />
      </Tabs>
      {tab === 0 ? <UsersTab /> : <EmailConfigTab />}
    </Container>
  );
}

function UsersTab() {
  const { me } = useAuth();
  const notify = useNotify();
  const confirm = useConfirm();
  const qc = useQueryClient();
  const [role, setRole] = useState<string>('all');
  const [text, setText] = useState('');
  const [q, setQ] = useState('');
  const [selected, setSelected] = useState<Set<number>>(new Set());
  const [page, setPage] = useState(0);
  const { data, isLoading } = useQuery({
    queryKey: ['users', role],
    queryFn: () => api.get<UserRow[]>(`/api/users${role === 'all' ? '' : `?role=${role}`}`),
  });
  const rows = useMemo(
    () => (data ?? []).filter((u) => !q || [u.email, u.firstName, u.lastName, u.institution].some((x) => (x ?? '').toLowerCase().includes(q))),
    [data, q],
  );
  const pageRows = rows.slice(page * 15, page * 15 + 15);

  /** btn_user / btn_curator / btn_Manager / btn_admin → setUserLevel(n). */
  const setLevel = async (level: number) => {
    if (level === 3 && !(await confirm({ message: `Give Admin rights to ${selected.size} user(s)? Admins can manage all users.` }))) return;
    try {
      await api.post('/api/users/role', { ids: [...selected], role: level });
      notify(`${selected.size} user(s) set to ${roleNames[level]}.`);
      setSelected(new Set());
      qc.invalidateQueries({ queryKey: ['users'] });
    } catch (e) {
      notify(errorMessage(e), 'error');
    }
  };
  const toggle = (id: number) => {
    const s = new Set(selected);
    if (s.has(id)) s.delete(id);
    else s.add(id);
    setSelected(s);
  };
  const allOnPage = pageRows.length > 0 && pageRows.every((u) => selected.has(u.id));

  return (
    <>
      <Stack direction={{ xs: 'column', md: 'row' }} gap={2} alignItems={{ md: 'center' }} sx={{ mb: 2 }}>
        <Box component="form" onSubmit={(e: FormEvent) => { e.preventDefault(); setQ(text.trim().toLowerCase()); setPage(0); }} sx={{ display: 'flex', gap: 1, flex: 1, maxWidth: 480 }}>
          <TextField id="txtBox_searchUser" label="Search name, email or organization" value={text} onChange={(e) => setText(e.target.value)} />
          <Button id="btn_searchUser" type="submit" variant="outlined">Search</Button>
        </Box>
        <TextField id="combobox_user" select label="Role" value={role} sx={{ width: { md: 180 } }} onChange={(e) => { setRole(e.target.value); setSelected(new Set()); setPage(0); }}>
          <MenuItem value="all">All</MenuItem>
          {roleNames.map((r, i) => <MenuItem key={r} value={String(i)}>{r}</MenuItem>)}
        </TextField>
        {selected.size > 0 && (
          <Stack id="hbox_User" direction="row" gap={1} flexWrap="wrap" alignItems="center">
            <Typography variant="body2">Set role:</Typography>
            <Button id="btn_user" variant="outlined" onClick={() => setLevel(0)}>User</Button>
            <Button id="btn_curator" variant="outlined" onClick={() => setLevel(1)}>Curator</Button>
            <Button id="btn_Manager" variant="outlined" onClick={() => setLevel(2)}>Manager</Button>
            <Button id="btn_admin" variant="outlined" color="secondary" onClick={() => setLevel(3)}>Admin</Button>
          </Stack>
        )}
      </Stack>
      <Paper variant="outlined" sx={{ overflowX: 'auto' }}>
        <Table size="small" id="grid_user">
          <TableHead>
            <TableRow>
              <TableCell padding="checkbox">
                <Checkbox id="checkbox_header" checked={allOnPage} indeterminate={!allOnPage && pageRows.some((u) => selected.has(u.id))} inputProps={{ 'aria-label': 'Select all on this page' }}
                  onChange={(e) => { const s = new Set(selected); pageRows.forEach((u) => (e.target.checked ? s.add(u.id) : s.delete(u.id))); setSelected(s); }} />
              </TableCell>
              <TableCell>Email</TableCell><TableCell>Last name</TableCell><TableCell>First name</TableCell><TableCell>Organization</TableCell><TableCell>Role</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {isLoading && <TableRow><TableCell colSpan={6}><CircularProgress size={22} /></TableCell></TableRow>}
            {pageRows.map((u) => (
              <TableRow key={u.id} hover selected={selected.has(u.id)}>
                <TableCell padding="checkbox"><Checkbox checked={selected.has(u.id)} onChange={() => toggle(u.id)} disabled={u.id === me?.userId} inputProps={{ 'aria-label': `Select ${u.email}` }} /></TableCell>
                <TableCell>{u.email}</TableCell><TableCell>{u.lastName}</TableCell><TableCell>{u.firstName}</TableCell><TableCell>{u.institution}</TableCell>
                <TableCell><Chip size="small" label={u.roleName} variant="outlined" color={u.role >= 2 ? 'primary' : 'default'} /></TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        <TablePagination component="div" count={rows.length} page={page} rowsPerPage={15} rowsPerPageOptions={[15]} onPageChange={(_, p) => setPage(p)} />
      </Paper>
    </>
  );
}

const HELP: Record<MailCategory, string> = {
  IMPROVEMENT: 'Receives "Suggest Improvements" messages from the Contact Us page.',
  INQUIRY: 'Receives "Inquiry" messages from the Contact Us page.',
  SUGGEST_A_STUDY: 'Receives "Suggest a study" messages from the Contact Us page.',
  BUG_REPORT: 'Receives "Bug Report" messages from the Contact Us page.',
};
const HELP_IDS: Record<MailCategory, string> = { IMPROVEMENT: 'btn_I', INQUIRY: 'btn_Si', SUGGEST_A_STUDY: 'btn_sAs', BUG_REPORT: 'btn_bR' };
const CHOSEN_IDS: Record<MailCategory, string> = { IMPROVEMENT: 'chosen_Improvement', INQUIRY: 'chosen_inquiry', SUGGEST_A_STUDY: 'chosen_suggestStudy', BUG_REPORT: 'chosen_bugreport' };

/** "Email Config" tab: who receives Contact-Us mails per category. New recipients get an invitation e-mail. */
function EmailConfigTab() {
  const notify = useNotify();
  const qc = useQueryClient();
  const { data } = useQuery({ queryKey: ['email-config'], queryFn: () => api.get<Recipient[]>('/api/users/email-config') });
  const [sel, setSel] = useState<Record<MailCategory, string[]>>({ IMPROVEMENT: [], INQUIRY: [], BUG_REPORT: [], SUGGEST_A_STUDY: [] });
  const [busy, setBusy] = useState(false);
  useEffect(() => {
    if (!data) return;
    const next = { IMPROVEMENT: [], INQUIRY: [], BUG_REPORT: [], SUGGEST_A_STUDY: [] } as Record<MailCategory, string[]>;
    for (const r of data) for (const c of Object.keys(next) as MailCategory[]) if (r.categories[c] !== 'NONE') next[c].push(r.email);
    setSel(next);
  }, [data]);
  const emails = (data ?? []).map((r) => r.email);
  const state = (email: string, c: MailCategory) => data?.find((r) => r.email === email)?.categories[c] ?? 'NONE';

  /** buttonSubmit "Save". */
  const save = async () => {
    setBusy(true);
    try {
      await api.put('/api/users/email-config', { selected: sel });
      notify('Saved. New recipients were sent an invitation e-mail.');
      qc.invalidateQueries({ queryKey: ['email-config'] });
    } catch (e) {
      notify(errorMessage(e), 'error');
    } finally {
      setBusy(false);
    }
  };

  return (
    <Stack spacing={3} sx={{ maxWidth: 900 }}>
      {(Object.keys(mailCategoryLabels) as MailCategory[]).map((c) => (
        <Stack key={c} direction="row" alignItems="flex-start" spacing={1}>
          <Autocomplete id={CHOSEN_IDS[c]} sx={{ flex: 1 }} multiple options={emails} value={sel[c]} onChange={(_, v) => setSel({ ...sel, [c]: v })}
            renderTags={(v, getTagProps) => v.map((e, i) => {
              const st = state(e, c);
              return <Chip {...getTagProps({ index: i })} key={e} size="small" label={st === 'INVITED' ? `${e} (invited)` : st === 'NONE' ? `${e} (new)` : e} color={st === 'ACCEPTED' ? 'success' : 'default'} />;
            })}
            renderInput={(p) => <TextField {...p} label={mailCategoryLabels[c]} />} />
          <Tooltip title={HELP[c]}><Box id={HELP_IDS[c]} sx={{ pt: 1 }}><InfoOutlined color="action" /></Box></Tooltip>
        </Stack>
      ))}
      <Typography variant="body2" color="text.secondary">Green = invitation accepted. Removing someone stops their mails immediately.</Typography>
      <Box><Button id="buttonSubmit" variant="contained" onClick={save} disabled={busy || !data}>Save</Button></Box>
    </Stack>
  );
}

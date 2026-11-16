import {
  Box, Button, Checkbox, Chip, CircularProgress, Container, IconButton, MenuItem, Paper, Stack, Table, TableBody, TableCell, TableContainer,
  TableHead, TablePagination, TableRow, TableSortLabel, TextField, Tooltip, Typography,
} from '@mui/material';
import Edit from '@mui/icons-material/Edit';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useMemo, useState, type FormEvent } from 'react';
import { api } from '../../api/client';
import { Role, StudyStatus, type StudyListRow } from '../../api/types';
import { useAuth } from '../../auth/AuthContext';
import { useConfirm } from '../../components/ConfirmDialog';
import { errorMessage, useNotify } from '../../components/Notifier';
import { StudyEditDialog } from './StudyEditDialog';

const statusColor = { 0: 'warning', 1: 'success', 2: 'error' } as const;
type SortKey = 'title' | 'codedDate';

/** study_.zul (DataMgtInit) + study_view.zul → ViewStudyController + ViewStudyRenderer. */
export function StudiesPage() {
  const { hasRole } = useAuth();
  const confirm = useConfirm();
  const notify = useNotify();
  const qc = useQueryClient();
  const [status, setStatus] = useState<string>('all');
  const [text, setText] = useState('');
  const [q, setQ] = useState('');
  const [selected, setSelected] = useState<Set<number>>(new Set());
  const [page, setPage] = useState(0);
  const [sort, setSort] = useState<{ key: SortKey; asc: boolean }>({ key: 'title', asc: true });
  const [editId, setEditId] = useState<number | null>(null);
  const manager = hasRole(Role.MANAGER);

  const { data, isLoading } = useQuery({
    queryKey: ['studies', status, q],
    queryFn: () => api.get<StudyListRow[]>(`/api/studies?${status === 'all' ? '' : `status=${status}&`}q=${encodeURIComponent(q)}`),
  });

  const rows = useMemo(() => {
    const r = [...(data ?? [])];
    r.sort((a, b) => (sort.asc ? 1 : -1) * String(a[sort.key] ?? '').localeCompare(String(b[sort.key] ?? ''), undefined, { sensitivity: 'base' }));
    return r;
  }, [data, sort]);
  const pageRows = rows.slice(page * 10, page * 10 + 10);

  const refresh = async () => {
    setSelected(new Set());
    await qc.invalidateQueries({ queryKey: ['studies'] });
  };

  /** btn_searchStudy (Enter now submits too). */
  const search = (e: FormEvent) => {
    e.preventDefault();
    setPage(0);
    setQ(text.trim());
  };

  /** btn_approve / btn_pending / btn_disapprove → studyApproval(n). Selection is by id, not row index. */
  const setStudyStatus = async (st: number) => {
    try {
      await api.post('/api/studies/status', { ids: [...selected], status: st });
      notify(`${selected.size} stud${selected.size === 1 ? 'y' : 'ies'} updated.`);
      await refresh();
    } catch (e) {
      notify(errorMessage(e), 'error');
    }
  };

  /** btn_delete → Messagebox → soft delete. */
  const remove = async () => {
    if (!(await confirm({ message: 'You are about to delete a record. Do you wish to continue?', danger: true, confirmLabel: 'Delete' }))) return;
    try {
      await api.post('/api/studies/delete', { ids: [...selected] });
      notify('Deleted.');
      await refresh();
    } catch (e) {
      notify(errorMessage(e), 'error');
    }
  };

  /** Row pencil: approved studies cannot be edited. */
  const edit = (r: StudyListRow) => {
    if (r.status === StudyStatus.APPROVED) notify('This study is already approved and so, it cannot be edited.', 'warning');
    else setEditId(r.id);
  };

  const toggle = (id: number) => {
    const s = new Set(selected);
    if (s.has(id)) s.delete(id);
    else s.add(id);
    setSelected(s);
  };
  const allOnPage = pageRows.length > 0 && pageRows.every((r) => selected.has(r.id));
  const sortBy = (key: SortKey) => setSort((s) => ({ key, asc: s.key === key ? !s.asc : true }));

  return (
    <Container maxWidth="xl" sx={{ py: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>View / Edit Study</Typography>
      <Stack direction={{ xs: 'column', md: 'row' }} gap={2} alignItems={{ md: 'center' }} sx={{ mb: 2 }}>
        <TextField id="combobox_approval" select label="Status" value={status} sx={{ width: { md: 200 } }}
          onChange={(e) => { setStatus(e.target.value); setPage(0); setSelected(new Set()); }}>
          <MenuItem value="all">All</MenuItem>
          <MenuItem value="1">Approved</MenuItem>
          <MenuItem value="0">Pending</MenuItem>
          <MenuItem value="2">Disapproved</MenuItem>
        </TextField>
        <Box component="form" onSubmit={search} sx={{ display: 'flex', gap: 1, flex: 1, maxWidth: 560 }}>
          <TextField id="txtBox_searchStudy" label="Title, year, country, ecosystem or service" value={text} onChange={(e) => setText(e.target.value)} />
          <Button id="btn_searchStudy" type="submit" variant="outlined">Search</Button>
        </Box>
        {manager && selected.size > 0 && (
          <Stack id="hbox_approvalBtns" direction="row" flexWrap="wrap" gap={1}>
            <Button id="btn_approve" variant="contained" color="success" onClick={() => setStudyStatus(1)}>Approve</Button>
            <Button id="btn_pending" variant="outlined" onClick={() => setStudyStatus(0)}>Pending</Button>
            <Button id="btn_disapprove" variant="outlined" color="warning" onClick={() => setStudyStatus(2)}>Disapprove</Button>
            <Button id="btn_delete" variant="outlined" color="error" onClick={remove}>Delete</Button>
          </Stack>
        )}
      </Stack>
      <Paper variant="outlined">
        <TableContainer>
          <Table size="small" id="grid_study">
            <TableHead>
              <TableRow>
                <TableCell padding="checkbox">
                  {manager && (
                    <Checkbox id="checkbox_header" checked={allOnPage} indeterminate={!allOnPage && pageRows.some((r) => selected.has(r.id))}
                      onChange={(e) => {
                        const s = new Set(selected);
                        pageRows.forEach((r) => (e.target.checked ? s.add(r.id) : s.delete(r.id)));
                        setSelected(s);
                      }} inputProps={{ 'aria-label': 'Select all on this page' }} />
                  )}
                </TableCell>
                <TableCell>Edit</TableCell>
                <TableCell>Year</TableCell>
                <TableCell sortDirection={sort.key === 'title' ? (sort.asc ? 'asc' : 'desc') : false}>
                  <TableSortLabel active={sort.key === 'title'} direction={sort.asc ? 'asc' : 'desc'} onClick={() => sortBy('title')}>Title</TableSortLabel>
                </TableCell>
                <TableCell>Submitted By</TableCell>
                <TableCell>
                  <TableSortLabel active={sort.key === 'codedDate'} direction={sort.asc ? 'asc' : 'desc'} onClick={() => sortBy('codedDate')}>Date</TableSortLabel>
                </TableCell>
                <TableCell>Reviewed By</TableCell>
                <TableCell>Date</TableCell>
                <TableCell>Status</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {isLoading && <TableRow><TableCell colSpan={9}><CircularProgress size={24} /></TableCell></TableRow>}
              {pageRows.map((r) => (
                <TableRow key={r.id} hover selected={selected.has(r.id)}>
                  <TableCell padding="checkbox">
                    {manager && <Checkbox checked={selected.has(r.id)} onChange={() => toggle(r.id)} inputProps={{ 'aria-label': `Select ${r.title}` }} />}
                  </TableCell>
                  <TableCell padding="checkbox">
                    <Tooltip title={`Edit ${r.title}`}><IconButton size="small" onClick={() => edit(r)} aria-label={`Edit ${r.title}`}><Edit fontSize="small" /></IconButton></Tooltip>
                  </TableCell>
                  <TableCell>{r.pubYear ?? ''}</TableCell>
                  <TableCell sx={{ minWidth: 280 }}>{r.title}</TableCell>
                  <TableCell>{r.submittedBy}</TableCell>
                  <TableCell sx={{ whiteSpace: 'nowrap' }}>{fmt(r.codedDate)}</TableCell>
                  <TableCell>{r.reviewedBy}</TableCell>
                  <TableCell sx={{ whiteSpace: 'nowrap' }}>{fmt(r.reviewedDate)}</TableCell>
                  <TableCell><Chip size="small" label={r.statusName} color={statusColor[r.status as 0 | 1 | 2] ?? 'warning'} variant="outlined" /></TableCell>
                </TableRow>
              ))}
              {!isLoading && !rows.length && <TableRow><TableCell colSpan={9}>No studies found.</TableCell></TableRow>}
            </TableBody>
          </Table>
        </TableContainer>
        <TablePagination component="div" count={rows.length} page={page} rowsPerPage={10} rowsPerPageOptions={[10]} onPageChange={(_, p) => setPage(p)} />
      </Paper>
      <StudyEditDialog id={editId} onClose={async (saved) => {
        setEditId(null);
        if (saved) {
          notify('Study saved.');
          await qc.invalidateQueries({ queryKey: ['study'] });
          await refresh();
        }
      }} />
    </Container>
  );
}

/** codeddate/revieweddate are ISO strings from LocalDateTime.toString(). */
function fmt(d: string | null) {
  if (!d) return '';
  const t = Date.parse(d);
  return Number.isNaN(t) ? d : new Date(t).toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' });
}

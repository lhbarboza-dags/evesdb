import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Grid, IconButton, Table, TableBody, TableCell, TableHead, TableRow, TextField } from '@mui/material';
import Delete from '@mui/icons-material/DeleteOutline';
import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import type { ProtectedAreaDto } from '../../api/types';
import { errorMessage, useNotify } from '../../components/Notifier';

const empty = { name: '', area: '', established: '', location: '' };
type PaForm = typeof empty;

function PaFields({ f, set, error }: { f: PaForm; set: (f: PaForm) => void; error?: string }) {
  return (
    <Grid container spacing={2}>
      <Grid item xs={12} sm={6}><TextField id="txtbox_name" label="Name" required value={f.name} onChange={(e) => set({ ...f, name: e.target.value })} error={!!error} helperText={error} inputProps={{ maxLength: 55 }} /></Grid>
      <Grid item xs={12} sm={6}><TextField id="txtbox_area" label="Area (Ha)" value={f.area} onChange={(e) => set({ ...f, area: e.target.value })} inputProps={{ maxLength: 55 }} /></Grid>
      <Grid item xs={12} sm={6}><TextField id="txtbox_established" label="Established" value={f.established} onChange={(e) => set({ ...f, established: e.target.value })} inputProps={{ maxLength: 55 }} /></Grid>
      <Grid item xs={12} sm={6}><TextField id="txtbox_location" label="Location" value={f.location} onChange={(e) => set({ ...f, location: e.target.value })} inputProps={{ maxLength: 55 }} /></Grid>
    </Grid>
  );
}

/** AddPADialog.zul → PAController: btn_addPA (stage, duplicate name check), × (unstage), buttonSave, cancelButton. */
export function ProtectedAreaAddDialog({ open, onClose }: { open: boolean; onClose: (added: ProtectedAreaDto[] | null) => void }) {
  const notify = useNotify();
  const [f, setF] = useState(empty);
  const [staged, setStaged] = useState<PaForm[]>([]);
  const [err, setErr] = useState<string | undefined>();
  const [busy, setBusy] = useState(false);
  useEffect(() => {
    if (open) {
      setF(empty);
      setStaged([]);
      setErr(undefined);
    }
  }, [open]);

  const add = () => {
    if (!f.name.trim()) return setErr('Name is required.');
    if (staged.some((s) => s.name.trim().toLowerCase() === f.name.trim().toLowerCase())) return setErr('Record already exists.');
    setStaged([...staged, f]);
    setF(empty);
    setErr(undefined);
  };
  const save = async () => {
    setBusy(true);
    try {
      onClose(await api.post<ProtectedAreaDto[]>('/api/dictionary/protected-areas', { items: staged }));
    } catch (e) {
      notify(errorMessage(e), 'error');
    } finally {
      setBusy(false);
    }
  };
  return (
    <Dialog open={open} onClose={() => onClose(null)} maxWidth="md" fullWidth>
      <DialogTitle>Add Protected Area</DialogTitle>
      <DialogContent dividers>
        <PaFields f={f} set={setF} error={err} />
        <Button id="btn_addPA" sx={{ mt: 2 }} variant="outlined" onClick={add}>Add to list</Button>
        {staged.length > 0 && (
          <Table size="small" sx={{ mt: 2 }}>
            <TableHead><TableRow><TableCell /><TableCell>Name</TableCell><TableCell>Area</TableCell><TableCell>Established</TableCell><TableCell>Location</TableCell></TableRow></TableHead>
            <TableBody>
              {staged.map((s, i) => (
                <TableRow key={s.name}>
                  <TableCell padding="checkbox"><IconButton size="small" aria-label="Remove" onClick={() => setStaged(staged.filter((_, j) => j !== i))}><Delete fontSize="small" /></IconButton></TableCell>
                  <TableCell>{s.name}</TableCell><TableCell>{s.area}</TableCell><TableCell>{s.established}</TableCell><TableCell>{s.location}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </DialogContent>
      <DialogActions>
        <Button id="cancelButton" onClick={() => onClose(null)}>Cancel</Button>
        <Button id="buttonSave" variant="contained" disabled={!staged.length || busy} onClick={save}>Save ({staged.length})</Button>
      </DialogActions>
    </Dialog>
  );
}

/** EditPADialog.zul → PAController edit_buttonSave (now validated, location saved). */
export function ProtectedAreaEditDialog({ pa, onClose }: { pa: ProtectedAreaDto | null; onClose: (saved: boolean) => void }) {
  const notify = useNotify();
  const [f, setF] = useState(empty);
  const [err, setErr] = useState<string | undefined>();
  useEffect(() => {
    if (pa) setF({ name: pa.name, area: pa.area ?? '', established: pa.established ?? '', location: pa.location ?? '' });
    setErr(undefined);
  }, [pa]);
  const save = async () => {
    if (!f.name.trim()) return setErr('Name is required.');
    try {
      await api.put(`/api/dictionary/protected-areas/${pa!.id}`, f);
      onClose(true);
    } catch (e) {
      notify(errorMessage(e), 'error');
    }
  };
  return (
    <Dialog open={!!pa} onClose={() => onClose(false)} maxWidth="md" fullWidth>
      <DialogTitle>Edit Protected Area</DialogTitle>
      <DialogContent dividers><PaFields f={f} set={setF} error={err} /></DialogContent>
      <DialogActions>
        <Button id="cancelButton" onClick={() => onClose(false)}>Cancel</Button>
        <Button id="edit_buttonSave" variant="contained" onClick={save}>Save</Button>
      </DialogActions>
    </Dialog>
  );
}


import { Alert, Autocomplete, Button, Dialog, DialogActions, DialogContent, DialogTitle, Grid, IconButton, Table, TableBody, TableCell, TableHead, TableRow, TextField } from '@mui/material';
import Delete from '@mui/icons-material/DeleteOutline';
import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import { errorMessage, useNotify } from '../../components/Notifier';

export interface ParentOption { id: number; label: string }
interface Item { code: string; name: string; desc: string }

/**
 * Staged "add several" dialogs with an optional parent:
 * AddRealmDialog (no parent), AddBiomeDialog (realm), AddEfgDialog (biome), AddEServiceDialog (no parent), AddEsubServiceDialog (e-service).
 * Add button stages a row (duplicate check), × removes it, Save persists all, Cancel closes. Changing the parent clears the staged rows.
 */
export function CodeNameBatchDialog({ open, title, url, parentLabel, parents, limits, addButtonId, onClose }: {
  open: boolean;
  title: string;
  url: string;
  parentLabel?: string;
  parents?: ParentOption[];
  limits: { code: number; name: number; desc: number };
  addButtonId: string;
  onClose: (saved: boolean) => void;
}) {
  const notify = useNotify();
  const [parent, setParent] = useState<ParentOption | null>(null);
  const [f, setF] = useState<Item>({ code: '', name: '', desc: '' });
  const [staged, setStaged] = useState<Item[]>([]);
  const [err, setErr] = useState<string | null>(null);
  useEffect(() => {
    if (open) {
      setParent(null);
      setF({ code: '', name: '', desc: '' });
      setStaged([]);
      setErr(null);
    }
  }, [open]);

  const add = () => {
    if (parents && !parent) return setErr(`Select a ${parentLabel?.toLowerCase()}.`);
    if (!f.code.trim() || !f.name.trim()) return setErr('Code and name are required.');
    const lc = (s: string) => s.trim().toLowerCase();
    if (staged.some((s) => lc(s.code) === lc(f.code) || lc(s.name) === lc(f.name))) return setErr('Record already exists.');
    setStaged([...staged, f]);
    setF({ code: '', name: '', desc: '' });
    setErr(null);
  };
  const save = async () => {
    try {
      await api.post(url, { parentId: parent?.id ?? null, items: staged });
      notify('Saved.');
      onClose(true);
    } catch (e) {
      notify(errorMessage(e), 'error');
    }
  };

  return (
    <Dialog open={open} onClose={() => onClose(false)} maxWidth="md" fullWidth>
      <DialogTitle>{title}</DialogTitle>
      <DialogContent dividers>
        {err && <Alert severity="warning" sx={{ mb: 2 }}>{err}</Alert>}
        <Grid container spacing={2}>
          {parents && (
            <Grid item xs={12}>
              <Autocomplete options={parents} value={parent} getOptionLabel={(o) => o.label} isOptionEqualToValue={(a, b) => a.id === b.id}
                onChange={(_, v) => { setParent(v); setStaged([]); }} renderInput={(p) => <TextField {...p} label={parentLabel} required />} />
            </Grid>
          )}
          <Grid item xs={12} sm={4}><TextField id="txtbox_code" label="Code" required value={f.code} onChange={(e) => setF({ ...f, code: e.target.value })} inputProps={{ maxLength: limits.code }} /></Grid>
          <Grid item xs={12} sm={8}><TextField id="txtbox_name" label="Name" required value={f.name} onChange={(e) => setF({ ...f, name: e.target.value })} inputProps={{ maxLength: limits.name }} /></Grid>
          <Grid item xs={12}><TextField id="txtbox_desc" label="Description" multiline minRows={2} value={f.desc} onChange={(e) => setF({ ...f, desc: e.target.value })} inputProps={{ maxLength: limits.desc }} /></Grid>
        </Grid>
        <Button id={addButtonId} sx={{ mt: 2 }} variant="outlined" onClick={add}>Add to list</Button>
        {staged.length > 0 && (
          <Table size="small" sx={{ mt: 2 }}>
            <TableHead><TableRow><TableCell /><TableCell>Code</TableCell><TableCell>Name</TableCell><TableCell>Description</TableCell></TableRow></TableHead>
            <TableBody>
              {staged.map((s, i) => (
                <TableRow key={s.code}>
                  <TableCell padding="checkbox"><IconButton size="small" aria-label="Remove" onClick={() => setStaged(staged.filter((_, j) => j !== i))}><Delete fontSize="small" /></IconButton></TableCell>
                  <TableCell>{s.code}</TableCell><TableCell>{s.name}</TableCell><TableCell>{s.desc}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </DialogContent>
      <DialogActions>
        <Button id="cancelButton" onClick={() => onClose(false)}>Cancel</Button>
        <Button id="buttonSave" variant="contained" disabled={!staged.length} onClick={save}>Save ({staged.length})</Button>
      </DialogActions>
    </Dialog>
  );
}

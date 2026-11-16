import { Button, Dialog, DialogActions, DialogContent, DialogTitle, MenuItem, Stack, TextField } from '@mui/material';
import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import type { TermRow } from '../../api/types';
import { errorMessage, useNotify } from '../../components/Notifier';

export const PAC_LABELS: Record<string, string> = {
  PAC1: 'IUCN',
  PAC2: 'Protected Area System in the Phil (NIPAS Act Sec. 3): Categories',
  PAC3: 'Before NIPAS Act 1992',
};

/**
 * One dialog for every CV-term dictionary:
 * AddCurrencyDialog, AddBeneficialDialog, AddBeneficiaryTypeDialog, AddMethodDialog, AddAvailStudyInfoDialog (add)
 * and AddCvTermDialog / WHOController (edit, title "Edit <cv>"). For "PA Category" the CV (PAC1/2/3) is chosen on add.
 */
export function TermDialog({ open, cv, title, term, codeMax = 55, nameMax = 255, onClose }: {
  open: boolean;
  cv: string;
  title: string;
  term: TermRow | null;
  codeMax?: number;
  nameMax?: number;
  onClose: (t: TermRow | null) => void;
}) {
  const notify = useNotify();
  const [f, setF] = useState({ cv, code: '', name: '', desc: '' });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const isPac = cv === 'PA Category';
  useEffect(() => {
    if (open) {
      setF(term ? { cv: term.cv, code: term.code, name: term.name, desc: term.desc ?? '' } : { cv: isPac ? 'PAC1' : cv, code: '', name: '', desc: '' });
      setErrors({});
    }
  }, [open, term, cv, isPac]);

  const save = async () => {
    const e: Record<string, string> = {};
    if (!f.code.trim()) e.code = 'Code is required.';
    if (!f.name.trim()) e.name = 'Name is required.';
    setErrors(e);
    if (Object.keys(e).length) return;
    try {
      const saved = term ? await api.put<TermRow>(`/api/dictionary/terms/${term.id}`, f) : await api.post<TermRow>('/api/dictionary/terms', f);
      onClose(saved);
    } catch (err) {
      notify(errorMessage(err), 'error');
    }
  };

  return (
    <Dialog open={open} onClose={() => onClose(null)} maxWidth="sm" fullWidth>
      <DialogTitle id="lbl_Title">{term ? `Edit ${title}` : `Add ${title} Record`}</DialogTitle>
      <DialogContent dividers>
        <Stack spacing={2}>
          {isPac && !term && (
            <TextField select label="Category" value={f.cv} onChange={(e) => setF({ ...f, cv: e.target.value })}>
              {Object.entries(PAC_LABELS).map(([k, v]) => <MenuItem key={k} value={k}>{v}</MenuItem>)}
            </TextField>
          )}
          <TextField id="txtbox_code" label="Code" required value={f.code} onChange={(e) => setF({ ...f, code: e.target.value })} error={!!errors.code} helperText={errors.code} inputProps={{ maxLength: codeMax }} />
          <TextField id="txtbox_name" label="Name" required value={f.name} onChange={(e) => setF({ ...f, name: e.target.value })} error={!!errors.name} helperText={errors.name} inputProps={{ maxLength: nameMax }} />
          <TextField id="txtbox_desc" label="Description" multiline minRows={3} value={f.desc} onChange={(e) => setF({ ...f, desc: e.target.value })} inputProps={{ maxLength: 255 }} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button id="cancelButton" onClick={() => onClose(null)}>Cancel</Button>
        <Button id="buttonSave" variant="contained" onClick={save}>Save</Button>
      </DialogActions>
    </Dialog>
  );
}

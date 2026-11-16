import {
  Alert, Button, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle, FormControlLabel, Radio, RadioGroup, Stack, Tab, Tabs, Typography,
} from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import { Role, type StudyDto } from '../../api/types';
import { useAuth } from '../../auth/AuthContext';
import { errorMessage } from '../../components/Notifier';
import { SitesEditor } from './SitesEditor';
import { StudyFields } from './StudyFields';
import { validateStudy, type Errors } from './studyGraph';

/** editstudy.zul (window editWindow) → EditStudyController. Opened from the pencil in study_view.zul with arg "StudySrc". */
export function StudyEditDialog({ id, onClose }: { id: number | null; onClose: (saved: boolean) => void }) {
  const { hasRole } = useAuth();
  const { data, isLoading } = useQuery({ queryKey: ['study', id], queryFn: () => api.get<StudyDto>(`/api/studies/${id}`), enabled: id != null });
  const [s, setS] = useState<StudyDto | null>(null);
  const [tab, setTab] = useState(0);
  const [errors, setErrors] = useState<Errors>({});
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (data) setS(structuredClone(data));
    setTab(0);
    setErrors({});
    setError(null);
  }, [data, id]);

  /** buttonSave: validate the six textboxes (abstract required here) → saveRecord(). */
  const save = async () => {
    if (!s) return;
    const e = validateStudy(s, true);
    setErrors(e);
    if (Object.keys(e).length) {
      setTab(0);
      return;
    }
    setBusy(true);
    setError(null);
    try {
      await api.put<StudyDto>(`/api/studies/${s.id}`, s);
      onClose(true);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  return (
    <Dialog open={id != null} onClose={() => onClose(false)} maxWidth="lg" fullWidth>
      <DialogTitle>Edit Study</DialogTitle>
      <DialogContent dividers sx={{ minHeight: 480 }}>
        {(isLoading || !s) && <CircularProgress />}
        {s && (
          <>
            {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
            {hasRole(Role.MANAGER) && (
              <Stack id="approvalStatusRow" direction={{ xs: 'column', sm: 'row' }} alignItems={{ sm: 'center' }} spacing={2} sx={{ mb: 2 }}>
                <Typography sx={{ fontWeight: 700 }}>Approval status</Typography>
                <RadioGroup id="approvalGrp" row value={String(s.status ?? 0)} onChange={(e) => setS({ ...s, status: Number(e.target.value) })}>
                  <FormControlLabel value="0" control={<Radio id="rad_pending" />} label="Pending" />
                  <FormControlLabel value="1" control={<Radio id="rad_approval" />} label="Approved" />
                  <FormControlLabel value="2" control={<Radio id="rad_disapproval" />} label="Disapproved" />
                </RadioGroup>
              </Stack>
            )}
            <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 2 }}>
              <Tab label="Study Source" />
              <Tab label="Sites and valuations" />
            </Tabs>
            {tab === 0 ? <StudyFields s={s} set={setS} errors={errors} /> : <SitesEditor sites={s.sites} onChange={(sites) => setS({ ...s, sites })} mode="edit" />}
          </>
        )}
      </DialogContent>
      <DialogActions>
        <Button id="cancelButton" onClick={() => onClose(false)}>Cancel</Button>
        <Button id="buttonSave" variant="contained" disabled={!s || busy} onClick={save}>Save</Button>
      </DialogActions>
    </Dialog>
  );
}

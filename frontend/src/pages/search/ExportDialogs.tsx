import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, TextField } from '@mui/material';
import { useEffect, useState } from 'react';
import { download } from '../../api/client';
import { errorMessage, useNotify } from '../../components/Notifier';

/**
 * btn_export flow: AgreementDownloadDialog.zul (okBtn → next / cancelBtn → close)
 * then inputDownloadDialog.zul (textbox#tb file name, okBtn → XLSX download / cancelBtn → close).
 */
export function ExportDialogs({ open, ids, onClose }: { open: boolean; ids: number[]; onClose: () => void }) {
  const notify = useNotify();
  const [step, setStep] = useState<'agree' | 'name'>('agree');
  const [name, setName] = useState('sibol-eves-export');
  const [busy, setBusy] = useState(false);
  useEffect(() => {
    if (open) setStep('agree');
  }, [open]);

  const doDownload = async () => {
    setBusy(true);
    try {
      await download('/api/search/export', { ids, fileName: name, agreed: true }, `${name}.xlsx`);
      onClose();
    } catch (e) {
      notify(errorMessage(e), 'error');
    } finally {
      setBusy(false);
    }
  };

  return (
    <>
      <Dialog open={open && step === 'agree'} onClose={onClose} maxWidth="sm" fullWidth>
        <DialogTitle>Download</DialogTitle>
        <DialogContent>
          <DialogContentText>
            By downloading the result list, you are acknowledging and agreeing to abide by the Terms of Use provided during the registration process.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button id="cancelBtn" onClick={onClose}>Cancel</Button>
          <Button id="okBtn" variant="contained" onClick={() => setStep('name')}>I agree</Button>
        </DialogActions>
      </Dialog>
      <Dialog open={open && step === 'name'} onClose={onClose} maxWidth="xs" fullWidth>
        <DialogTitle>Export to Excel</DialogTitle>
        <DialogContent>
          <DialogContentText sx={{ mb: 2 }}>{ids.length} studies will be exported, one row per value.</DialogContentText>
          <TextField id="tb" label="File name" value={name} onChange={(e) => setName(e.target.value)} autoFocus
            InputProps={{ endAdornment: <span style={{ color: '#666' }}>.xlsx</span> }} />
        </DialogContent>
        <DialogActions>
          <Button id="cancelBtn" onClick={onClose}>Cancel</Button>
          <Button id="okBtn" variant="contained" disabled={busy || !ids.length} onClick={doDownload}>Download</Button>
        </DialogActions>
      </Dialog>
    </>
  );
}

import { Alert, Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, TextField, Typography } from '@mui/material';
import { useEffect, useState } from 'react';

/** 10 random letters, as deleteDialog.zul's lbl_rndom. */
export function randomCode(len = 10) {
  const letters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
  return Array.from({ length: len }, () => letters[Math.floor(Math.random() * letters.length)]).join('');
}

/**
 * deleteDialog.zul: the user must retype a random code before deleting.
 * The ZK OK handler never compared the typed code; here OK stays disabled until it matches.
 */
export function ConfirmDeleteDialog({ open, itemLabel, onCancel, onConfirm }: {
  open: boolean;
  itemLabel: string;
  onCancel: () => void;
  onConfirm: () => void;
}) {
  const [code, setCode] = useState(randomCode());
  const [typed, setTyped] = useState('');
  useEffect(() => {
    if (open) {
      setCode(randomCode());
      setTyped('');
    }
  }, [open]);
  return (
    <Dialog open={open} onClose={onCancel} maxWidth="xs" fullWidth>
      <DialogTitle>Delete {itemLabel}?</DialogTitle>
      <DialogContent>
        <Alert severity="warning" sx={{ mb: 2 }}>
          This removes the item from the list. Type the code below to confirm.
        </Alert>
        <Box sx={{ fontFamily: 'monospace', fontSize: 20, letterSpacing: 3, textAlign: 'center', p: 1, bgcolor: 'background.default', borderRadius: 1, userSelect: 'none' }}>
          {code}
        </Box>
        <Typography variant="body2" color="text.secondary" sx={{ mt: 2, mb: 1 }}>
          Enter the code
        </Typography>
        <TextField id="delete-code" autoFocus value={typed} onChange={(e) => setTyped(e.target.value.toUpperCase())} />
      </DialogContent>
      <DialogActions>
        <Button onClick={onCancel}>Cancel</Button>
        <Button variant="contained" color="error" disabled={typed !== code} onClick={onConfirm}>
          Delete
        </Button>
      </DialogActions>
    </Dialog>
  );
}

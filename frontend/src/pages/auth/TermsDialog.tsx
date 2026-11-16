import { Box, Button, Checkbox, Dialog, DialogActions, DialogContent, DialogTitle, FormControlLabel, Typography } from '@mui/material';
import { useState } from 'react';

const TERMS = [
  'Downloading and using the SIBOL-EVES database for policy, research, and educational purposes is permitted.',
  'The data and monetary values provided in the database are derived from economic valuation studies published in various sources, such as journals, reports, working papers, and conference papers. While efforts have been made to ensure accuracy and reliability, errors or omissions may occur. It is advised to exercise caution when applying these values.',
  'SIBOL-EVES provides external links to the studies included in the database. However, this does not imply endorsement or guarantee of their availability.',
  'Users are encouraged to independently verify the suitability and accuracy of any information provided in the database and to use their own judgment and discretion.',
  'SIBOL and its partner organizations are not liable for any damages that may arise from your use of the SIBOL-EVES website and database.',
  'SIBOL-EVES reserves the right to amend or update the Terms of Use at any time without prior notice. The updated version will be effective immediately upon posting.',
];

export function TermsText() {
  return (
    <>
      <Typography paragraph>By signing up, you agree to the following Terms of Use for the SIBOL-EVES database:</Typography>
      <Box component="ol" sx={{ pl: 3, '& li': { mb: 1 } }}>
        {TERMS.map((t) => <li key={t}>{t}</li>)}
      </Box>
    </>
  );
}

/** agreement.zul + AgreementController: opened on entering /register (RegisterInit). */
export function TermsDialog({ open, onAgree, onCancel }: { open: boolean; onAgree: () => void; onCancel: () => void }) {
  const [checked, setChecked] = useState(false);
  return (
    <Dialog open={open} maxWidth="md" fullWidth aria-labelledby="terms-title">
      <DialogTitle id="terms-title">Agreement / Terms of Use</DialogTitle>
      <DialogContent dividers>
        <TermsText />
        <FormControlLabel
          control={<Checkbox id="checkAgree" checked={checked} onChange={(e) => setChecked(e.target.checked)} />}
          label="I HAVE READ AND UNDERSTAND THIS AGREEMENT."
        />
      </DialogContent>
      <DialogActions>
        <Button id="buttonCancel" onClick={onCancel}>Cancel</Button>
        <Button id="buttonOk" variant="contained" disabled={!checked} onClick={onAgree}>I agree</Button>
      </DialogActions>
    </Dialog>
  );
}

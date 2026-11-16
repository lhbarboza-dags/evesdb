import { Box, Button, Stack } from '@mui/material';
import PictureAsPdf from '@mui/icons-material/PictureAsPdf';

/** doc.zul + _doc.zul: the web manual in an iframe (now with the PDF link from the banner). */
export function ManualPage() {
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: 'calc(100vh - 64px - 56px)' }}>
      <Stack direction="row" justifyContent="flex-end" sx={{ px: 2, py: 1 }}>
        <Button startIcon={<PictureAsPdf />} href="/manual/SIBOL-EVES Database Manual.pdf" target="_blank" rel="noopener">
          PDF Download
        </Button>
      </Stack>
      <Box component="iframe" id="iframehelp" title="SIBOL-EVES user manual" src="/manual/SIBOL-EVES Database Manual.html" sx={{ flex: 1, border: 0, width: '100%' }} />
    </Box>
  );
}

import { createTheme } from '@mui/material/styles';

// Forest/estuary palette for an ecosystem-services database; serif headings for the research-database feel.
export const theme = createTheme({
  palette: {
    primary: { main: '#1d5c57', dark: '#123f3b', light: '#4f8c86', contrastText: '#ffffff' },
    secondary: { main: '#a8641c', contrastText: '#ffffff' },
    background: { default: '#f3f6f5', paper: '#ffffff' },
    text: { primary: '#1c2b2a', secondary: '#4b5e5c' },
    success: { main: '#2e7d4f' },
    warning: { main: '#b26a00' },
    error: { main: '#b3261e' },
    divider: '#d6e0de',
  },
  typography: {
    fontFamily: '"Source Sans 3", "Segoe UI", system-ui, sans-serif',
    h1: { fontFamily: '"Source Serif 4", Georgia, serif', fontWeight: 600 },
    h2: { fontFamily: '"Source Serif 4", Georgia, serif', fontWeight: 600 },
    h3: { fontFamily: '"Source Serif 4", Georgia, serif', fontWeight: 600 },
    h4: { fontFamily: '"Source Serif 4", Georgia, serif', fontWeight: 600, fontSize: '1.75rem' },
    h5: { fontFamily: '"Source Serif 4", Georgia, serif', fontWeight: 600, fontSize: '1.3rem' },
    h6: { fontWeight: 700, fontSize: '1.05rem' },
    button: { textTransform: 'none', fontWeight: 600 },
  },
  shape: { borderRadius: 6 },
  components: {
    MuiButton: { defaultProps: { disableElevation: true } },
    MuiTextField: { defaultProps: { size: 'small', fullWidth: true } },
    MuiAutocomplete: { defaultProps: { size: 'small' } },
    MuiTableCell: { styleOverrides: { head: { fontWeight: 700, background: '#e8efed' } } },
    MuiDialogTitle: { styleOverrides: { root: { fontFamily: '"Source Serif 4", Georgia, serif', fontWeight: 600 } } },
  },
});

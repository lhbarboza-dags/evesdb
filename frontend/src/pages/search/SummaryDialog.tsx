import { Box, Button, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle, Tab, Table, TableBody, TableCell, TableHead, TableRow, Tabs, Typography } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import { api } from '../../api/client';
import type { Count, Summary } from '../../api/types';

/** ShowSummary.zul (window#showSummary) + ShowSummaryController: breakdown/count of the current result list. */
export function SummaryDialog({ open, ids, onClose }: { open: boolean; ids: number[]; onClose: () => void }) {
  const [tab, setTab] = useState(0);
  const { data, isLoading } = useQuery({
    queryKey: ['summary', ids],
    queryFn: () => api.post<Summary>('/api/search/summary', { ids }),
    enabled: open,
  });
  const tabs: { label: string; head: string; unit: string; rows?: Count[] }[] = [
    { label: 'Country', head: 'Country', unit: 'Studies', rows: data?.countries },
    { label: 'Ecosystem', head: 'Realm / Biome / EFG', unit: 'Studies', rows: data?.ecosystems },
    { label: 'Service', head: 'Service / Sub-service', unit: 'Studies', rows: data?.services },
    { label: 'Values by ecosystem', head: 'Realm / Biome / EFG', unit: 'Values', rows: data?.ecosystemValues },
    { label: 'Values by service', head: 'Service / Sub-service', unit: 'Values', rows: data?.serviceValues },
  ];
  const t = tabs[tab];
  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>Breakdown / Count</DialogTitle>
      <DialogContent dividers sx={{ minHeight: 360 }}>
        <Tabs value={tab} onChange={(_, v) => setTab(v)} variant="scrollable" sx={{ mb: 2 }}>
          {tabs.map((x) => <Tab key={x.label} label={x.label} />)}
        </Tabs>
        {isLoading ? (
          <CircularProgress />
        ) : (
          <>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              {ids.length} studies in the current result. Total: {(t.rows ?? []).length} {t.head.toLowerCase()} entries.
            </Typography>
            <Box sx={{ maxHeight: 420, overflow: 'auto' }}>
              <Table size="small" stickyHeader>
                <TableHead>
                  <TableRow>
                    <TableCell>Code : Name</TableCell>
                    <TableCell align="right">{t.unit}</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {(t.rows ?? []).map((r) => (
                    <TableRow key={r.label}>
                      <TableCell>{r.label}</TableCell>
                      <TableCell align="right" sx={{ fontVariantNumeric: 'tabular-nums' }}>{r.count}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </Box>
          </>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Close</Button>
      </DialogActions>
    </Dialog>
  );
}

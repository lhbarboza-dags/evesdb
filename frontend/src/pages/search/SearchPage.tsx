import {
  Autocomplete, Box, Button, Chip, CircularProgress, Collapse, Divider, FormControlLabel, IconButton, Pagination, Paper, Popover,
  Radio, RadioGroup, Stack, Switch, Tab, Table, TableBody, TableCell, TableHead, TableRow, Tabs, TextField, Tooltip, Typography,
} from '@mui/material';
import Close from '@mui/icons-material/Close';
import FilterList from '@mui/icons-material/FilterList';
import Map from '@mui/icons-material/Map';
import BarChart from '@mui/icons-material/BarChart';
import FileDownload from '@mui/icons-material/FileDownload';
import ExpandMore from '@mui/icons-material/ExpandMore';
import { useMutation } from '@tanstack/react-query';
import { useEffect, useMemo, useState, type FormEvent, type MouseEvent } from 'react';
import { api } from '../../api/client';
import { useSearchFilters } from '../../api/hooks';
import type { Count, EcoOption, MethodOption, SearchRequest, SearchResult, ServiceOption, StudyRow } from '../../api/types';
import { errorMessage, useNotify } from '../../components/Notifier';
import { StudyDetailLoader } from './StudyDetail';
import { SummaryDialog } from './SummaryDialog';
import { ExportDialogs } from './ExportDialogs';
import { SiteMapPanel } from './SiteMapPanel';

const PAGE = 10;
type ExtraTab = { key: string; kind: 'study' | 'map'; label: string; studyId?: number; ids?: number[] };

/** search.zul + searchContent.zul → SearchController (+ StudySrcRenderer / StudySrcDetailRenderer). */
export function SearchPage() {
  const notify = useNotify();
  const filters = useSearchFilters();
  const [keyword, setKeyword] = useState('');
  const [matchAll, setMatchAll] = useState(false);
  const [ecos, setEcos] = useState<EcoOption[]>([]);
  const [servs, setServs] = useState<ServiceOption[]>([]);
  const [meths, setMeths] = useState<MethodOption[]>([]);
  const [countries, setCountries] = useState<string[]>([]);
  const [showFilters, setShowFilters] = useState(false);
  const [detailed, setDetailed] = useState(false);
  const [page, setPage] = useState(1);
  const [tabs, setTabs] = useState<ExtraTab[]>([]);
  const [tab, setTab] = useState('results');
  const [summaryOpen, setSummaryOpen] = useState(false);
  const [exportOpen, setExportOpen] = useState(false);
  const [mapCount, setMapCount] = useState(0);

  const search = useMutation({
    mutationFn: (req: SearchRequest) => api.post<SearchResult>('/api/search', req),
    onSuccess: () => setPage(1),
    onError: (e) => notify(errorMessage(e), 'error'),
  });
  const result = search.data;
  const ids = useMemo(() => result?.studies.map((s) => s.id) ?? [], [result]);

  const buildRequest = (): SearchRequest => ({
    keyword: keyword.trim(),
    matchAll,
    countries,
    ecosystems: ecos.map((e) => ({ level: e.level, code: e.code })),
    services: servs.map((s) => ({ level: s.level, code: s.code })),
    methods: meths.map((m) => m.code),
  });

  // doAfterCompose → search() with no filter.
  useEffect(() => {
    search.mutate({ keyword: '', matchAll: false, countries: [], ecosystems: [], services: [], methods: [] });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  /** #search2_btn (and Enter in keywordBox, which had no onOK in ZK). */
  const onSearch = (e?: FormEvent) => {
    e?.preventDefault();
    setTab('results');
    search.mutate(buildRequest());
  };

  /** #clear_btn "Clear Filters" — had no listener in ZK. */
  const clearFilters = () => {
    setKeyword('');
    setEcos([]);
    setServs([]);
    setMeths([]);
    setCountries([]);
    setMatchAll(false);
  };

  /** Row click → focus existing tab or add a closable study tab (title cut to 20 chars). */
  const openStudy = (s: StudyRow) => {
    const key = `study-${s.id}`;
    if (!tabs.some((t) => t.key === key)) setTabs((t) => [...t, { key, kind: 'study', label: s.title.length > 20 ? `${s.title.slice(0, 20)}…` : s.title, studyId: s.id }]);
    setTab(key);
  };

  /** #btn_gmap → closable "GMAP<n>" tab with the sites of the current result. */
  const openMap = () => {
    const n = mapCount + 1;
    setMapCount(n);
    const key = `map-${n}`;
    setTabs((t) => [...t, { key, kind: 'map', label: `GMAP${n}`, ids }]);
    setTab(key);
  };

  const closeTab = (key: string, e: MouseEvent) => {
    e.stopPropagation();
    setTabs((t) => t.filter((x) => x.key !== key));
    if (tab === key) setTab('results');
  };

  const filterPanel = (
    <Box component="form" onSubmit={onSearch} noValidate>
      <Stack spacing={2}>
        <TextField id="keywordBox" label="Keyword (title, author or year)" value={keyword} onChange={(e) => setKeyword(e.target.value)} />
        <Autocomplete id="chosen_Ecosytem" multiple options={filters.data?.ecosystems ?? []} value={ecos} onChange={(_, v) => setEcos(v)}
          getOptionLabel={(o) => o.label} groupBy={(o) => o.level === 'REALM' ? 'Realm' : o.level === 'BIOME' ? 'Biome' : 'EFG'}
          isOptionEqualToValue={(a, b) => a.level === b.level && a.code === b.code} renderInput={(p) => <TextField {...p} label="Ecosystem" />} />
        <Autocomplete id="chosen_Service" multiple options={filters.data?.services ?? []} value={servs} onChange={(_, v) => setServs(v)}
          getOptionLabel={(o) => o.label} isOptionEqualToValue={(a, b) => a.level === b.level && a.code === b.code}
          renderInput={(p) => <TextField {...p} label="Ecosystem service" />} />
        <Autocomplete id="chosen_Method" multiple options={filters.data?.methods ?? []} value={meths} onChange={(_, v) => setMeths(v)}
          getOptionLabel={(o) => o.label} isOptionEqualToValue={(a, b) => a.code === b.code} renderInput={(p) => <TextField {...p} label="Valuation method" />} />
        <Autocomplete id="chosen_country" multiple options={filters.data?.countries ?? []} value={countries} onChange={(_, v) => setCountries(v)}
          renderInput={(p) => <TextField {...p} label="Country" />} />
        <RadioGroup id="searchOptions" row value={matchAll ? 'exact' : 'all'} onChange={(e) => setMatchAll(e.target.value === 'exact')}>
          <FormControlLabel value="all" control={<Radio id="rad_all" size="small" />} label="Match any" />
          <FormControlLabel value="exact" control={<Radio id="rad_exact" size="small" />} label="Match all" />
        </RadioGroup>
        <Stack direction="row" spacing={1}>
          <Button id="search2_btn" type="submit" variant="contained" disabled={search.isPending}>Search</Button>
          <Button id="clear_btn" onClick={clearFilters}>Clear Filters</Button>
        </Stack>
      </Stack>
    </Box>
  );

  const pageRows = result?.studies.slice((page - 1) * PAGE, page * PAGE) ?? [];

  return (
    <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, minHeight: 'calc(100vh - 64px - 56px)' }}>
      <Paper square elevation={0} sx={{ width: { md: 340 }, flexShrink: 0, borderRight: { md: 1 }, borderBottom: { xs: 1, md: 0 }, borderColor: 'divider', p: 2 }}>
        <Stack direction="row" alignItems="center" justifyContent="space-between" sx={{ mb: { md: 2 } }}>
          <Typography variant="h6" component="h1">Search studies</Typography>
          <Button id="wst_searchOptions" sx={{ display: { md: 'none' } }} startIcon={<FilterList />} onClick={() => setShowFilters((s) => !s)}>
            {showFilters ? 'Hide' : 'Show'} filters
          </Button>
        </Stack>
        <Box sx={{ display: { xs: 'none', md: 'block' } }}>{filterPanel}</Box>
        <Collapse in={showFilters} sx={{ display: { md: 'none' } }}><Box sx={{ pt: 2 }}>{filterPanel}</Box></Collapse>
      </Paper>

      <Box sx={{ flex: 1, minWidth: 0, p: { xs: 1.5, md: 3 } }}>
        <Tabs id="tb" value={tab} onChange={(_, v) => setTab(v)} variant="scrollable" scrollButtons="auto" sx={{ borderBottom: 1, borderColor: 'divider', mb: 2 }}>
          <Tab value="results" label="Results" />
          {tabs.map((t) => (
            <Tab key={t.key} value={t.key} label={
              <Stack direction="row" alignItems="center" spacing={0.5}>
                <span>{t.label}</span>
                <IconButton size="small" component="span" aria-label={`Close ${t.label}`} onClick={(e) => closeTab(t.key, e)}><Close fontSize="inherit" /></IconButton>
              </Stack>
            } />
          ))}
        </Tabs>

        {tab === 'results' && (
          <>
            <Stack direction={{ xs: 'column', sm: 'row' }} alignItems={{ sm: 'center' }} justifyContent="space-between" gap={1} sx={{ mb: 2 }}>
              <Box>
                <Typography id="results" variant="h6" component="p">
                  {search.isPending ? 'Searching…' : `${result?.studyCount ?? 0} studies`}
                  <Typography id="valueNum" component="span" color="text.secondary" sx={{ ml: 1 }}>· {result?.valueCount ?? 0} values</Typography>
                </Typography>
                {!!result?.filterCounts.length && (
                  <Stack id="EcoCount" direction="row" flexWrap="wrap" gap={0.5} sx={{ mt: 0.5 }}>
                    {result.filterCounts.map((c) => <Chip key={c.label} size="small" label={`${c.label}: ${c.count}`} />)}
                  </Stack>
                )}
              </Box>
              <Stack direction="row" flexWrap="wrap" gap={1} alignItems="center">
                <FormControlLabel control={<Switch id="summaryCheck" checked={detailed} onChange={(e) => setDetailed(e.target.checked)} />} label="Show details" />
                <Button id="buttonSummary" variant="outlined" startIcon={<BarChart />} disabled={!ids.length} onClick={() => setSummaryOpen(true)}>Summary</Button>
                <Button id="btn_export" variant="outlined" startIcon={<FileDownload />} disabled={!ids.length} onClick={() => setExportOpen(true)}>Export</Button>
                <Button id="btn_gmap" variant="outlined" startIcon={<Map />} disabled={!ids.length} onClick={openMap}>Map</Button>
              </Stack>
            </Stack>

            {search.isPending && <CircularProgress />}
            {!search.isPending && result && result.studies.length === 0 && <Typography color="text.secondary">No studies match these filters.</Typography>}
            <Stack id={detailed ? 'studyDetailListbox' : 'studyListbox'} spacing={1.5}>
              {pageRows.map((s, i) => (
                <ResultCard key={s.id} index={(page - 1) * PAGE + i + 1} row={s} detailed={detailed} onOpen={() => openStudy(s)} />
              ))}
            </Stack>
            {(result?.studies.length ?? 0) > PAGE && (
              <Pagination sx={{ mt: 2 }} count={Math.ceil(result!.studies.length / PAGE)} page={page} onChange={(_, p) => setPage(p)} />
            )}
          </>
        )}

        {tabs.map((t) =>
          t.key === tab ? (
            <Paper key={t.key} variant="outlined" sx={{ p: { xs: 2, md: 3 } }}>
              {t.kind === 'study' ? <StudyDetailLoader id={t.studyId!} /> : <SiteMapPanel ids={t.ids!} />}
            </Paper>
          ) : null,
        )}
      </Box>

      <SummaryDialog open={summaryOpen} ids={ids} onClose={() => setSummaryOpen(false)} />
      <ExportDialogs open={exportOpen} ids={ids} onClose={() => setExportOpen(false)} />
    </Box>
  );
}

/** One row of studyListbox (StudySrcRenderer) / studyDetailListbox (StudySrcDetailRenderer). */
function ResultCard({ index, row, detailed, onOpen }: { index: number; row: StudyRow; detailed: boolean; onOpen: () => void }) {
  return (
    <Paper variant="outlined" sx={{ p: 2, cursor: 'pointer', '&:hover': { borderColor: 'primary.light' } }} onClick={onOpen}
      role="button" tabIndex={0} onKeyDown={(e) => { if (e.key === 'Enter') onOpen(); }}>
      <Typography sx={{ fontWeight: 700 }}>{index}. {row.title} {row.pubYear ? `(${row.pubYear})` : ''}</Typography>
      <Typography variant="body2" color="text.secondary">{row.author}</Typography>
      <Stack direction="row" flexWrap="wrap" gap={1} sx={{ mt: 1 }}>
        <CountPopover label="Ecosystem" counts={row.byEcosystem} total={row.valueCount} />
        <CountPopover label="Service" counts={row.byService} total={row.valueCount} />
        <CountPopover label="Method" counts={row.byMethod} total={row.valueCount} />
        <CountPopover label="Values" counts={row.byEcosystemService} total={row.valueCount} head="Ecosystem - Service" />
      </Stack>
      {detailed && (
        <Box sx={{ mt: 2 }} onClick={(e) => e.stopPropagation()}>
          <Divider sx={{ mb: 2 }} />
          <StudyDetailLoader id={row.id} />
        </Box>
      )}
    </Paper>
  );
}

/** The 4 chevron buttons per row that open a popup of counts (StudySrcRenderer getPopup*Values). */
function CountPopover({ label, counts, total, head }: { label: string; counts: Count[]; total: number; head?: string }) {
  const [anchor, setAnchor] = useState<HTMLElement | null>(null);
  return (
    <>
      <Tooltip title={`Show values by ${label.toLowerCase()}`}>
        <Button size="small" variant="text" endIcon={<ExpandMore />} onClick={(e) => { e.stopPropagation(); setAnchor(e.currentTarget); }}>
          {label}: See {total} values
        </Button>
      </Tooltip>
      <Popover open={!!anchor} anchorEl={anchor} onClose={(e: MouseEvent) => { e?.stopPropagation?.(); setAnchor(null); }} anchorOrigin={{ vertical: 'bottom', horizontal: 'left' }}>
        <Box sx={{ p: 1, maxWidth: 520, maxHeight: 400, overflow: 'auto' }} onClick={(e) => e.stopPropagation()}>
          <Table size="small">
            <TableHead>
              <TableRow><TableCell>{head ?? label}</TableCell><TableCell align="right">Number of Values</TableCell></TableRow>
            </TableHead>
            <TableBody>
              {counts.map((c) => (
                <TableRow key={c.label}><TableCell>{c.label || '—'}</TableCell><TableCell align="right">{c.count}</TableCell></TableRow>
              ))}
            </TableBody>
          </Table>
        </Box>
      </Popover>
    </>
  );
}

import {
  Box, Button, CircularProgress, Collapse, Container, IconButton, List, ListItemButton, ListItemText, Paper, Stack, Tab, Table, TableBody,
  TableCell, TableHead, TablePagination, TableRow, Tabs, TextField, Tooltip, Typography,
} from '@mui/material';
import Add from '@mui/icons-material/Add';
import Edit from '@mui/icons-material/Edit';
import Delete from '@mui/icons-material/DeleteOutline';
import KeyboardArrowDown from '@mui/icons-material/KeyboardArrowDown';
import KeyboardArrowRight from '@mui/icons-material/KeyboardArrowRight';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { Fragment, useState, type FormEvent, type ReactNode } from 'react';
import { api } from '../../api/client';
import type { ClimateTypeRow, CountryInfoRow, CountryRow, EServiceRow, ProtectedAreaDto, RealmRow, TermRow } from '../../api/types';
import { useConfirm } from '../../components/ConfirmDialog';
import { errorMessage, useNotify } from '../../components/Notifier';
import { CodeNameBatchDialog } from './CodeNameBatchDialog';
import { ClimateDialog, CountryDialog, CountryInfoAddDialog, CountryInfoEditDialog, EcosystemChainDialog, ServiceChainDialog } from './OtherDialogs';
import { ProtectedAreaAddDialog, ProtectedAreaEditDialog } from './ProtectedAreaDialogs';
import { PAC_LABELS, TermDialog } from './TermDialog';

const TABS = [
  'Country', 'Climate', 'Protected Areas', 'Protected Areas Category', 'Beneficiary Unit', 'Beneficiary Type', 'Currency', 'Ecosystem', 'Service',
  'Valuation Method', 'Available Study Info',
] as const;

/** dictionary.zul (DataMgtInit) + _dictionary.zul → DictionaryController (tabbox#tb). */
export function DictionaryPage() {
  const [tab, setTab] = useState(0);
  const t = TABS[tab];
  return (
    <Container maxWidth="xl" sx={{ py: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>Dictionary</Typography>
      <Tabs id="tb" value={tab} onChange={(_, v) => setTab(v)} variant="scrollable" scrollButtons="auto" sx={{ borderBottom: 1, borderColor: 'divider', mb: 2 }}>
        {TABS.map((x) => <Tab key={x} label={x} />)}
      </Tabs>
      {t === 'Country' && <CountryTab />}
      {t === 'Climate' && <ClimateTab />}
      {t === 'Protected Areas' && <ProtectedAreaTab />}
      {t === 'Protected Areas Category' && <TermTab cv="PA Category" title="PA Category" searchId="btn_PAC" addId="btn_addPAC" category />}
      {t === 'Beneficiary Unit' && <TermTab cv="Beneficiary Unit" title="Beneficiary Unit" searchId="btn_beneficial" addId="btn_addbeneficial" />}
      {t === 'Beneficiary Type' && <TermTab cv="Beneficiary Type" title="Beneficiary Type" searchId="btn_beneficiaryType" addId="btn_addbeneficiaryType" />}
      {t === 'Currency' && <TermTab cv="Currency" title="Currency" searchId="btn_searchCurrency" addId="btn_addCurrency" />}
      {t === 'Ecosystem' && <EcosystemTab />}
      {t === 'Service' && <ServiceTab />}
      {t === 'Valuation Method' && <TermTab cv="Valuation Method" title="Valuation Method" searchId="btn_searchMthd" addId="btn_addValMthd" codeMax={25} nameMax={100} />}
      {t === 'Available Study Info' && <TermTab cv="Available study Info" title="Available Study Info" searchId="btn_searchavailstudyinfo" addId="btn_addavailstudyinfo" />}
    </Container>
  );
}

// ---------------------------------------------------------------- shared bits

function Toolbar({ searchId, addId, addLabel, onAdd, onSearch, extra }: { searchId: string; addId?: string; addLabel?: string; onAdd?: () => void; onSearch: (q: string) => void; extra?: ReactNode }) {
  const [q, setQ] = useState('');
  const submit = (e: FormEvent) => {
    e.preventDefault();
    onSearch(q.trim().toLowerCase());
  };
  return (
    <Stack direction={{ xs: 'column', md: 'row' }} gap={1} justifyContent="space-between" sx={{ mb: 2 }}>
      <Box component="form" onSubmit={submit} sx={{ display: 'flex', gap: 1, maxWidth: 480, flex: 1 }}>
        <TextField label="Search" value={q} onChange={(e) => setQ(e.target.value)} />
        <Button id={searchId} type="submit" variant="outlined">Search</Button>
      </Box>
      <Stack direction="row" gap={1} flexWrap="wrap">
        {extra}
        {addId && onAdd && <Button id={addId} variant="contained" startIcon={<Add />} onClick={onAdd}>{addLabel ?? 'New'}</Button>}
      </Stack>
    </Stack>
  );
}

/** Row edit/delete with confirmation ("You are about to delete <name>…") → soft delete. */
function useDelete(url: (id: number) => string, refresh: () => void) {
  const confirm = useConfirm();
  const notify = useNotify();
  return async (id: number, name: string) => {
    if (!(await confirm({ message: `You are about to delete ${name}. Do you wish to continue?`, danger: true, confirmLabel: 'Delete' }))) return;
    try {
      await api.del(url(id));
      notify('Deleted.');
      refresh();
    } catch (e) {
      notify(errorMessage(e), 'error');
    }
  };
}

function RowActions({ onEdit, onDelete, label }: { onEdit?: () => void; onDelete?: () => void; label: string }) {
  return (
    <Box sx={{ whiteSpace: 'nowrap' }}>
      {onEdit && <Tooltip title={`Edit ${label}`}><IconButton size="small" aria-label={`Edit ${label}`} onClick={onEdit}><Edit fontSize="small" /></IconButton></Tooltip>}
      {onDelete && <Tooltip title={`Delete ${label}`}><IconButton size="small" aria-label={`Delete ${label}`} onClick={onDelete}><Delete fontSize="small" /></IconButton></Tooltip>}
    </Box>
  );
}

// ---------------------------------------------------------------- CV term tabs

/** Currency / Beneficiary Unit / Beneficiary Type / Valuation Method / Available Study Info / PA Category (CvTermRowRenderer). */
function TermTab({ cv, title, searchId, addId, category, codeMax, nameMax }: { cv: string; title: string; searchId: string; addId: string; category?: boolean; codeMax?: number; nameMax?: number }) {
  const qc = useQueryClient();
  const { data, isLoading } = useQuery({ queryKey: ['terms', cv], queryFn: () => api.get<TermRow[]>(`/api/dictionary/terms?cv=${encodeURIComponent(cv)}`) });
  const [q, setQ] = useState('');
  const [dialog, setDialog] = useState<{ term: TermRow | null } | null>(null);
  const refresh = () => qc.invalidateQueries({ queryKey: ['terms', cv] });
  const del = useDelete((id) => `/api/dictionary/terms/${id}`, refresh);
  const rows = (data ?? []).filter((t) => !q || t.name.toLowerCase().includes(q) || t.code.toLowerCase().includes(q));
  return (
    <>
      <Toolbar searchId={searchId} addId={addId} onAdd={() => setDialog({ term: null })} onSearch={setQ} />
      <Paper variant="outlined" sx={{ overflowX: 'auto' }}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Code</TableCell>{category && <TableCell>Category</TableCell>}<TableCell>Name</TableCell><TableCell>Description</TableCell><TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {isLoading && <TableRow><TableCell colSpan={5}><CircularProgress size={22} /></TableCell></TableRow>}
            {rows.map((t) => (
              <TableRow key={t.id} hover>
                <TableCell>{t.code}</TableCell>
                {category && <TableCell>{PAC_LABELS[t.cv] ?? t.cv}</TableCell>}
                <TableCell>{t.name}</TableCell>
                <TableCell>{t.desc}</TableCell>
                <TableCell align="right"><RowActions label={t.name} onEdit={() => setDialog({ term: t })} onDelete={() => del(t.id, t.name)} /></TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>
      <TermDialog open={!!dialog} cv={cv} title={title} term={dialog?.term ?? null} codeMax={codeMax} nameMax={nameMax}
        onClose={(saved) => { setDialog(null); if (saved) { refresh(); qc.invalidateQueries({ queryKey: ['lookups'] }); } }} />
    </>
  );
}

// ---------------------------------------------------------------- Country

/** tab_country: grid_country (CountryItemRenderer) with per-year detail (CountryInfoRenderer). */
function CountryTab() {
  const qc = useQueryClient();
  const { data, isLoading } = useQuery({ queryKey: ['dict-countries'], queryFn: () => api.get<CountryRow[]>('/api/dictionary/countries') });
  const [q, setQ] = useState('');
  const [open, setOpen] = useState<Record<number, boolean>>({});
  const [edit, setEdit] = useState<{ country: CountryRow | null } | null>(null);
  const [addInfo, setAddInfo] = useState<CountryRow | null>(null);
  const [editInfo, setEditInfo] = useState<CountryInfoRow | null>(null);
  const refresh = () => { qc.invalidateQueries({ queryKey: ['dict-countries'] }); qc.invalidateQueries({ queryKey: ['lookups'] }); };
  const delCountry = useDelete((id) => `/api/dictionary/countries/${id}`, refresh);
  const delInfo = useDelete((id) => `/api/dictionary/country-infos/${id}`, refresh);
  const rows = (data ?? []).filter((c) => !q || c.name.toLowerCase().includes(q));
  return (
    <>
      <Toolbar searchId="btn_searchCountry" addId="btn_addCountry" onAdd={() => setEdit({ country: null })} onSearch={setQ} />
      <Paper variant="outlined" sx={{ overflowX: 'auto' }}>
        <Table size="small" id="grid_country">
          <TableHead><TableRow><TableCell padding="checkbox" /><TableCell>Code</TableCell><TableCell>Country Name</TableCell><TableCell>Currency</TableCell><TableCell>Info per year</TableCell><TableCell align="right">Actions</TableCell></TableRow></TableHead>
          <TableBody>
            {isLoading && <TableRow><TableCell colSpan={6}><CircularProgress size={22} /></TableCell></TableRow>}
            {rows.map((c) => (
              <Fragment key={c.id}>
                <TableRow hover>
                  <TableCell padding="checkbox">
                    <IconButton size="small" aria-label="Show years" onClick={() => setOpen({ ...open, [c.id]: !open[c.id] })}>{open[c.id] ? <KeyboardArrowDown /> : <KeyboardArrowRight />}</IconButton>
                  </TableCell>
                  <TableCell>{c.code}</TableCell>
                  <TableCell>{c.name}</TableCell>
                  <TableCell>{c.currency?.name ?? '—'}</TableCell>
                  <TableCell><Button size="small" startIcon={<Add />} onClick={() => setAddInfo(c)}>Add</Button> {c.infos.length} year(s)</TableCell>
                  <TableCell align="right"><RowActions label={c.name} onEdit={() => setEdit({ country: c })} onDelete={() => delCountry(c.id, c.name)} /></TableCell>
                </TableRow>
                <TableRow>
                  <TableCell colSpan={6} sx={{ py: 0, borderBottom: open[c.id] ? undefined : 0 }}>
                    <Collapse in={!!open[c.id]} unmountOnExit>
                      <Box sx={{ py: 1.5 }}>
                        <Typography variant="subtitle2" gutterBottom>WB Income Group, GDP per Capita, Population Density Information by Year {c.name.toUpperCase()}</Typography>
                        <Table size="small">
                          <TableHead><TableRow><TableCell>Year</TableCell><TableCell>WB Income</TableCell><TableCell>GDP per Capita</TableCell><TableCell>Population Density</TableCell><TableCell align="right">Actions</TableCell></TableRow></TableHead>
                          <TableBody>
                            {!c.infos.length && <TableRow><TableCell colSpan={5}>No yearly information.</TableCell></TableRow>}
                            {c.infos.map((i) => (
                              <TableRow key={i.id}>
                                <TableCell>{i.year}</TableCell><TableCell>{i.wbIncome}</TableCell><TableCell>{i.gdpPerCapita}</TableCell><TableCell>{i.popDensity}</TableCell>
                                <TableCell align="right"><RowActions label={String(i.year)} onEdit={() => setEditInfo(i)} onDelete={() => delInfo(i.id, String(i.year))} /></TableCell>
                              </TableRow>
                            ))}
                          </TableBody>
                        </Table>
                      </Box>
                    </Collapse>
                  </TableCell>
                </TableRow>
              </Fragment>
            ))}
          </TableBody>
        </Table>
      </Paper>
      <CountryDialog open={!!edit} country={edit?.country ?? null} onClose={(s) => { setEdit(null); if (s) refresh(); }} />
      <CountryInfoAddDialog country={addInfo} onClose={(s) => { setAddInfo(null); if (s) refresh(); }} />
      <CountryInfoEditDialog info={editInfo} onClose={(s) => { setEditInfo(null); if (s) refresh(); }} />
    </>
  );
}

// ---------------------------------------------------------------- Climate

/** tabClimate: tree_climate (ClimateTreeRenderer: type → "code : name" classes). */
function ClimateTab() {
  const qc = useQueryClient();
  const { data } = useQuery({ queryKey: ['dict-climate'], queryFn: () => api.get<ClimateTypeRow[]>('/api/dictionary/climate-types') });
  const [q, setQ] = useState('');
  const [add, setAdd] = useState(false);
  const rows = (data ?? []).filter((t) => !q || t.name.toLowerCase().includes(q) || t.classes.some((c) => c.name.toLowerCase().includes(q)));
  return (
    <>
      <Toolbar searchId="btn_searchClimate" addId="btn_addClimate" onAdd={() => setAdd(true)} onSearch={setQ} />
      <TreeList id="tree_climate" nodes={rows.map((t) => ({ key: `t${t.id}`, primary: t.name, secondary: t.desc, children: t.classes.map((c) => ({ key: `c${c.id}`, primary: `${c.code} : ${c.name}`, secondary: c.desc })) }))} />
      <ClimateDialog open={add} types={data ?? []} onClose={(s) => { setAdd(false); if (s) { qc.invalidateQueries({ queryKey: ['dict-climate'] }); qc.invalidateQueries({ queryKey: ['lookups'] }); } }} />
    </>
  );
}

// ---------------------------------------------------------------- Protected areas

/** tab_PA: grid_PA (PaItemRenderer, paging 10). */
function ProtectedAreaTab() {
  const qc = useQueryClient();
  const { data } = useQuery({ queryKey: ['dict-pa'], queryFn: () => api.get<ProtectedAreaDto[]>('/api/dictionary/protected-areas') });
  const [q, setQ] = useState('');
  const [page, setPage] = useState(0);
  const [add, setAdd] = useState(false);
  const [edit, setEdit] = useState<ProtectedAreaDto | null>(null);
  const refresh = () => { qc.invalidateQueries({ queryKey: ['dict-pa'] }); qc.invalidateQueries({ queryKey: ['lookups'] }); };
  const del = useDelete((id) => `/api/dictionary/protected-areas/${id}`, refresh);
  const rows = (data ?? []).filter((p) => !q || p.name.toLowerCase().includes(q));
  return (
    <>
      <Toolbar searchId="btn_PA" addId="btn_addPA" onAdd={() => setAdd(true)} onSearch={(v) => { setQ(v); setPage(0); }} />
      <Paper variant="outlined" sx={{ overflowX: 'auto' }}>
        <Table size="small" id="grid_PA">
          <TableHead><TableRow><TableCell>Name</TableCell><TableCell>Area (Ha)</TableCell><TableCell>Established</TableCell><TableCell>Location</TableCell><TableCell align="right">Actions</TableCell></TableRow></TableHead>
          <TableBody>
            {rows.slice(page * 10, page * 10 + 10).map((p) => (
              <TableRow key={p.id} hover>
                <TableCell>{p.name}</TableCell><TableCell>{p.area}</TableCell><TableCell>{p.established}</TableCell><TableCell>{p.location}</TableCell>
                <TableCell align="right"><RowActions label={p.name} onEdit={() => setEdit(p)} onDelete={() => del(p.id!, p.name)} /></TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        <TablePagination component="div" count={rows.length} page={page} rowsPerPage={10} rowsPerPageOptions={[10]} onPageChange={(_, p) => setPage(p)} />
      </Paper>
      <ProtectedAreaAddDialog open={add} onClose={(a) => { setAdd(false); if (a) refresh(); }} />
      <ProtectedAreaEditDialog pa={edit} onClose={(s) => { setEdit(null); if (s) refresh(); }} />
    </>
  );
}

// ---------------------------------------------------------------- Ecosystem / Service trees

/** tab_eco: tree_ecosystem (EcosystemTreeRenderer) + btn_addEcosystem / btn_addrealm / btn_addbiome / btn_addefg. */
function EcosystemTab() {
  const qc = useQueryClient();
  const { data } = useQuery({ queryKey: ['dict-realms'], queryFn: () => api.get<RealmRow[]>('/api/dictionary/realms') });
  const [q, setQ] = useState('');
  const [dialog, setDialog] = useState<'chain' | 'realm' | 'biome' | 'efg' | null>(null);
  const refresh = () => { qc.invalidateQueries({ queryKey: ['dict-realms'] }); qc.invalidateQueries({ queryKey: ['lookups'] }); qc.invalidateQueries({ queryKey: ['search-filters'] }); };
  const m = (code: string, name: string) => !q || code.toLowerCase().includes(q) || name.toLowerCase().includes(q);
  const nodes = (data ?? [])
    .map((r) => ({
      key: `r${r.id}`, primary: `${r.code} : (Realm) ${r.name}`, secondary: r.desc,
      children: r.biomes
        .map((b) => ({
          key: `b${b.id}`, primary: `${b.code} : (Biome) ${b.name}`, secondary: b.desc,
          children: b.efgs.filter((e) => m(e.code, e.name) || m(b.code, b.name) || m(r.code, r.name)).map((e) => ({ key: `e${e.id}`, primary: `${e.code} : (EFG) ${e.name}`, secondary: e.desc })),
        }))
        .filter((b) => b.children.length || m(b.primary, '') || m(r.code, r.name)),
    }))
    .filter((r) => r.children.length || m(r.primary, ''));
  const realms = (data ?? []).map((r) => ({ id: r.id, label: `${r.code} - ${r.name}` }));
  const biomes = (data ?? []).flatMap((r) => r.biomes.map((b) => ({ id: b.id, label: `${b.code} - ${b.name}` })));
  return (
    <>
      <Toolbar searchId="btn_searcheco" onSearch={setQ} extra={<>
        <Button id="btn_addEcosystem" variant="contained" startIcon={<Add />} onClick={() => setDialog('chain')}>Ecosystem</Button>
        <Button id="btn_addrealm" variant="outlined" onClick={() => setDialog('realm')}>Realm</Button>
        <Button id="btn_addbiome" variant="outlined" onClick={() => setDialog('biome')}>Biome</Button>
        <Button id="btn_addefg" variant="outlined" onClick={() => setDialog('efg')}>EFG</Button>
      </>} />
      <TreeList id="tree_ecosystem" nodes={nodes} expandAll={!!q} />
      <EcosystemChainDialog open={dialog === 'chain'} onClose={(s) => { setDialog(null); if (s) refresh(); }} />
      <CodeNameBatchDialog open={dialog === 'realm'} title="Add Realm Record" url="/api/dictionary/realms" addButtonId="btn_addrealm" limits={{ code: 10, name: 55, desc: 255 }} onClose={(s) => { setDialog(null); if (s) refresh(); }} />
      <CodeNameBatchDialog open={dialog === 'biome'} title="Add Biome Record" url="/api/dictionary/biomes" addButtonId="btn_addbiome" parentLabel="Realm" parents={realms} limits={{ code: 10, name: 55, desc: 1000 }} onClose={(s) => { setDialog(null); if (s) refresh(); }} />
      <CodeNameBatchDialog open={dialog === 'efg'} title="Add Ecosystem Functional Group" url="/api/dictionary/efgs" addButtonId="btn_addefg" parentLabel="Biome" parents={biomes} limits={{ code: 10, name: 55, desc: 255 }} onClose={(s) => { setDialog(null); if (s) refresh(); }} />
    </>
  );
}

/** tab_service: tree_service (ServiceTreeRenderer) + btn_addService / btn_addeser / btn_addesubserv. */
function ServiceTab() {
  const qc = useQueryClient();
  const { data } = useQuery({ queryKey: ['dict-eservices'], queryFn: () => api.get<EServiceRow[]>('/api/dictionary/eservices') });
  const [q, setQ] = useState('');
  const [dialog, setDialog] = useState<'chain' | 'es' | 'sub' | null>(null);
  const refresh = () => { qc.invalidateQueries({ queryKey: ['dict-eservices'] }); qc.invalidateQueries({ queryKey: ['lookups'] }); qc.invalidateQueries({ queryKey: ['search-filters'] }); };
  const m = (s: string) => !q || s.toLowerCase().includes(q);
  const nodes = (data ?? [])
    .map((s) => ({
      key: `s${s.id}`, primary: `${s.code} : ${s.name}`, secondary: s.desc,
      children: s.subservices.filter((x) => m(`${x.code} ${x.name}`) || m(`${s.code} ${s.name}`)).map((x) => ({ key: `x${x.id}`, primary: `${x.code} : ${x.name}`, secondary: x.desc })),
    }))
    .filter((s) => s.children.length || m(s.primary));
  return (
    <>
      <Toolbar searchId="btn_searchService" onSearch={setQ} extra={<>
        <Button id="btn_addService" variant="contained" startIcon={<Add />} onClick={() => setDialog('chain')}>Service</Button>
        <Button id="btn_addeser" variant="outlined" onClick={() => setDialog('es')}>E-service</Button>
        <Button id="btn_addesubserv" variant="outlined" onClick={() => setDialog('sub')}>E-sub-service</Button>
      </>} />
      <TreeList id="tree_service" nodes={nodes} expandAll={!!q} />
      <ServiceChainDialog open={dialog === 'chain'} onClose={(s) => { setDialog(null); if (s) refresh(); }} />
      <CodeNameBatchDialog open={dialog === 'es'} title="Add E-Service Record" url="/api/dictionary/eservices" addButtonId="btn_addserv" limits={{ code: 10, name: 125, desc: 255 }} onClose={(s) => { setDialog(null); if (s) refresh(); }} />
      <CodeNameBatchDialog open={dialog === 'sub'} title="Add E-Sub-Service Record" url="/api/dictionary/esubservices" addButtonId="btn_addbiome" parentLabel="E-service" parents={(data ?? []).map((s) => ({ id: s.id, label: `${s.code} - ${s.name}` }))} limits={{ code: 10, name: 125, desc: 255 }} onClose={(s) => { setDialog(null); if (s) refresh(); }} />
    </>
  );
}

interface Node { key: string; primary: string; secondary?: string; children?: Node[] }

/** Expandable tree used for climate, ecosystem and service (replaces zul <tree>). */
function TreeList({ id, nodes, expandAll }: { id: string; nodes: Node[]; expandAll?: boolean }) {
  const [open, setOpen] = useState<Record<string, boolean>>({});
  const render = (n: Node, depth: number): ReactNode => {
    const isOpen = expandAll || open[n.key];
    return (
      <Fragment key={n.key}>
        <ListItemButton sx={{ pl: 1 + depth * 3 }} onClick={() => n.children?.length && setOpen({ ...open, [n.key]: !open[n.key] })}>
          <Box sx={{ width: 28, display: 'flex' }}>{n.children?.length ? (isOpen ? <KeyboardArrowDown /> : <KeyboardArrowRight />) : null}</Box>
          <ListItemText primary={n.primary} secondary={n.secondary || undefined} primaryTypographyProps={{ fontWeight: depth === 0 ? 700 : 400 }} />
        </ListItemButton>
        {n.children?.length ? <Collapse in={!!isOpen} unmountOnExit>{n.children.map((c) => render(c, depth + 1))}</Collapse> : null}
      </Fragment>
    );
  };
  return (
    <Paper variant="outlined">
      <List id={id} dense disablePadding>
        {nodes.length ? nodes.map((n) => render(n, 0)) : <ListItemText sx={{ p: 2 }} primary="Nothing found." />}
      </List>
    </Paper>
  );
}

import {
  Alert, Autocomplete, Button, Dialog, DialogActions, DialogContent, DialogTitle, Grid, IconButton, Stack, Table, TableBody, TableCell,
  TableHead, TableRow, TextField, Tooltip,
} from '@mui/material';
import AddCircleOutline from '@mui/icons-material/AddCircleOutline';
import Delete from '@mui/icons-material/DeleteOutline';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import type { ClimateTypeRow, CountryInfoRow, CountryRow, Ref, TermRow } from '../../api/types';
import { RefSelect } from '../../components/RefSelect';
import { errorMessage, useNotify } from '../../components/Notifier';
import { TermDialog } from './TermDialog';

// ======================================================================= Country

/** AddCountryDialog.zul → CountryController: add (no arg) or edit (arg "country"); btn_addCurr opens AddCurrencyDialog. */
export function CountryDialog({ open, country, onClose }: { open: boolean; country: CountryRow | null; onClose: (saved: boolean) => void }) {
  const notify = useNotify();
  const qc = useQueryClient();
  const currencies = useQuery({ queryKey: ['terms', 'Currency'], queryFn: () => api.get<TermRow[]>('/api/dictionary/terms?cv=Currency'), enabled: open });
  const [f, setF] = useState<{ code: string; name: string; currency: Ref | null }>({ code: '', name: '', currency: null });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [currOpen, setCurrOpen] = useState(false);
  useEffect(() => {
    if (open) {
      setF(country ? { code: country.code, name: country.name, currency: country.currency } : { code: '', name: '', currency: null });
      setErrors({});
    }
  }, [open, country]);

  const save = async () => {
    const e: Record<string, string> = {};
    if (!f.code.trim()) e.code = 'Code is required (up to 5 characters).';
    if (!f.name.trim()) e.name = 'Name is required (up to 25 characters).';
    setErrors(e);
    if (Object.keys(e).length) return;
    try {
      if (country) await api.put(`/api/dictionary/countries/${country.id}`, f);
      else await api.post('/api/dictionary/countries', f);
      onClose(true);
    } catch (err) {
      notify(errorMessage(err), 'error');
    }
  };

  const options: Ref[] = (currencies.data ?? []).map((t) => ({ id: t.id, code: t.code, name: t.name }));
  return (
    <Dialog open={open} onClose={() => onClose(false)} maxWidth="sm" fullWidth>
      <DialogTitle>{country ? 'Edit Country' : 'Add Country Record'}</DialogTitle>
      <DialogContent dividers>
        <Stack spacing={2}>
          <TextField id="txtbox_code" label="Code" required value={f.code} onChange={(e) => setF({ ...f, code: e.target.value })} error={!!errors.code} helperText={errors.code} inputProps={{ maxLength: 5 }} />
          <TextField id="txtbox_name" label="Country name" required value={f.name} onChange={(e) => setF({ ...f, name: e.target.value })} error={!!errors.name} helperText={errors.name} inputProps={{ maxLength: 25 }} />
          <Stack direction="row" alignItems="center" spacing={0.5}>
            <RefSelect id="combobox_curr" label="Currency" options={options} value={f.currency} onChange={(v) => setF({ ...f, currency: v })} />
            <Tooltip title="Add a currency"><IconButton id="btn_addCurr" aria-label="Add a currency" onClick={() => setCurrOpen(true)}><AddCircleOutline /></IconButton></Tooltip>
          </Stack>
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button id="cancelButton" onClick={() => onClose(false)}>Cancel</Button>
        <Button id="buttonSave" variant="contained" onClick={save}>Save</Button>
      </DialogActions>
      <TermDialog open={currOpen} cv="Currency" title="Currency" term={null} onClose={async (t) => {
        setCurrOpen(false);
        if (t) {
          await qc.invalidateQueries({ queryKey: ['terms', 'Currency'] });
          setF((x) => ({ ...x, currency: { id: t.id, code: t.code, name: t.name } }));
        }
      }} />
    </Dialog>
  );
}

const emptyInfo = { year: '', wbIncome: '', gdpPerCapita: '', popDensity: '' };
type InfoForm = typeof emptyInfo;

function InfoFields({ f, set, error }: { f: InfoForm; set: (f: InfoForm) => void; error?: string }) {
  return (
    <Grid container spacing={2}>
      <Grid item xs={12} sm={3}><TextField id="txtbox_year" label="Year" required value={f.year} onChange={(e) => set({ ...f, year: e.target.value })} error={!!error} helperText={error} inputProps={{ maxLength: 4 }} /></Grid>
      <Grid item xs={12} sm={3}><TextField id="txtbox_wbINcome" label="WB income group" value={f.wbIncome} onChange={(e) => set({ ...f, wbIncome: e.target.value })} inputProps={{ maxLength: 55 }} /></Grid>
      <Grid item xs={12} sm={3}><TextField id="txtbox_gdp" label="GDP per capita" value={f.gdpPerCapita} onChange={(e) => set({ ...f, gdpPerCapita: e.target.value })} inputProps={{ maxLength: 55 }} /></Grid>
      <Grid item xs={12} sm={3}><TextField id="txtbox_popDen" label="Population density" value={f.popDensity} onChange={(e) => set({ ...f, popDensity: e.target.value })} inputProps={{ maxLength: 55 }} /></Grid>
    </Grid>
  );
}
const YEAR = /^(19|20)\d{2}$/;

/** AddCountryInfoDialog.zul → CountryInfoController: btn_addCi stages a year ("Record already exists." on duplicates), Save persists all. */
export function CountryInfoAddDialog({ country, onClose }: { country: CountryRow | null; onClose: (saved: boolean) => void }) {
  const notify = useNotify();
  const [f, setF] = useState(emptyInfo);
  const [staged, setStaged] = useState<InfoForm[]>([]);
  const [err, setErr] = useState<string | undefined>();
  useEffect(() => {
    setF(emptyInfo);
    setStaged([]);
    setErr(undefined);
  }, [country]);
  const add = () => {
    if (!YEAR.test(f.year)) return setErr('Year must be 19xx or 20xx.');
    if (staged.some((s) => s.year === f.year) || country?.infos.some((i) => String(i.year) === f.year)) return setErr('Record already exists.');
    setStaged([...staged, f].sort((a, b) => a.year.localeCompare(b.year)));
    setF(emptyInfo);
    setErr(undefined);
  };
  const save = async () => {
    try {
      await api.post(`/api/dictionary/countries/${country!.id}/infos`, { items: staged });
      onClose(true);
    } catch (e) {
      notify(errorMessage(e), 'error');
    }
  };
  return (
    <Dialog open={!!country} onClose={() => onClose(false)} maxWidth="md" fullWidth>
      <DialogTitle>Add information per year: {country?.name}</DialogTitle>
      <DialogContent dividers>
        <InfoFields f={f} set={setF} error={err} />
        <Button id="btn_addCi" variant="outlined" sx={{ mt: 2 }} onClick={add}>Add to list</Button>
        {staged.length > 0 && (
          <Table size="small" sx={{ mt: 2 }} id="grid_ci">
            <TableHead><TableRow><TableCell /><TableCell>Year</TableCell><TableCell>WB income</TableCell><TableCell>GDP per capita</TableCell><TableCell>Population density</TableCell></TableRow></TableHead>
            <TableBody>
              {staged.map((s, i) => (
                <TableRow key={s.year}>
                  <TableCell padding="checkbox"><IconButton size="small" aria-label="Remove" onClick={() => setStaged(staged.filter((_, j) => j !== i))}><Delete fontSize="small" /></IconButton></TableCell>
                  <TableCell>{s.year}</TableCell><TableCell>{s.wbIncome}</TableCell><TableCell>{s.gdpPerCapita}</TableCell><TableCell>{s.popDensity}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </DialogContent>
      <DialogActions>
        <Button id="cancelButton" onClick={() => onClose(false)}>Cancel</Button>
        <Button id="buttonSave" variant="contained" disabled={!staged.length} onClick={save}>Save ({staged.length})</Button>
      </DialogActions>
    </Dialog>
  );
}

/** EditCountryInfoDialog.zul → EditCountryInfoController. */
export function CountryInfoEditDialog({ info, onClose }: { info: CountryInfoRow | null; onClose: (saved: boolean) => void }) {
  const notify = useNotify();
  const [f, setF] = useState(emptyInfo);
  const [err, setErr] = useState<string | undefined>();
  useEffect(() => {
    if (info) setF({ year: String(info.year), wbIncome: info.wbIncome ?? '', gdpPerCapita: info.gdpPerCapita ?? '', popDensity: info.popDensity ?? '' });
    setErr(undefined);
  }, [info]);
  const save = async () => {
    if (!YEAR.test(f.year)) return setErr('Year must be 19xx or 20xx.');
    try {
      await api.put(`/api/dictionary/country-infos/${info!.id}`, f);
      onClose(true);
    } catch (e) {
      notify(errorMessage(e), 'error');
    }
  };
  return (
    <Dialog open={!!info} onClose={() => onClose(false)} maxWidth="md" fullWidth>
      <DialogTitle>Edit information for {info?.year}</DialogTitle>
      <DialogContent dividers><InfoFields f={f} set={setF} error={err} /></DialogContent>
      <DialogActions>
        <Button id="cancelButton" onClick={() => onClose(false)}>Cancel</Button>
        <Button id="buttonSave" variant="contained" onClick={save}>Save</Button>
      </DialogActions>
    </Dialog>
  );
}

// ======================================================================= Climate

/** AddClimateDialog.zul → ClimateController: combobox_name (existing or new type, onSelect fills description), btn_addClass stages classes, Save. */
export function ClimateDialog({ open, types, onClose }: { open: boolean; types: ClimateTypeRow[]; onClose: (saved: boolean) => void }) {
  const notify = useNotify();
  const [typeName, setTypeName] = useState('');
  const [typeDesc, setTypeDesc] = useState('');
  const [c, setC] = useState({ code: '', name: '', desc: '' });
  const [classes, setClasses] = useState<typeof c[]>([]);
  const [err, setErr] = useState<string | null>(null);
  useEffect(() => {
    if (open) {
      setTypeName('');
      setTypeDesc('');
      setC({ code: '', name: '', desc: '' });
      setClasses([]);
      setErr(null);
    }
  }, [open]);
  const existing = types.find((t) => t.name.toLowerCase() === typeName.trim().toLowerCase());
  const addClass = () => {
    if (!c.code.trim() || !c.name.trim()) return setErr('Class code and name are required.');
    const lc = c.name.trim().toLowerCase();
    if (classes.some((x) => x.name.toLowerCase() === lc) || existing?.classes.some((x) => x.name.toLowerCase() === lc)) return setErr('Climate class already exists.');
    setClasses([...classes, c]);
    setC({ code: '', name: '', desc: '' });
    setErr(null);
  };
  const save = async () => {
    if (!typeName.trim()) return setErr('Climate type name is required.');
    try {
      await api.post('/api/dictionary/climate-types', { typeName, typeDesc, classes });
      onClose(true);
    } catch (e) {
      notify(errorMessage(e), 'error');
    }
  };
  return (
    <Dialog open={open} onClose={() => onClose(false)} maxWidth="md" fullWidth>
      <DialogTitle>Add Climate Record</DialogTitle>
      <DialogContent dividers>
        {err && <Alert severity="warning" sx={{ mb: 2 }}>{err}</Alert>}
        <Grid container spacing={2}>
          <Grid item xs={12} sm={5}>
            <Autocomplete id="combobox_name" freeSolo options={types.map((t) => t.name)} inputValue={typeName}
              onInputChange={(_, v) => setTypeName(v)}
              onChange={(_, v) => { const t = types.find((x) => x.name === v); if (t) setTypeDesc(t.desc ?? ''); }}
              renderInput={(p) => <TextField {...p} label="Climate type" required inputProps={{ ...p.inputProps, maxLength: 50 }} helperText={existing ? 'Existing type: classes will be added to it.' : typeName ? 'A new type will be created.' : ' '} />} />
          </Grid>
          <Grid item xs={12} sm={7}><TextField id="txtbox_desc" label="Type description" value={typeDesc} onChange={(e) => setTypeDesc(e.target.value)} inputProps={{ maxLength: 255 }} /></Grid>
          <Grid item xs={12} sm={3}><TextField id="txtbox_climateclasscode" label="Class code" value={c.code} onChange={(e) => setC({ ...c, code: e.target.value })} inputProps={{ maxLength: 10 }} /></Grid>
          <Grid item xs={12} sm={4}><TextField id="txtbox_climateclassname" label="Class name" value={c.name} onChange={(e) => setC({ ...c, name: e.target.value })} inputProps={{ maxLength: 100 }} /></Grid>
          <Grid item xs={12} sm={5}><TextField id="txtbox_climateclassdesc" label="Class description" value={c.desc} onChange={(e) => setC({ ...c, desc: e.target.value })} inputProps={{ maxLength: 255 }} /></Grid>
        </Grid>
        <Button id="btn_addClass" variant="outlined" sx={{ mt: 2 }} onClick={addClass}>Add class</Button>
        {classes.length > 0 && (
          <Table size="small" sx={{ mt: 2 }} id="grid_climateClass">
            <TableHead><TableRow><TableCell /><TableCell>Code</TableCell><TableCell>Name</TableCell><TableCell>Description</TableCell></TableRow></TableHead>
            <TableBody>
              {classes.map((x, i) => (
                <TableRow key={x.name}>
                  <TableCell padding="checkbox"><IconButton size="small" aria-label="Remove" onClick={() => setClasses(classes.filter((_, j) => j !== i))}><Delete fontSize="small" /></IconButton></TableCell>
                  <TableCell>{x.code}</TableCell><TableCell>{x.name}</TableCell><TableCell>{x.desc}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </DialogContent>
      <DialogActions>
        <Button id="cancelButton" onClick={() => onClose(false)}>Cancel</Button>
        <Button id="buttonSave" variant="contained" onClick={save}>Save</Button>
      </DialogActions>
    </Dialog>
  );
}

// ======================================================================= Chains

/** AddEcosystemDialog.zul → EcosystemController: find-or-create realm → biome → EFG by code. */
export function EcosystemChainDialog({ open, onClose }: { open: boolean; onClose: (saved: boolean) => void }) {
  const notify = useNotify();
  const empty = { realmCode: '', realmName: '', biomeCode: '', biomeName: '', efgCode: '', efgName: '' };
  const [f, setF] = useState(empty);
  useEffect(() => { if (open) setF(empty); }, [open]); // eslint-disable-line react-hooks/exhaustive-deps
  const save = async () => {
    if (!f.realmCode.trim()) return notify('Realm code is required.', 'warning');
    try {
      await api.post('/api/dictionary/ecosystems', f);
      onClose(true);
    } catch (e) {
      notify(errorMessage(e), 'error');
    }
  };
  const tf = (k: keyof typeof empty, id: string, label: string) => (
    <TextField id={id} label={label} value={f[k]} onChange={(e) => setF({ ...f, [k]: e.target.value })} />
  );
  return (
    <Dialog open={open} onClose={() => onClose(false)} maxWidth="sm" fullWidth>
      <DialogTitle>Add Ecosystem Record</DialogTitle>
      <DialogContent dividers>
        <Alert severity="info" sx={{ mb: 2 }}>Existing codes are reused; new codes create the realm, biome or functional group.</Alert>
        <Grid container spacing={2}>
          <Grid item xs={4}>{tf('realmCode', 'cmbobox_realmCode', 'Realm code *')}</Grid><Grid item xs={8}>{tf('realmName', 'cmbobox_realmName', 'Realm name')}</Grid>
          <Grid item xs={4}>{tf('biomeCode', 'cmbobox_biomeCode', 'Biome code')}</Grid><Grid item xs={8}>{tf('biomeName', 'cmbobox_biomeName', 'Biome name')}</Grid>
          <Grid item xs={4}>{tf('efgCode', 'cmbobox_efgCode', 'EFG code')}</Grid><Grid item xs={8}>{tf('efgName', 'cmbobox_efgName', 'EFG name')}</Grid>
        </Grid>
      </DialogContent>
      <DialogActions>
        <Button id="cancelButton" onClick={() => onClose(false)}>Cancel</Button>
        <Button id="buttonSave" variant="contained" onClick={save}>Save</Button>
      </DialogActions>
    </Dialog>
  );
}

/** AddServiceDialog.zul → ServiceController: find-or-create e-service → e-sub-service by code. */
export function ServiceChainDialog({ open, onClose }: { open: boolean; onClose: (saved: boolean) => void }) {
  const notify = useNotify();
  const empty = { serviceCode: '', serviceName: '', subServiceCode: '', subServiceName: '', desc: '' };
  const [f, setF] = useState(empty);
  useEffect(() => { if (open) setF(empty); }, [open]); // eslint-disable-line react-hooks/exhaustive-deps
  const save = async () => {
    if (!f.serviceCode.trim()) return notify('Service code is required.', 'warning');
    try {
      await api.post('/api/dictionary/services', f);
      onClose(true);
    } catch (e) {
      notify(errorMessage(e), 'error');
    }
  };
  const tf = (k: keyof typeof empty, id: string, label: string, max: number) => (
    <TextField id={id} label={label} value={f[k]} onChange={(e) => setF({ ...f, [k]: e.target.value })} inputProps={{ maxLength: max }} />
  );
  return (
    <Dialog open={open} onClose={() => onClose(false)} maxWidth="sm" fullWidth>
      <DialogTitle>Add Service Record</DialogTitle>
      <DialogContent dividers>
        <Grid container spacing={2}>
          <Grid item xs={4}>{tf('serviceCode', 'cmbo_serviceCode', 'Service code *', 10)}</Grid><Grid item xs={8}>{tf('serviceName', 'cmbo_serviceName', 'Service name', 125)}</Grid>
          <Grid item xs={4}>{tf('subServiceCode', 'cmbo_subServiceCode', 'Sub-service code', 10)}</Grid><Grid item xs={8}>{tf('subServiceName', 'cmbo_subServiceName', 'Sub-service name', 125)}</Grid>
          <Grid item xs={12}>{tf('desc', 'txtbox_desc', 'Description', 255)}</Grid>
        </Grid>
      </DialogContent>
      <DialogActions>
        <Button id="cancelButton" onClick={() => onClose(false)}>Cancel</Button>
        <Button id="buttonSave" variant="contained" onClick={save}>Save</Button>
      </DialogActions>
    </Dialog>
  );
}

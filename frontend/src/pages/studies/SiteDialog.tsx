import {
  Accordion, AccordionDetails, AccordionSummary, Alert, Autocomplete, Button, Dialog, DialogActions, DialogContent, DialogTitle, Grid,
  IconButton, Link, MenuItem, Stack, TextField, Tooltip, Typography,
} from '@mui/material';
import ExpandMore from '@mui/icons-material/ExpandMore';
import AddCircleOutline from '@mui/icons-material/AddCircleOutline';
import { useEffect, useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { useLookups } from '../../api/hooks';
import type { CountryRow, ProtectedAreaDto, Ref, SiteDto } from '../../api/types';
import { MultiRefSelect, RefSelect } from '../../components/RefSelect';
import { dmsToDecimal, emptySite, validateSite, type Errors } from './studyGraph';
import { ProtectedAreaAddDialog } from '../dictionary/ProtectedAreaDialogs';

type Panel = 'site' | 'ci' | 'attr';
const fieldPanel: Record<string, Panel> = { name: 'site', latitude: 'site', longitude: 'site', country: 'ci', scale: 'attr' };

/**
 * AddSiteDialog.zul → AddSiteController (window addSiteWindow, accordion tab_site / tab_ci / tab_attr).
 * Add when `site` is null, edit (arg "myData") otherwise. Returns the site to the parent; nothing is saved here.
 */
export function SiteDialog({ open, site, onClose }: { open: boolean; site: SiteDto | null; onClose: (s: SiteDto | null) => void }) {
  const { data: lk } = useLookups();
  const qc = useQueryClient();
  const [s, setS] = useState<SiteDto>(emptySite());
  const [country, setCountry] = useState<CountryRow | null>(null);
  const [dms, setDms] = useState({ d: '', m: '', s: '', d2: '', m2: '', s2: '' });
  const [panel, setPanel] = useState<Panel>('site');
  const [errors, setErrors] = useState<Errors>({});
  const [paOpen, setPaOpen] = useState(false);

  useEffect(() => {
    if (!open) return;
    const base = site ? structuredClone(site) : emptySite();
    setS(base);
    setCountry(lk?.countries.find((c) => c.id === base.location?.countryInfo?.countryId) ?? null);
    setDms({ d: '', m: '', s: '', d2: '', m2: '', s2: '' });
    setPanel('site');
    setErrors({});
  }, [open, site, lk]);

  if (!lk) return null;
  const loc = s.location ?? { id: null, name: '', countryInfo: null, protectedStatus: null, protectedArea: null };
  const setLoc = (patch: Partial<typeof loc>) => setS({ ...s, location: { ...loc, ...patch } });
  const info = country?.infos.find((i) => i.id === loc.countryInfo?.id) ?? null;
  const othersAttr = lk.siteAttributes.find((a) => (a.name ?? '').toLowerCase().startsWith('other'));
  const hasOthers = !!othersAttr && s.attributes.some((a) => !a.deleted && a.attribute?.id === othersAttr.id);

  /** combobox_country onChange: reset year/GDP/WB/pop, fill years, auto-select the first year. */
  const onCountry = (c: CountryRow | null) => {
    setCountry(c);
    const first = c?.infos[0];
    setLoc({ countryInfo: c ? { id: first?.id ?? null, countryId: c.id, countryName: c.name, year: first?.year ?? null } : null });
  };

  /** txtbox_d/m/s (latitude) and d2/m2/s2 (longitude) onChange → calculate(). */
  const onDms = (k: keyof typeof dms, v: string) => {
    const next = { ...dms, [k]: v };
    setDms(next);
    if (k.endsWith('2')) setS({ ...s, longitude: dmsToDecimal(next.d2, next.m2, next.s2) });
    else setS({ ...s, latitude: dmsToDecimal(next.d, next.m, next.s) });
  };

  /** listbox_attr onSelect (+ grid_others visibility, cleared when hidden). */
  const onAttributes = (refs: Ref[]) =>
    setS({ ...s, attributes: refs.map((r) => ({ id: null, deleted: false, attribute: r, otherValue: r.id === othersAttr?.id ? (s.attributes.find((a) => a.attribute?.id === r.id)?.otherValue ?? '') : '' })) });

  /** buttonSave: validate in ZK order, open the tab of the first error. */
  const save = () => {
    const e = validateSite({ ...s, location: { ...loc, countryInfo: country ? { ...(loc.countryInfo ?? { id: null }), countryId: country.id } : null } });
    setErrors(e);
    const first = Object.keys(e)[0];
    if (first) {
      setPanel(fieldPanel[first] ?? 'site');
      return;
    }
    onClose({ ...s, location: { ...loc, countryInfo: country ? { ...(loc.countryInfo ?? { id: null }), countryId: country.id, countryName: country.name } : null } });
  };

  const section = (id: Panel, title: string, body: JSX.Element) => (
    <Accordion expanded={panel === id} onChange={() => setPanel(id)} disableGutters>
      <AccordionSummary id={`tab_${id}`} expandIcon={<ExpandMore />}><Typography sx={{ fontWeight: 700 }}>{title}</Typography></AccordionSummary>
      <AccordionDetails>{body}</AccordionDetails>
    </Accordion>
  );

  return (
    <Dialog open={open} onClose={() => onClose(null)} maxWidth="md" fullWidth>
      <DialogTitle>{site ? 'Edit Site' : 'Add Site'}</DialogTitle>
      <DialogContent dividers>
        {!!Object.keys(errors).length && <Alert severity="error" sx={{ mb: 2 }}>Please correct the highlighted fields.</Alert>}
        {section('site', 'Site', (
          <Grid container spacing={2}>
            <Grid item xs={12} sm={6}><TextField id="txtbox_site" label="Site name" required value={s.name} onChange={(e) => setS({ ...s, name: e.target.value })} error={!!errors.name} helperText={errors.name} inputProps={{ maxLength: 255 }} /></Grid>
            <Grid item xs={12} sm={6}><TextField id="txtbox_municipality" label="Location (municipality / province)" value={loc.name ?? ''} onChange={(e) => setLoc({ name: e.target.value })} /></Grid>
            <Grid item xs={12} sm={6}>
              <TextField id="txtbox_latitude" label="Latitude (decimal)" required value={s.latitude} onChange={(e) => setS({ ...s, latitude: e.target.value })} error={!!errors.latitude} helperText={errors.latitude} inputProps={{ maxLength: 24 }} />
              <Stack direction="row" spacing={1} sx={{ mt: 1 }}>
                <TextField id="txtbox_d" label="Deg" value={dms.d} onChange={(e) => onDms('d', e.target.value)} />
                <TextField id="txtbox_m" label="Min" value={dms.m} onChange={(e) => onDms('m', e.target.value)} />
                <TextField id="txtbox_s" label="Sec" value={dms.s} onChange={(e) => onDms('s', e.target.value)} />
              </Stack>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField id="txtbox_longitude" label="Longitude (decimal)" required value={s.longitude} onChange={(e) => setS({ ...s, longitude: e.target.value })} error={!!errors.longitude} helperText={errors.longitude} inputProps={{ maxLength: 24 }} />
              <Stack direction="row" spacing={1} sx={{ mt: 1 }}>
                <TextField id="txtbox_d2" label="Deg" value={dms.d2} onChange={(e) => onDms('d2', e.target.value)} />
                <TextField id="txtbox_m2" label="Min" value={dms.m2} onChange={(e) => onDms('m2', e.target.value)} />
                <TextField id="txtbox_s2" label="Sec" value={dms.s2} onChange={(e) => onDms('s2', e.target.value)} />
              </Stack>
            </Grid>
            <Grid item xs={12}>
              <Typography variant="caption" color="text.secondary">
                Degrees/minutes/seconds fill the decimal value (negative degrees for south/west). Converter:{' '}
                <Link href="https://www.latlong.net/degrees-minutes-seconds-to-decimal-degrees" target="_blank" rel="noopener">latlong.net</Link>
              </Typography>
            </Grid>
            <Grid item xs={12}>
              <MultiRefSelect id="listbox_climate" label="Climate type" showCode={false}
                options={lk.climateTypes.map((c) => ({ id: c.id, code: null, name: c.name }))}
                value={s.climates.filter((c) => !c.deleted).map((c) => c.climateType!).filter(Boolean)}
                onChange={(refs) => setS({ ...s, climates: refs.map((r) => ({ id: null, deleted: false, climateType: r, climateClass: null, description: null })) })} />
              <Typography variant="caption" color="text.secondary">
                Reference: <Link href="https://climateknowledgeportal.worldbank.org" target="_blank" rel="noopener">World Bank Climate Knowledge Portal</Link>
              </Typography>
            </Grid>
          </Grid>
        ))}
        {section('ci', 'Country information', (
          <Grid container spacing={2}>
            <Grid item xs={12} sm={6}>
              <Autocomplete id="combobox_country" options={lk.countries} value={country} onChange={(_, c) => onCountry(c)} getOptionLabel={(c) => c.name}
                isOptionEqualToValue={(a, b) => a.id === b.id} renderInput={(p) => <TextField {...p} label="Country" required error={!!errors.country} helperText={errors.country} />} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField id="combobox_countryYear" select label="Year" value={loc.countryInfo?.id ?? ''} disabled={!country?.infos.length}
                helperText={country && !country.infos.length ? 'No yearly information for this country.' : ' '}
                onChange={(e) => {
                  const i = country?.infos.find((x) => x.id === Number(e.target.value));
                  setLoc({ countryInfo: { id: i?.id ?? null, countryId: country?.id, countryName: country?.name, year: i?.year } });
                }}>
                {(country?.infos ?? []).map((i) => <MenuItem key={i.id} value={i.id}>{i.year}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={12} sm={4}><TextField id="txtbox_popDensity" label="Population density" value={info?.popDensity ?? ''} InputProps={{ readOnly: true }} /></Grid>
            <Grid item xs={12} sm={4}><TextField id="txtbox_gdp" label="GDP per capita" value={info?.gdpPerCapita ?? ''} InputProps={{ readOnly: true }} /></Grid>
            <Grid item xs={12} sm={4}><TextField id="txtbox_wbIncome" label="WB income group" value={info?.wbIncome ?? ''} InputProps={{ readOnly: true }} /></Grid>
            <Grid item xs={12}>
              <Typography variant="caption" color="text.secondary">
                Source: <Link href="https://data.worldbank.org/country/" target="_blank" rel="noopener">World Bank country data</Link>
              </Typography>
            </Grid>
          </Grid>
        ))}
        {section('attr', 'Attributes', (
          <Grid container spacing={2}>
            <Grid item xs={12}>
              <MultiRefSelect id="listbox_attr" label="Site attributes" options={lk.siteAttributes} showCode={false}
                value={s.attributes.filter((a) => !a.deleted).map((a) => a.attribute!).filter(Boolean)} onChange={onAttributes} />
            </Grid>
            {hasOthers && (
              <Grid item xs={12} id="grid_others">
                <TextField id="txtboxAttrOthers" label="Others (specify)" value={s.attributes.find((a) => a.attribute?.id === othersAttr!.id)?.otherValue ?? ''}
                  onChange={(e) => setS({ ...s, attributes: s.attributes.map((a) => (a.attribute?.id === othersAttr!.id ? { ...a, otherValue: e.target.value } : a)) })} />
              </Grid>
            )}
            <Grid item xs={12} sm={6}>
              <RefSelect id="combobox_status" label="Protected status" options={lk.protectedStatuses} value={loc.protectedStatus} onChange={(v) => setLoc({ protectedStatus: v })} showCode={false} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <Stack direction="row" alignItems="flex-start" spacing={0.5}>
                <Autocomplete id="combobox_PA" sx={{ flex: 1 }} options={lk.protectedAreas} value={lk.protectedAreas.find((p) => p.id === loc.protectedArea?.id) ?? null}
                  onChange={(_, p) => setLoc({ protectedArea: p })} getOptionLabel={(p: ProtectedAreaDto) => p.name} isOptionEqualToValue={(a, b) => a.id === b.id}
                  renderInput={(p) => <TextField {...p} label="Protected area" />} />
                <Tooltip title="Add a protected area">
                  <IconButton id="addBtn_Pa" onClick={() => setPaOpen(true)} aria-label="Add a protected area"><AddCircleOutline /></IconButton>
                </Tooltip>
              </Stack>
            </Grid>
            <Grid item xs={12} sm={4}>
              <RefSelect id="combobox_scale" label="Scale" required options={lk.scales} value={s.scale} onChange={(v) => setS({ ...s, scale: v })} error={!!errors.scale} helperText={errors.scale} showCode={false} />
            </Grid>
            <Grid item xs={6} sm={4}><TextField id="txtbox_area" label="Area" value={s.area ?? ''} onChange={(e) => setS({ ...s, area: e.target.value })} /></Grid>
            <Grid item xs={6} sm={4}><TextField id="txtbox_unit" label="Area unit" value={s.areaUnit ?? ''} onChange={(e) => setS({ ...s, areaUnit: e.target.value })} /></Grid>
          </Grid>
        ))}
      </DialogContent>
      <DialogActions>
        <Button id="cancelButton" onClick={() => onClose(null)}>Cancel</Button>
        <Button id="buttonSave" variant="contained" onClick={save}>Save</Button>
      </DialogActions>
      <ProtectedAreaAddDialog open={paOpen} onClose={async (added) => {
        setPaOpen(false);
        if (added?.length) {
          await qc.invalidateQueries({ queryKey: ['lookups'] });
          setLoc({ protectedArea: added[0] });
        }
      }} />
    </Dialog>
  );
}

import {
  Accordion, AccordionDetails, AccordionSummary, Alert, Badge, Button, Dialog, DialogActions, DialogContent, DialogTitle, Grid, IconButton,
  Tab, Table, TableBody, TableCell, TableHead, TableRow, Tabs, TextField, Typography,
} from '@mui/material';
import ExpandMore from '@mui/icons-material/ExpandMore';
import Close from '@mui/icons-material/Close';
import { useEffect, useState } from 'react';
import { useLookups } from '../../api/hooks';
import type { DatasetDto, Lookups, Ref } from '../../api/types';
import { MultiRefSelect, RefSelect } from '../../components/RefSelect';
import { useConfirm } from '../../components/ConfirmDialog';
import { useNotify } from '../../components/Notifier';
import { emptyDataset, toNumber, validateDataset, type Errors } from './studyGraph';

type Section = 'sf' | 'valMeth' | 'values';
const sectionOf: Record<string, Section> = {
  realmDesc: 'sf', biomeDesc: 'sf', efgDesc: 'sf', serviceDesc: 'sf', subserviceDesc: 'sf',
  valuedEq: 'valMeth', surveyStudyInfo: 'valMeth',
};

/** Numeric inputs are kept as text while typing (txtbox_Aggregate etc.), converted on save. */
type NumText = { aggregate: string; rangeLow: string; rangeHigh: string; beneficiaryNumber: string };
const numText = (d: DatasetDto): NumText => ({
  aggregate: d.value?.aggregate == null ? '' : String(d.value.aggregate),
  rangeLow: d.value?.rangeLow == null ? '' : String(d.value.rangeLow),
  rangeHigh: d.value?.rangeHigh == null ? '' : String(d.value.rangeHigh),
  beneficiaryNumber: d.value?.beneficiaryNumber == null ? '' : String(d.value.beneficiaryNumber),
});

function withNumbers(d: DatasetDto, n: NumText): DatasetDto {
  return { ...d, value: { ...d.value!, aggregate: toNumber(n.aggregate), rangeLow: toNumber(n.rangeLow), rangeHigh: toNumber(n.rangeHigh), beneficiaryNumber: toNumber(n.beneficiaryNumber) } };
}

/** Validation sees the raw numeric text so "abc" is rejected instead of silently becoming null. */
function check(d: DatasetDto, n: NumText): Errors {
  const probe = { ...d, value: { ...d.value!, ...(n as unknown as Record<string, number>) } } as DatasetDto;
  return validateDataset(probe);
}

/**
 * The three accordion sections shared by AddSiteValuationDialog.zul and EditSiteValuationDialog.zul:
 * tab_sf (study focus), tab_valMeth (valuation method), tab_values (values).
 */
function ValuationFields({ lk, d, setD, n, setN, errors, section, setSection }: {
  lk: Lookups; d: DatasetDto; setD: (d: DatasetDto) => void; n: NumText; setN: (n: NumText) => void; errors: Errors; section: Section; setSection: (s: Section) => void;
}) {
  const eco = d.ecosystem!;
  const sv = d.service!;
  const m = d.method!;
  const v = d.value!;
  const realm = lk.realms.find((r) => r.id === eco.realm?.id);
  const biome = realm?.biomes.find((b) => b.id === eco.biome?.id);
  const es = lk.eservices.find((x) => x.id === sv.eservice?.id);
  const toRef = (x: { id: number; code: string; name: string }): Ref => ({ id: x.id, code: x.code, name: x.name });
  const setEco = (p: Partial<typeof eco>) => setD({ ...d, ecosystem: { ...eco, ...p } });
  const setSv = (p: Partial<typeof sv>) => setD({ ...d, service: { ...sv, ...p } });
  const setM = (p: Partial<typeof m>) => setD({ ...d, method: { ...m, ...p } });
  const setV = (p: Partial<typeof v>) => setD({ ...d, value: { ...v, ...p } });
  const text = (id: string, label: string, value: string | null | undefined, onChange: (s: string) => void, key: string, max?: number, multiline = false) => (
    <TextField id={id} label={label} value={value ?? ''} onChange={(e) => onChange(e.target.value)} error={!!errors[key]} helperText={errors[key]}
      inputProps={max ? { maxLength: max } : undefined} multiline={multiline} minRows={multiline ? 2 : undefined} />
  );
  const sec = (id: Section, title: string, body: JSX.Element) => (
    <Accordion expanded={section === id} onChange={() => setSection(id)} disableGutters>
      <AccordionSummary id={`tab_${id}`} expandIcon={<ExpandMore />}><Typography sx={{ fontWeight: 700 }}>{title}</Typography></AccordionSummary>
      <AccordionDetails>{body}</AccordionDetails>
    </Accordion>
  );

  return (
    <>
      {sec('sf', 'Study focus', (
        <Grid container spacing={2}>
          {/* combobox_realm onSelect → rebuild biomes → biomeSelect → rebuild EFGs */}
          <Grid item xs={12} sm={5}><RefSelect id="combobox_realm" label="Realm" options={lk.realms.map(toRef)} value={eco.realm} onChange={(r) => setEco({ realm: r, biome: null, efg: null })} /></Grid>
          <Grid item xs={12} sm={7}>{text('txtbox_realmdesc', 'Realm description', eco.realmDesc, (x) => setEco({ realmDesc: x }), 'realmDesc', 1000)}</Grid>
          <Grid item xs={12} sm={5}><RefSelect id="combobox_biome" label="Biome" options={(realm?.biomes ?? []).map(toRef)} value={eco.biome} disabled={!realm} onChange={(b) => setEco({ biome: b, efg: null })} /></Grid>
          <Grid item xs={12} sm={7}>{text('txtbox_biomedesc', 'Biome description', eco.biomeDesc, (x) => setEco({ biomeDesc: x }), 'biomeDesc', 1000)}</Grid>
          <Grid item xs={12} sm={5}><RefSelect id="combobox_efg" label="Ecosystem functional group" options={(biome?.efgs ?? []).map(toRef)} value={eco.efg} disabled={!biome} onChange={(e) => setEco({ efg: e })} /></Grid>
          <Grid item xs={12} sm={7}>{text('txtbox_efgdesc', 'EFG description', eco.efgDesc, (x) => setEco({ efgDesc: x }), 'efgDesc', 1000)}</Grid>
          {/* combobox_es onSelect → rebuild sub-services */}
          <Grid item xs={12} sm={5}><RefSelect id="combobox_es" label="Ecosystem service" options={lk.eservices.map(toRef)} value={sv.eservice} onChange={(x) => setSv({ eservice: x, esubservice: null })} /></Grid>
          <Grid item xs={12} sm={7}>{text('txtbox_servdesc', 'Service description', sv.serviceDesc, (x) => setSv({ serviceDesc: x }), 'serviceDesc', 1000)}</Grid>
          <Grid item xs={12} sm={5}><RefSelect id="combobox_ess" label="Sub-service" options={(es?.subservices ?? []).map(toRef)} value={sv.esubservice} disabled={!es} onChange={(x) => setSv({ esubservice: x })} /></Grid>
          <Grid item xs={12} sm={7}>{text('txtbox_subservdesc', 'Sub-service description', sv.subserviceDesc, (x) => setSv({ subserviceDesc: x }), 'subserviceDesc', 1000)}</Grid>
        </Grid>
      ))}
      {sec('valMeth', 'Valuation method', (
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6}><RefSelect id="combobox_valMethod" label="Valuation method" options={lk.valuationMethods} value={m.method} onChange={(x) => setM({ method: x })} /></Grid>
          <Grid item xs={12} sm={6}><RefSelect id="combobox_economicMeasure" label="Economic measure" options={lk.economicMeasures} value={m.economicMeasure} onChange={(x) => setM({ economicMeasure: x })} showCode={false} /></Grid>
          <Grid item xs={12}>{text('txtbox_valueChange', 'Valued change', m.valuedChange, (x) => setM({ valuedChange: x }), 'valuedChange', undefined, true)}</Grid>
          <Grid item xs={12}>{text('txtbox_eqFunctionInfo', 'Valuation equation / function info', m.valuedEq, (x) => setM({ valuedEq: x }), 'valuedEq', 1500, true)}</Grid>
          <Grid item xs={12}>
            {/* listbox_availinfo onSelect → bandbox text */}
            <MultiRefSelect id="listbox_availinfo" label="Available study info" options={lk.availStudyInfo} showCode={false}
              value={m.availStudyInfo.filter((a) => !a.deleted).map((a) => a.term!).filter(Boolean)}
              onChange={(refs) => setM({ availStudyInfo: refs.map((r) => ({ id: null, deleted: false, term: r })) })} />
          </Grid>
          <Grid item xs={12}>{text('txtbox_surStudyInfo', 'Survey study info', m.surveyStudyInfo, (x) => setM({ surveyStudyInfo: x }), 'surveyStudyInfo', 1500, true)}</Grid>
        </Grid>
      ))}
      {sec('values', 'Values', (
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6}><RefSelect id="combobox_valueType" label="Value type" options={lk.valueTypes} value={v.valueType} onChange={(x) => setV({ valueType: x })} showCode={false} /></Grid>
          <Grid item xs={12} sm={6}>{text('txtbox_valuePt', 'Value point', v.valuePoint, (x) => setV({ valuePoint: x }), 'valuePoint', 25)}</Grid>
          <Grid item xs={12} sm={4}>{text('txtbox_Aggregate', 'Aggregate value', n.aggregate, (x) => setN({ ...n, aggregate: x }), 'aggregate')}</Grid>
          <Grid item xs={6} sm={4}>{text('txtbox_rangeLow', 'Range low', n.rangeLow, (x) => setN({ ...n, rangeLow: x }), 'rangeLow')}</Grid>
          <Grid item xs={6} sm={4}>{text('txtbox_rangeHigh', 'Range high', n.rangeHigh, (x) => setN({ ...n, rangeHigh: x }), 'rangeHigh')}</Grid>
          <Grid item xs={12} sm={4}><RefSelect id="combobox_currency" label="Currency" options={lk.currencies} value={v.currency} onChange={(x) => setV({ currency: x })} /></Grid>
          <Grid item xs={12} sm={4}><RefSelect id="combobox_unit" label="Unit (spatial)" options={lk.units} value={v.unit} onChange={(x) => setV({ unit: x })} showCode={false} /></Grid>
          <Grid item xs={12} sm={4}>{text('txtbox_valueYr', 'Value year', v.year, (x) => setV({ year: x }), 'year', 4)}</Grid>
          <Grid item xs={6}>{text('txtbox_pvYrs', 'PV years', v.pvYears, (x) => setV({ pvYears: x }), 'pvYears', 25)}</Grid>
          <Grid item xs={6}>{text('txtbox_discountRate', 'PV discount rate', v.pvDiscountRate, (x) => setV({ pvDiscountRate: x }), 'pvDiscountRate', 10)}</Grid>
          <Grid item xs={12} sm={4}><RefSelect id="combobox_beneficiaryType" label="Beneficiary type" options={lk.beneficiaryTypes} value={v.beneficiaryType} onChange={(x) => setV({ beneficiaryType: x })} showCode={false} /></Grid>
          <Grid item xs={12} sm={4}>{text('txtbox_beneficiaryNumber', 'Number of beneficiaries', n.beneficiaryNumber, (x) => setN({ ...n, beneficiaryNumber: x }), 'beneficiaryNumber')}</Grid>
          <Grid item xs={12} sm={4}><RefSelect id="combobox_beneficiaryUnit" label="Beneficiary unit" options={lk.beneficiaryUnits} value={v.beneficiaryUnit} onChange={(x) => setV({ beneficiaryUnit: x })} showCode={false} /></Grid>
        </Grid>
      ))}
    </>
  );
}

const firstSection = (e: Errors): Section => sectionOf[Object.keys(e)[0]] ?? 'values';

/**
 * AddSiteValuationDialog.zul → AddSiteValuationController: build a staged list of valuations, then "Save To List".
 * buttonAdd (validate → add to list, "Added to list."), buttonAddAndClear (reset form), × per row (remove), buttonSave
 * (confirm "You are about to add (N) values"), cancelButton (null).
 */
export function ValuationBatchDialog({ open, onClose }: { open: boolean; onClose: (added: DatasetDto[] | null) => void }) {
  const { data: lk } = useLookups();
  const confirm = useConfirm();
  const notify = useNotify();
  const [tab, setTab] = useState(1);
  const [d, setD] = useState<DatasetDto>(emptyDataset());
  const [n, setN] = useState<NumText>(numText(emptyDataset()));
  const [errors, setErrors] = useState<Errors>({});
  const [section, setSection] = useState<Section>('sf');
  const [staged, setStaged] = useState<DatasetDto[]>([]);

  useEffect(() => {
    if (open) {
      setStaged([]);
      clear();
      setTab(1);
    }
  }, [open]);

  if (!lk) return null;

  function clear() {
    const e = emptyDataset();
    setD(e);
    setN(numText(e));
    setErrors({});
    setSection('sf');
  }

  const add = () => {
    const e = check(d, n);
    setErrors(e);
    if (Object.keys(e).length) {
      setSection(firstSection(e));
      notify('Some values are incorrect.', 'error');
      return;
    }
    setStaged([...staged, withNumbers(d, n)]);
    notify('Added to list.');
    clear();
  };

  const saveToList = async () => {
    if (!staged.length) return notify('Add at least one value first.', 'warning');
    if (await confirm({ message: `You are about to add (${staged.length}) values. Do you wish to add this list?` })) onClose(staged);
  };

  return (
    <Dialog open={open} onClose={() => onClose(null)} maxWidth="lg" fullWidth>
      <DialogTitle>Add Site Valuation</DialogTitle>
      <DialogContent dividers sx={{ minHeight: 480 }}>
        <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 2 }}>
          <Tab id="recordCount" label={<Badge color="secondary" badgeContent={staged.length} showZero sx={{ pr: 1.5 }}>Study focus, method, values</Badge>} />
          <Tab label="Add new" />
        </Tabs>
        {tab === 0 ? (
          staged.length ? (
            <Table size="small" id="addedDataset">
              <TableHead>
                <TableRow>{['', 'Realm', 'Biome', 'EFG', 'Service', 'Sub-service', 'Method', 'Aggregate', 'Low', 'High'].map((h) => <TableCell key={h}>{h}</TableCell>)}</TableRow>
              </TableHead>
              <TableBody>
                {staged.map((x, i) => (
                  <TableRow key={x.key}>
                    <TableCell padding="checkbox">
                      <IconButton size="small" aria-label="Remove" onClick={() => setStaged(staged.filter((_, j) => j !== i))}><Close fontSize="small" /></IconButton>
                    </TableCell>
                    <TableCell>{x.ecosystem?.realm?.code ?? '—'}</TableCell>
                    <TableCell>{x.ecosystem?.biome?.code ?? '—'}</TableCell>
                    <TableCell>{x.ecosystem?.efg?.code ?? '—'}</TableCell>
                    <TableCell>{x.service?.eservice?.code ?? '—'}</TableCell>
                    <TableCell>{x.service?.esubservice?.code ?? '—'}</TableCell>
                    <TableCell>{x.method?.method?.name ?? '—'}</TableCell>
                    <TableCell>{x.value?.aggregate ?? '—'}</TableCell>
                    <TableCell>{x.value?.rangeLow ?? '—'}</TableCell>
                    <TableCell>{x.value?.rangeHigh ?? '—'}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          ) : (
            <Typography color="text.secondary">No values added yet. Use the "Add new" tab.</Typography>
          )
        ) : (
          <>
            {!!Object.keys(errors).length && <Alert severity="error" sx={{ mb: 2 }}>Some values are incorrect.</Alert>}
            <ValuationFields lk={lk} d={d} setD={setD} n={n} setN={setN} errors={errors} section={section} setSection={setSection} />
          </>
        )}
      </DialogContent>
      <DialogActions sx={{ flexWrap: 'wrap', gap: 1 }}>
        {tab === 1 && (
          <>
            <Button id="buttonAddAndClear" onClick={clear}>Clear</Button>
            <Button id="buttonAdd" variant="outlined" onClick={add}>Add to list</Button>
          </>
        )}
        <span style={{ flex: 1 }} />
        <Button id="cancelButton" onClick={() => onClose(null)}>Cancel</Button>
        <Button id="buttonSave" variant="contained" disabled={!staged.length} onClick={saveToList}>Save to list ({staged.length})</Button>
      </DialogActions>
    </Dialog>
  );
}

/** EditSiteValuationDialog.zul → EditSiteValuationController: edit one valuation in place (arg "dataset"). */
export function ValuationEditDialog({ open, dataset, onClose }: { open: boolean; dataset: DatasetDto | null; onClose: (d: DatasetDto | null) => void }) {
  const { data: lk } = useLookups();
  const notify = useNotify();
  const [d, setD] = useState<DatasetDto>(emptyDataset());
  const [n, setN] = useState<NumText>(numText(emptyDataset()));
  const [errors, setErrors] = useState<Errors>({});
  const [section, setSection] = useState<Section>('sf');

  useEffect(() => {
    if (open && dataset) {
      // Fill missing parts (the ZK edit dialog NPE'd when the valuation method was null).
      const base = emptyDataset();
      const full: DatasetDto = {
        ...dataset,
        ecosystem: dataset.ecosystem ?? base.ecosystem,
        service: dataset.service ?? base.service,
        method: dataset.method ?? base.method,
        value: dataset.value ?? base.value,
      };
      setD(structuredClone(full));
      setN(numText(full));
      setErrors({});
      setSection('sf');
    }
  }, [open, dataset]);

  if (!lk) return null;
  const save = () => {
    const e = check(d, n);
    setErrors(e);
    if (Object.keys(e).length) {
      setSection(firstSection(e));
      notify('Some values are incorrect.', 'error');
      return;
    }
    onClose(withNumbers(d, n));
  };
  return (
    <Dialog open={open} onClose={() => onClose(null)} maxWidth="lg" fullWidth>
      <DialogTitle>Edit Site Valuation</DialogTitle>
      <DialogContent dividers>
        {!!Object.keys(errors).length && <Alert severity="error" sx={{ mb: 2 }}>Some values are incorrect.</Alert>}
        <ValuationFields lk={lk} d={d} setD={setD} n={n} setN={setN} errors={errors} section={section} setSection={setSection} />
      </DialogContent>
      <DialogActions>
        <Button id="cancelButton" onClick={() => onClose(null)}>Cancel</Button>
        <Button id="buttonSave" variant="contained" onClick={save}>Save</Button>
      </DialogActions>
    </Dialog>
  );
}

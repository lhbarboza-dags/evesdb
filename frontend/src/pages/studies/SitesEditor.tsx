import {
  Box, Button, Collapse, IconButton, Stack, Table, TableBody, TableCell, TableHead, TableRow, Tooltip, Typography,
} from '@mui/material';
import Add from '@mui/icons-material/Add';
import Edit from '@mui/icons-material/Edit';
import Delete from '@mui/icons-material/DeleteOutline';
import KeyboardArrowDown from '@mui/icons-material/KeyboardArrowDown';
import KeyboardArrowUp from '@mui/icons-material/KeyboardArrowUp';
import PlaylistAdd from '@mui/icons-material/PlaylistAdd';
import { Fragment, useState, type ReactNode } from 'react';
import type { DatasetDto, SiteDto } from '../../api/types';
import { ConfirmDeleteDialog } from '../../components/ConfirmDeleteDialog';
import { useNotify } from '../../components/Notifier';
import { InfoPopover } from '../search/StudyDetail';
import { SiteDialog } from './SiteDialog';
import { ValuationBatchDialog, ValuationEditDialog } from './ValuationDialogs';
import { liveDatasets, rowKey } from './studyGraph';

const nz = (v: unknown) => (v === null || v === undefined || v === '' ? '—' : String(v));

function KV({ k, v }: { k: string; v: ReactNode }) {
  return (
    <Box sx={{ display: 'grid', gridTemplateColumns: '150px 1fr', gap: 1, py: 0.25 }}>
      <Typography variant="body2" sx={{ fontWeight: 700 }}>{k}</Typography>
      <Typography variant="body2" component="div">{v}</Typography>
    </Box>
  );
}

/**
 * Sites grid with expandable valuations.
 *  mode="add"  → AddSiteRowRenderer + datamgt AddSiteValuationRenderer (wizard, _study.zul grid_site)
 *  mode="edit" → EditSiteRowRenderer + EditSiteValuationRenderer (editstudy.zul)
 */
export function SitesEditor({ sites, onChange, mode }: { sites: SiteDto[]; onChange: (s: SiteDto[]) => void; mode: 'add' | 'edit' }) {
  const notify = useNotify();
  const [open, setOpen] = useState<Record<string, boolean>>({});
  const [siteDialog, setSiteDialog] = useState<{ index: number | null } | null>(null);
  const [batchFor, setBatchFor] = useState<number | null>(null);
  const [editDs, setEditDs] = useState<{ site: number; ds: number } | null>(null);
  const [deleteSite, setDeleteSite] = useState<number | null>(null);

  const visible = sites.map((s, i) => ({ s, i })).filter(({ s }) => !s.deleted);
  const replace = (i: number, s: SiteDto) => onChange(sites.map((x, j) => (j === i ? s : x)));

  /** Site trash: add mode removes (ZK: deleteDialog OK never removed it); edit mode soft-deletes, never the last site. */
  const removeSite = (i: number) => {
    if (mode === 'edit' && visible.length <= 1) return notify('Cannot delete last item for valuation', 'warning');
    const s = sites[i];
    if (s.id == null) onChange(sites.filter((_, j) => j !== i));
    else replace(i, { ...s, deleted: true });
  };

  /** Dataset trash: edit mode keeps at least one valuation; saved rows are soft-deleted, unsaved rows removed. */
  const removeDataset = (si: number, di: number) => {
    const s = sites[si];
    if (mode === 'edit' && liveDatasets(s).length <= 1) return notify('Cannot delete last item for valuation', 'warning');
    const d = s.datasets[di];
    const datasets = d.id == null ? s.datasets.filter((_, j) => j !== di) : s.datasets.map((x, j) => (j === di ? { ...x, deleted: true } : x));
    replace(si, { ...s, datasets });
  };

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 1 }}>
        <Typography variant="h6">Study sites</Typography>
        <Button id={mode === 'add' ? 'btn_addSite' : 'addSite'} variant="contained" startIcon={<Add />} onClick={() => setSiteDialog({ index: null })}>
          Add site
        </Button>
      </Stack>
      <Box sx={{ overflowX: 'auto', border: 1, borderColor: 'divider', borderRadius: 1 }}>
        <Table size="small" id="grid_site">
          <TableHead>
            <TableRow>
              <TableCell padding="checkbox" />
              <TableCell>Site</TableCell>
              <TableCell>Country</TableCell>
              <TableCell>Coordinates</TableCell>
              <TableCell>Scale</TableCell>
              <TableCell>Values</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {!visible.length && (
              <TableRow><TableCell colSpan={7}><Typography color="text.secondary">No sites yet. Add a site, then add its valuations.</Typography></TableCell></TableRow>
            )}
            {visible.map(({ s, i }) => {
              const k = rowKey(s);
              const ci = s.location?.countryInfo;
              const ds = s.datasets.map((d, di) => ({ d, di })).filter(({ d }) => !d.deleted);
              return (
                <Fragment key={k}>
                  <TableRow hover>
                    <TableCell padding="checkbox">
                      <IconButton size="small" aria-label="Show valuations" onClick={() => setOpen({ ...open, [k]: !open[k] })}>
                        {open[k] ? <KeyboardArrowUp /> : <KeyboardArrowDown />}
                      </IconButton>
                    </TableCell>
                    <TableCell>{s.name}</TableCell>
                    <TableCell>
                      <Stack direction="row" alignItems="center">
                        {nz(ci?.countryName)}
                        <InfoPopover label="Country information">
                          <KV k="Year" v={nz(ci?.year)} />
                          <KV k="WB Income" v={nz(ci?.wbIncome)} />
                          <KV k="GDP Per Capita" v={nz(ci?.gdpPerCapita)} />
                          <KV k="Population Density" v={nz(ci?.popDensity)} />
                        </InfoPopover>
                      </Stack>
                    </TableCell>
                    <TableCell>{s.latitude}, {s.longitude}</TableCell>
                    <TableCell>{nz(s.scale?.name)}</TableCell>
                    <TableCell>{ds.length}</TableCell>
                    <TableCell align="right" sx={{ whiteSpace: 'nowrap' }}>
                      <InfoPopover label="Site summary">
                        <KV k="Location" v={nz(s.location?.name)} />
                        <KV k="Latitude / Longitude" v={`${s.latitude}, ${s.longitude}`} />
                        <KV k="Climate" v={nz(s.climates.filter((c) => !c.deleted).map((c) => c.climateType?.name).join(', '))} />
                        <KV k="Country / Year" v={`${nz(ci?.countryName)} / ${nz(ci?.year)}`} />
                        <KV k="Attributes" v={nz(s.attributes.filter((a) => !a.deleted).map((a) => (a.otherValue ? `${a.attribute?.name}: ${a.otherValue}` : a.attribute?.name)).join(', '))} />
                        <KV k="Protected status" v={nz(s.location?.protectedStatus?.name)} />
                        <KV k="Protected area" v={nz(s.location?.protectedArea?.name)} />
                        <KV k="Scale" v={nz(s.scale?.name)} />
                        <KV k="Area" v={s.area ? `${s.area} ${s.areaUnit ?? ''}` : '—'} />
                      </InfoPopover>
                      <Tooltip title="Add valuations"><IconButton aria-label="Add valuations" onClick={() => setBatchFor(i)}><PlaylistAdd /></IconButton></Tooltip>
                      <Tooltip title="Edit site"><IconButton aria-label="Edit site" onClick={() => setSiteDialog({ index: i })}><Edit /></IconButton></Tooltip>
                      <Tooltip title="Delete site">
                        <IconButton aria-label="Delete site" onClick={() => (mode === 'add' ? setDeleteSite(i) : removeSite(i))}><Delete /></IconButton>
                      </Tooltip>
                    </TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell colSpan={7} sx={{ py: 0, borderBottom: open[k] ? undefined : 0 }}>
                      <Collapse in={!!open[k]} unmountOnExit>
                        <Box sx={{ py: 1.5, overflowX: 'auto' }}>
                          <Table size="small">
                            <TableHead>
                              <TableRow>
                                {['Actions', 'Realm', 'Biome', 'EFG', 'E-service', 'E-sub-service', 'Method', 'Values'].map((h) => <TableCell key={h}>{h}</TableCell>)}
                              </TableRow>
                            </TableHead>
                            <TableBody>
                              {!ds.length && <TableRow><TableCell colSpan={8}>No valuations yet.</TableCell></TableRow>}
                              {ds.map(({ d, di }) => (
                                <TableRow key={rowKey(d)}>
                                  <TableCell sx={{ whiteSpace: 'nowrap' }}>
                                    <IconButton size="small" aria-label="Edit valuation" onClick={() => setEditDs({ site: i, ds: di })}><Edit fontSize="small" /></IconButton>
                                    <IconButton size="small" aria-label="Delete valuation" onClick={() => removeDataset(i, di)}><Delete fontSize="small" /></IconButton>
                                  </TableCell>
                                  <TableCell>{nz(d.ecosystem?.realm?.code)}</TableCell>
                                  <TableCell>{nz(d.ecosystem?.biome?.code)}</TableCell>
                                  <TableCell>{nz(d.ecosystem?.efg?.code)}</TableCell>
                                  <TableCell>{nz(d.service?.eservice?.code)}</TableCell>
                                  <TableCell>{nz(d.service?.esubservice?.code)}</TableCell>
                                  <TableCell><MethodCell d={d} /></TableCell>
                                  <TableCell><ValuesCell d={d} /></TableCell>
                                </TableRow>
                              ))}
                            </TableBody>
                          </Table>
                        </Box>
                      </Collapse>
                    </TableCell>
                  </TableRow>
                </Fragment>
              );
            })}
          </TableBody>
        </Table>
      </Box>

      <SiteDialog
        open={!!siteDialog}
        site={siteDialog?.index != null ? sites[siteDialog.index] : null}
        onClose={(s) => {
          if (s && siteDialog) {
            if (siteDialog.index == null) onChange([...sites, s]);
            else replace(siteDialog.index, { ...s, datasets: sites[siteDialog.index].datasets });
          }
          setSiteDialog(null);
        }}
      />
      <ValuationBatchDialog
        open={batchFor != null}
        onClose={(added) => {
          if (added && batchFor != null) {
            const s = sites[batchFor];
            replace(batchFor, { ...s, datasets: [...s.datasets, ...added] });
            setOpen({ ...open, [rowKey(s)]: true });
          }
          setBatchFor(null);
        }}
      />
      <ValuationEditDialog
        open={!!editDs}
        dataset={editDs ? sites[editDs.site].datasets[editDs.ds] : null}
        onClose={(d) => {
          if (d && editDs) {
            const s = sites[editDs.site];
            replace(editDs.site, { ...s, datasets: s.datasets.map((x, j) => (j === editDs.ds ? d : x)) });
          }
          setEditDs(null);
        }}
      />
      <ConfirmDeleteDialog
        open={deleteSite != null}
        itemLabel={deleteSite != null ? `site "${sites[deleteSite]?.name}"` : 'site'}
        onCancel={() => setDeleteSite(null)}
        onConfirm={() => {
          if (deleteSite != null) removeSite(deleteSite);
          setDeleteSite(null);
        }}
      />
    </Box>
  );
}

/** EditSiteValuationRenderer method info popup (or "Not Indicated."). */
function MethodCell({ d }: { d: DatasetDto }) {
  const m = d.method;
  const avail = m?.availStudyInfo.filter((a) => !a.deleted).map((a) => a.term?.name).join(', ');
  const hasInfo = !!(m?.valuedChange || m?.valuedEq || m?.economicMeasure || avail || m?.surveyStudyInfo);
  return (
    <Stack direction="row" alignItems="center">
      {m?.method?.name ?? '—'}
      {hasInfo ? (
        <InfoPopover label="Method details">
          <KV k="Valued Change" v={nz(m?.valuedChange)} />
          <KV k="Equation Function Info" v={nz(m?.valuedEq)} />
          <KV k="Economic Measure" v={nz(m?.economicMeasure?.name)} />
          <KV k="Available Study Info" v={nz(avail)} />
          <KV k="Survey Study Info" v={nz(m?.surveyStudyInfo)} />
        </InfoPopover>
      ) : (
        <Typography variant="caption" color="text.secondary" sx={{ ml: 1 }}>Not Indicated.</Typography>
      )}
    </Stack>
  );
}

/** EditSiteValuationRenderer values info popup. */
function ValuesCell({ d }: { d: DatasetDto }) {
  const v = d.value;
  return (
    <Stack direction="row" alignItems="center">
      {nz(v?.valuePoint || v?.aggregate)}
      <InfoPopover label="Values">
        <KV k="Year" v={nz(v?.year)} />
        <KV k="Value Point" v={nz(v?.valuePoint)} />
        <KV k="Aggregate" v={nz(v?.aggregate)} />
        <KV k="Range" v={v?.rangeLow != null || v?.rangeHigh != null ? `${nz(v?.rangeLow)} – ${nz(v?.rangeHigh)}` : '—'} />
        <KV k="Currency" v={nz(v?.currency?.name)} />
        <KV k="Unit" v={nz(v?.unit?.name)} />
        <KV k="PV Years" v={nz(v?.pvYears)} />
        <KV k="PV Disc Rate" v={nz(v?.pvDiscountRate)} />
        <KV k="Beneficiary Type" v={nz(v?.beneficiaryType?.name)} />
        <KV k="Beneficiary Number" v={nz(v?.beneficiaryNumber)} />
        <KV k="Beneficiary Unit" v={nz(v?.beneficiaryUnit?.name)} />
      </InfoPopover>
    </Stack>
  );
}

import { Box, Chip, CircularProgress, IconButton, Link, Popover, Stack, Table, TableBody, TableCell, TableHead, TableRow, Typography } from '@mui/material';
import InfoOutlined from '@mui/icons-material/InfoOutlined';
import { useQuery } from '@tanstack/react-query';
import { useState, type ReactNode } from 'react';
import { api } from '../../api/client';
import type { DatasetDto, SiteDto, StudyDto } from '../../api/types';

const nz = (v: unknown) => (v === null || v === undefined || v === '' ? '—' : String(v));

/** The study detail panel built by StudySrcRenderer row click (and StudySrcDetailRenderer in detailed mode). */
export function StudyDetailLoader({ id }: { id: number }) {
  const { data, isLoading, error } = useQuery({ queryKey: ['study-detail', id], queryFn: () => api.get<StudyDto>(`/api/search/studies/${id}`) });
  if (isLoading) return <CircularProgress size={24} />;
  if (error) return <Typography color="error">{(error as Error).message}</Typography>;
  return data ? <StudyDetail study={data} /> : null;
}

function Field({ label, children }: { label: string; children: ReactNode }) {
  return (
    <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '160px 1fr' }, gap: { xs: 0, sm: 2 }, py: 0.5 }}>
      <Typography component="span" sx={{ fontWeight: 700, color: 'text.secondary' }}>{label}</Typography>
      <Typography component="div" sx={{ minWidth: 0, overflowWrap: 'anywhere' }}>{children}</Typography>
    </Box>
  );
}

/** Info icon + popover (the ZK "z-icon-info-circle" buttons with Popup). */
export function InfoPopover({ label, children }: { label: string; children: ReactNode }) {
  const [anchor, setAnchor] = useState<HTMLElement | null>(null);
  return (
    <>
      <IconButton size="small" aria-label={label} onClick={(e) => { e.stopPropagation(); setAnchor(e.currentTarget); }}>
        <InfoOutlined fontSize="small" />
      </IconButton>
      <Popover open={!!anchor} anchorEl={anchor} onClose={() => setAnchor(null)} anchorOrigin={{ vertical: 'bottom', horizontal: 'left' }}>
        <Box sx={{ p: 2, maxWidth: 420 }} onClick={(e) => e.stopPropagation()}>
          <Typography variant="subtitle2" gutterBottom>{label}</Typography>
          {children}
        </Box>
      </Popover>
    </>
  );
}

export function StudyDetail({ study }: { study: StudyDto }) {
  const sites = study.sites;
  const focus = focusOf(sites);
  return (
    <Box sx={{ lineHeight: 1.6 }}>
      <Typography variant="h5" component="h3" gutterBottom>
        {study.title} {study.pubYear ? `(${study.pubYear})` : ''}
      </Typography>
      <Field label="Author">{nz(study.author)}</Field>
      <Field label="Document Type">{nz(study.docType?.name)}</Field>
      <Field label="Abstract">{nz(study.abstractText)}</Field>
      <Field label="Reference">{nz(study.reference)}</Field>
      <Field label="External link">
        {study.externalLink ? <Link href={study.externalLink} target="_blank" rel="noopener">{study.externalLink}</Link> : '—'}
      </Field>

      <Typography variant="h6" sx={{ mt: 3 }}>1. Study Site</Typography>
      <Box sx={{ overflowX: 'auto' }}>
        <Table size="small">
          <TableHead>
            <TableRow>
              {['Year of Study', 'Site', 'Location', 'Country', 'Climate type', 'Population Density', 'GDP per Capita', 'WB Income Group', 'Attribute', 'Protected Status', 'Protected Area', 'Scale', 'Area'].map((h) => (
                <TableCell key={h}>{h}</TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {sites.map((s) => {
              const ci = s.location?.countryInfo;
              const pa = s.location?.protectedArea;
              return (
                <TableRow key={s.id}>
                  <TableCell>{nz(ci?.year)}</TableCell>
                  <TableCell>{s.name}</TableCell>
                  <TableCell>{nz(s.location?.name)}<br /><Typography variant="caption" color="text.secondary">{s.latitude}, {s.longitude}</Typography></TableCell>
                  <TableCell>{nz(ci?.countryName)}</TableCell>
                  <TableCell>{nz(s.climates.map((c) => c.climateType?.name).join(', '))}</TableCell>
                  <TableCell>{nz(ci?.popDensity)}</TableCell>
                  <TableCell>{nz(ci?.gdpPerCapita)}</TableCell>
                  <TableCell>{nz(ci?.wbIncome)}</TableCell>
                  <TableCell>{nz(s.attributes.map((a) => (a.otherValue ? `${a.attribute?.name}: ${a.otherValue}` : a.attribute?.name)).join(', '))}</TableCell>
                  <TableCell>{nz(s.location?.protectedStatus?.name)}</TableCell>
                  <TableCell>
                    {pa ? (
                      <Stack direction="row" alignItems="center">
                        {pa.name}
                        <InfoPopover label="Protected Area">
                          <Field label="Name">{pa.name}</Field>
                          <Field label="Area (Ha)">{nz(pa.area)}</Field>
                          <Field label="Established">{nz(pa.established)}</Field>
                          <Field label="Location">{nz(pa.location)}</Field>
                        </InfoPopover>
                      </Stack>
                    ) : '—'}
                  </TableCell>
                  <TableCell>{nz(s.scale?.name)}</TableCell>
                  <TableCell>{s.area ? `${s.area} ${s.areaUnit ?? ''}` : '—'}</TableCell>
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </Box>

      <Typography variant="h6" sx={{ mt: 3 }}>2. Study Focus</Typography>
      <Stack direction="row" flexWrap="wrap" gap={1} sx={{ my: 1 }}>
        {focus.map((f) => <Chip key={f} label={f} variant="outlined" />)}
        {!focus.length && '—'}
      </Stack>

      <Typography variant="h6" sx={{ mt: 3 }}>3. Methods</Typography>
      <Stack direction="row" flexWrap="wrap" gap={1} sx={{ my: 1 }}>
        {[...new Set(sites.flatMap((s) => s.datasets.map((d) => d.method?.method?.name).filter(Boolean) as string[]))].map((m) => (
          <Chip key={m} label={m} color="primary" variant="outlined" />
        ))}
      </Stack>

      <Typography variant="h6" sx={{ mt: 3 }}>4. Values</Typography>
      {sites.map((s, si) => (
        <Box key={s.id} sx={{ mb: 2 }}>
          <Typography sx={{ fontWeight: 700, mt: 1 }}>4.{si + 1}. {s.name}</Typography>
          <Box sx={{ overflowX: 'auto' }}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  {['#', 'Ecosystem', 'Service', 'Method', 'Value Type', 'Economic Measure', 'Value Point', 'Value Aggregate', 'Value Range', 'Currency', 'Unit', 'Year'].map((h) => (
                    <TableCell key={h}>{h}</TableCell>
                  ))}
                </TableRow>
              </TableHead>
              <TableBody>
                {s.datasets.map((d, di) => <ValueRow key={d.id ?? di} n={`4.${si + 1}.${di + 1}`} d={d} />)}
              </TableBody>
            </Table>
          </Box>
        </Box>
      ))}
    </Box>
  );
}

function ValueRow({ n, d }: { n: string; d: DatasetDto }) {
  const v = d.value;
  const e = d.ecosystem;
  const s = d.service;
  return (
    <TableRow>
      <TableCell>{n}</TableCell>
      <TableCell>{[e?.realm?.name, e?.biome?.name, e?.efg?.name].filter(Boolean).join(' › ') || '—'}</TableCell>
      <TableCell>{[s?.eservice?.name, s?.esubservice?.name].filter(Boolean).join(' › ') || '—'}</TableCell>
      <TableCell>{nz(d.method?.method?.name)}</TableCell>
      <TableCell>{nz(v?.valueType?.name)}</TableCell>
      <TableCell>{nz(d.method?.economicMeasure?.name)}</TableCell>
      <TableCell>{nz(v?.valuePoint)}</TableCell>
      <TableCell>{nz(v?.aggregate)}</TableCell>
      <TableCell>{v?.rangeLow != null || v?.rangeHigh != null ? `${nz(v?.rangeLow)} – ${nz(v?.rangeHigh)}` : '—'}</TableCell>
      <TableCell>{nz(v?.currency?.name)}</TableCell>
      <TableCell>{nz(v?.unit?.name)}</TableCell>
      <TableCell>{nz(v?.year)}</TableCell>
    </TableRow>
  );
}

/** "Realm (..), Biome (..), EFG (..)" + "EService (..), ESub-Service (..)" as in StudySrcRenderer. */
function focusOf(sites: SiteDto[]) {
  const sets = { Realm: new Set<string>(), Biome: new Set<string>(), EFG: new Set<string>(), EService: new Set<string>(), 'ESub-Service': new Set<string>() };
  for (const s of sites)
    for (const d of s.datasets) {
      if (d.ecosystem?.realm?.name) sets.Realm.add(d.ecosystem.realm.name);
      if (d.ecosystem?.biome?.name) sets.Biome.add(d.ecosystem.biome.name);
      if (d.ecosystem?.efg?.name) sets.EFG.add(d.ecosystem.efg.name);
      if (d.service?.eservice?.name) sets.EService.add(d.service.eservice.name);
      if (d.service?.esubservice?.name) sets['ESub-Service'].add(d.service.esubservice.name);
    }
  return Object.entries(sets).flatMap(([k, v]) => (v.size ? [`${k} (${[...v].join(', ')})`] : []));
}

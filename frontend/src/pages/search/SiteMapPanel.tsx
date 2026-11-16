import { Alert, Box, CircularProgress, Grid, Link, List, ListItemButton, ListItemText, Pagination, Typography } from '@mui/material';
import { APIProvider, InfoWindow, Map, Marker, useMap } from '@vis.gl/react-google-maps';
import { useQuery } from '@tanstack/react-query';
import { useEffect, useMemo, useState } from 'react';
import { api } from '../../api/client';
import { usePublicConfig } from '../../api/hooks';
import type { MapSite } from '../../api/types';

const PAGE = 10;
const DEFAULT_CENTER = { lat: 23.69781, lng: 120.960515 }; // SearchController.createPanel default

/** Map tab (btn_gmap → createPanel): site list (GmapSiteItemRenderer, paging 10) + Gmaps with one marker per study site. */
export function SiteMapPanel({ ids }: { ids: number[] }) {
  const cfg = usePublicConfig();
  const { data, isLoading } = useQuery({ queryKey: ['map', ids], queryFn: () => api.post<MapSite[]>('/api/search/map', { ids }) });
  const [page, setPage] = useState(1);
  const [selected, setSelected] = useState<MapSite | null>(null);
  const sites = useMemo(() => data ?? [], [data]);
  const located = sites.filter((s) => s.lat != null && s.lng != null);

  if (isLoading || !cfg.data) return <CircularProgress />;
  const key = cfg.data.mapsApiKey;

  return (
    <Grid container spacing={2}>
      <Grid item xs={12} md={4}>
        <Typography variant="subtitle2" gutterBottom>{sites.length} sites ({located.length} with coordinates)</Typography>
        <List dense sx={{ border: 1, borderColor: 'divider', borderRadius: 1 }}>
          {sites.slice((page - 1) * PAGE, page * PAGE).map((s) => (
            <ListItemButton key={`${s.studyId}-${s.siteId}`} selected={selected === s} onClick={() => setSelected(s)}>
              <ListItemText
                primary={`${s.country || '—'} · ${s.siteName}`}
                secondary={<>{s.title}<br />{s.latitude}, {s.longitude}</>}
                secondaryTypographyProps={{ sx: { display: '-webkit-box', WebkitLineClamp: 3, WebkitBoxOrient: 'vertical', overflow: 'hidden' } }}
              />
            </ListItemButton>
          ))}
        </List>
        {sites.length > PAGE && <Pagination sx={{ mt: 1 }} count={Math.ceil(sites.length / PAGE)} page={page} onChange={(_, p) => setPage(p)} size="small" />}
      </Grid>
      <Grid item xs={12} md={8}>
        {key ? (
          <APIProvider apiKey={key}>
            <Box sx={{ height: { xs: 360, md: 560 }, borderRadius: 1, overflow: 'hidden' }}>
              <Map defaultZoom={3} defaultCenter={DEFAULT_CENTER} mapTypeId="hybrid" gestureHandling="greedy">
                {located.map((s) => (
                  <Marker key={`${s.studyId}-${s.siteId}`} position={{ lat: s.lat!, lng: s.lng! }} title={s.siteName} onClick={() => setSelected(s)} />
                ))}
                {selected?.lat != null && (
                  <InfoWindow position={{ lat: selected.lat, lng: selected.lng! }} onCloseClick={() => setSelected(null)}>
                    <MarkerContent s={selected} />
                  </InfoWindow>
                )}
                <FocusOnSelect site={selected} />
              </Map>
            </Box>
          </APIProvider>
        ) : (
          <Alert severity="info">
            Google Maps is not configured (set GOOGLE_MAPS_API_KEY). {selected && selected.lat != null && (
              <Link href={`https://www.google.com/maps?q=${selected.lat},${selected.lng}`} target="_blank" rel="noopener">Open the selected site in Google Maps</Link>
            )}
          </Alert>
        )}
      </Grid>
    </Grid>
  );
}

/** Listbox onSelect: open marker, zoom 7, recentre (SearchController.java:1418). */
function FocusOnSelect({ site }: { site: MapSite | null }) {
  const map = useMap();
  useEffect(() => {
    if (map && site?.lat != null && site.lng != null) {
      map.setZoom(7);
      map.panTo({ lat: site.lat, lng: site.lng });
    }
  }, [map, site]);
  return null;
}

function MarkerContent({ s }: { s: MapSite }) {
  return (
    <Box sx={{ maxWidth: 300 }}>
      <Typography variant="subtitle2">{s.title}</Typography>
      <Typography variant="body2">{s.siteName} · {s.country}</Typography>
      <Box component="ul" sx={{ pl: 2, my: 0.5 }}>
        {s.values.slice(0, 12).map((v, i) => <li key={i}><Typography variant="caption">{v}</Typography></li>)}
      </Box>
      {s.values.length > 12 && <Typography variant="caption">and {s.values.length - 12} more values</Typography>}
    </Box>
  );
}

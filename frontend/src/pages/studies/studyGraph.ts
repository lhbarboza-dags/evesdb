import type { DatasetDto, SiteDto, StudyDto } from '../../api/types';

let seq = 0;
export const newKey = (p: string) => `${p}-${Date.now().toString(36)}-${++seq}`;
export const rowKey = (x: { id: number | null; key?: string }) => (x.id != null ? `id-${x.id}` : (x.key ?? ''));

export function emptyStudy(): StudyDto {
  return { id: null, title: '', abstractText: '', author: '', docType: null, pubYear: '', reference: '', externalLink: '', status: null, sites: [] };
}

export function emptySite(): SiteDto {
  return {
    id: null, key: newKey('site'), deleted: false, name: '', latitude: '', longitude: '', area: '', areaUnit: '', scale: null,
    location: { id: null, name: '', countryInfo: null, protectedStatus: null, protectedArea: null },
    climates: [], attributes: [], datasets: [],
  };
}

export function emptyDataset(): DatasetDto {
  return {
    id: null, key: newKey('ds'), deleted: false,
    ecosystem: { id: null, realm: null, realmDesc: '', biome: null, biomeDesc: '', efg: null, efgDesc: '' },
    service: { id: null, eservice: null, serviceDesc: '', esubservice: null, subserviceDesc: '' },
    method: { id: null, method: null, valuedChange: '', valuedEq: '', economicMeasure: null, surveyStudyInfo: '', availStudyInfo: [] },
    value: {
      id: null, valueType: null, valuePoint: '', aggregate: null, rangeLow: null, rangeHigh: null, currency: null, unit: null, year: '',
      pvYears: '', pvDiscountRate: '', beneficiaryType: null, beneficiaryNumber: null, beneficiaryUnit: null,
    },
  };
}

export const liveSites = (s: StudyDto) => s.sites.filter((x) => !x.deleted);
export const liveDatasets = (s: SiteDto) => s.datasets.filter((x) => !x.deleted);

/**
 * AddSiteController.calculate: decimal = d + m/60 + s/3600, or "0" when a part is empty/invalid.
 * Fixed: a negative degree value now gives a negative result (south / west), the ZK code added the minutes instead.
 */
export function dmsToDecimal(d: string, m: string, s: string): string {
  const num = /^-?(\d*\.)?\d+$/;
  if (![d, m, s].every((x) => num.test(x.trim()))) return '0';
  const deg = parseFloat(d);
  const sign = deg < 0 || Object.is(deg, -0) || d.trim().startsWith('-') ? -1 : 1;
  const value = Math.abs(deg) + parseFloat(m) / 60 + parseFloat(s) / 3600;
  return String(Math.round(sign * value * 1e6) / 1e6);
}

// ------------------------------------------------------------------ validation (zul constraints)

export type Errors = Record<string, string>;
const DECIMAL = /^-?(\d*\.)?\d+$/;
const NUMERIC_OPT = /^$|^-?(\d*\.)?\d+$/;

/** _study.zul / editstudy.zul textboxes. abstractRequired: editstudy.zul had /^.{1,2000}$/ on the abstract. */
export function validateStudy(s: StudyDto, abstractRequired = false): Errors {
  const e: Errors = {};
  if (!s.title.trim() || s.title.length > 255) e.title = 'Title is required (up to 255 characters).';
  if (!s.author.trim() || s.author.length > 255) e.author = 'Author is required (up to 255 characters).';
  if (abstractRequired && !s.abstractText?.trim()) e.abstractText = 'Abstract is required.';
  if ((s.abstractText ?? '').length > 2000) e.abstractText = 'Up to 2000 characters.';
  if (!/^$|^(19|20)\d{2}$/.test(s.pubYear ?? '')) e.pubYear = 'Year must be 19xx or 20xx.';
  if ((s.reference ?? '').length > 255) e.reference = 'Up to 255 characters.';
  return e;
}

/** AddSiteController.buttonSave order: site name → country → scale → latitude → longitude. Returns field → message. */
export function validateSite(s: SiteDto): Errors {
  const e: Errors = {};
  if (!s.name.trim() || s.name.length > 255) e.name = 'Site name is required.';
  if (!s.location?.countryInfo?.countryId && !s.location?.countryInfo?.id) e.country = 'Country is required.';
  if (!s.scale) e.scale = 'Scale is required.';
  if (!DECIMAL.test(s.latitude.trim()) || s.latitude.length > 24) e.latitude = 'Latitude must be a decimal number.';
  if (!DECIMAL.test(s.longitude.trim()) || s.longitude.length > 24) e.longitude = 'Longitude must be a decimal number.';
  return e;
}

/** AddSiteValuationController.checkComponents / EditSiteValuationController.checkComponents. */
export function validateDataset(d: DatasetDto): Errors {
  const e: Errors = {};
  const m = d.method!;
  const v = d.value!;
  const len = (x: string | null | undefined, n: number) => (x ?? '').length > n;
  if (len(d.ecosystem?.realmDesc, 1000)) e.realmDesc = 'Up to 1000 characters.';
  if (len(d.ecosystem?.biomeDesc, 1000)) e.biomeDesc = 'Up to 1000 characters.';
  if (len(d.ecosystem?.efgDesc, 1000)) e.efgDesc = 'Up to 1000 characters.';
  if (len(d.service?.serviceDesc, 1000)) e.serviceDesc = 'Up to 1000 characters.';
  if (len(d.service?.subserviceDesc, 1000)) e.subserviceDesc = 'Up to 1000 characters.';
  if (len(m.valuedEq, 1500)) e.valuedEq = 'Up to 1500 characters.';
  if (len(m.surveyStudyInfo, 1500)) e.surveyStudyInfo = 'Up to 1500 characters.';
  if (len(v.valuePoint, 25)) e.valuePoint = 'Up to 25 characters.';
  for (const k of ['aggregate', 'rangeLow', 'rangeHigh', 'beneficiaryNumber'] as const)
    if (!NUMERIC_OPT.test(v[k] == null ? '' : String(v[k]))) e[k] = 'Must be a number.';
  if (!/^$|^[1-2]\d{3}$/.test(v.year ?? '')) e.year = 'Value year must be a 4-digit year.';
  if (len(v.pvYears, 25)) e.pvYears = 'Up to 25 characters.';
  if (len(v.pvDiscountRate, 10)) e.pvDiscountRate = 'Up to 10 characters.';
  return e;
}

/** Wizard step 2 → 3 (btnNext_step3_b). */
export function validateSitesStep(s: StudyDto): string | null {
  const sites = liveSites(s);
  if (!sites.length) return 'Please add site first.';
  const empty = sites.find((x) => !liveDatasets(x).length);
  if (empty) return `There is an empty valuation for a site: ${empty.name}`;
  return null;
}

/** Converts the numeric text inputs of a dataset to numbers (empty → null), as AppContext.getDecimalValue. */
export function toNumber(x: string): number | null {
  const t = x.trim();
  return t === '' || !NUMERIC_OPT.test(t) ? null : Number(t);
}

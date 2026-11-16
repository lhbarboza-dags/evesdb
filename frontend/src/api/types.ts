// Mirrors backend records (org.sibol.eves.api.*). Keep in sync with the Java DTOs.

export interface Ref {
  id: number | null;
  code?: string | null;
  name?: string | null;
}

export const refLabel = (r?: Ref | null) => (r ? (r.code ? `${r.code} - ${r.name ?? ''}` : (r.name ?? '')) : '');

// ---------------- auth
export interface Me {
  authenticated: boolean;
  userId: number | null;
  email: string | null;
  firstName: string | null;
  lastName: string | null;
  role: number | null;
  roleName: string | null;
}
export interface MessageResponse {
  success: boolean;
  message: string;
}
export interface PublicConfig {
  recaptchaEnabled: boolean;
  recaptchaSiteKey: string;
  mapsApiKey: string;
}

/** org.sibol.eves.app.UserRole */
export const Role = { USER: 0, CURATOR: 1, MANAGER: 2, ADMIN: 3 } as const;
export const roleNames = ['User', 'Curator', 'Manager', 'Admin'];

// ---------------- study graph
export interface CountryInfoDto {
  id: number | null;
  countryId?: number | null;
  countryCode?: string | null;
  countryName?: string | null;
  year?: number | null;
  wbIncome?: string | null;
  gdpPerCapita?: string | null;
  popDensity?: string | null;
}
export interface ProtectedAreaDto {
  id: number | null;
  name: string;
  area?: string | null;
  established?: string | null;
  location?: string | null;
}
export interface LocationDto {
  id: number | null;
  name: string | null;
  countryInfo: CountryInfoDto | null;
  protectedStatus: Ref | null;
  protectedArea: ProtectedAreaDto | null;
}
export interface ClimateDto {
  id: number | null;
  deleted: boolean;
  climateType: Ref | null;
  climateClass: Ref | null;
  description: string | null;
}
export interface SiteAttrDto {
  id: number | null;
  deleted: boolean;
  attribute: Ref | null;
  otherValue: string | null;
}
export interface EcosystemDto {
  id: number | null;
  realm: Ref | null;
  realmDesc: string | null;
  biome: Ref | null;
  biomeDesc: string | null;
  efg: Ref | null;
  efgDesc: string | null;
}
export interface ServiceDto {
  id: number | null;
  eservice: Ref | null;
  serviceDesc: string | null;
  esubservice: Ref | null;
  subserviceDesc: string | null;
}
export interface AvailInfoDto {
  id: number | null;
  deleted: boolean;
  term: Ref | null;
}
export interface MethodDto {
  id: number | null;
  method: Ref | null;
  valuedChange: string | null;
  valuedEq: string | null;
  economicMeasure: Ref | null;
  surveyStudyInfo: string | null;
  availStudyInfo: AvailInfoDto[];
}
export interface ValueDto {
  id: number | null;
  valueType: Ref | null;
  valuePoint: string | null;
  aggregate: number | null;
  rangeLow: number | null;
  rangeHigh: number | null;
  currency: Ref | null;
  unit: Ref | null;
  year: string | null;
  pvYears: string | null;
  pvDiscountRate: string | null;
  beneficiaryType: Ref | null;
  beneficiaryNumber: number | null;
  beneficiaryUnit: Ref | null;
}
export interface DatasetDto {
  id: number | null;
  /** client-only key for unsaved rows */
  key?: string;
  deleted: boolean;
  ecosystem: EcosystemDto | null;
  service: ServiceDto | null;
  method: MethodDto | null;
  value: ValueDto | null;
}
export interface SiteDto {
  id: number | null;
  key?: string;
  deleted: boolean;
  name: string;
  latitude: string;
  longitude: string;
  area: string | null;
  areaUnit: string | null;
  scale: Ref | null;
  location: LocationDto | null;
  climates: ClimateDto[];
  attributes: SiteAttrDto[];
  datasets: DatasetDto[];
}
export interface StudyDto {
  id: number | null;
  title: string;
  abstractText: string | null;
  author: string;
  docType: Ref | null;
  pubYear: string | null;
  reference: string | null;
  externalLink: string | null;
  status: number | null;
  statusName?: string | null;
  codedBy?: string | null;
  codedDate?: string | null;
  reviewedBy?: string | null;
  reviewedDate?: string | null;
  sites: SiteDto[];
}
export interface StudyListRow {
  id: number;
  pubYear: number | null;
  title: string;
  submittedBy: string;
  codedDate: string | null;
  reviewedBy: string;
  reviewedDate: string | null;
  status: number;
  statusName: string;
}
export const StudyStatus = { PENDING: 0, APPROVED: 1, DISAPPROVED: 2 } as const;

// ---------------- search
export type EcoLevel = 'REALM' | 'BIOME' | 'EFG';
export type ServiceLevel = 'SERVICE' | 'SUB_SERVICE';
export interface EcoOption { level: EcoLevel; code: string; name: string; label: string }
export interface ServiceOption { level: ServiceLevel; code: string; name: string; label: string }
export interface MethodOption { code: string; name: string; label: string }
export interface Filters { ecosystems: EcoOption[]; services: ServiceOption[]; methods: MethodOption[]; countries: string[] }
export interface SearchRequest {
  keyword: string;
  matchAll: boolean;
  countries: string[];
  ecosystems: { level: EcoLevel; code: string }[];
  services: { level: ServiceLevel; code: string }[];
  methods: string[];
}
export interface Count { label: string; count: number }
export interface StudyRow {
  id: number;
  title: string;
  author: string;
  pubYear: number | null;
  docType: string | null;
  valueCount: number;
  byEcosystem: Count[];
  byService: Count[];
  byMethod: Count[];
  byEcosystemService: Count[];
}
export interface SearchResult { studyCount: number; valueCount: number; filterCounts: Count[]; studies: StudyRow[] }
export interface Summary { countries: Count[]; ecosystems: Count[]; services: Count[]; ecosystemValues: Count[]; serviceValues: Count[] }
export interface MapSite {
  studyId: number;
  title: string;
  siteId: number;
  siteName: string;
  country: string;
  lat: number | null;
  lng: number | null;
  latitude: string;
  longitude: string;
  values: string[];
}

// ---------------- dictionary / lookups
export interface CountryInfoRow { id: number; year: number; wbIncome: string; gdpPerCapita: string; popDensity: string }
export interface CountryRow { id: number; code: string; name: string; currency: Ref | null; infos: CountryInfoRow[] }
export interface ClimateClassRow { id: number; code: string; name: string; desc: string }
export interface ClimateTypeRow { id: number; name: string; desc: string; classes: ClimateClassRow[] }
export interface EfgRow { id: number; code: string; name: string; desc: string }
export interface BiomeRow { id: number; code: string; name: string; desc: string; efgs: EfgRow[] }
export interface RealmRow { id: number; code: string; name: string; desc: string; biomes: BiomeRow[] }
export interface SubServiceRow { id: number; code: string; name: string; desc: string }
export interface EServiceRow { id: number; code: string; name: string; desc: string; subservices: SubServiceRow[] }
export interface TermRow { id: number; cv: string; code: string; name: string; desc: string }
export interface Lookups {
  docTypes: Ref[];
  protectedStatuses: Ref[];
  scales: Ref[];
  siteAttributes: Ref[];
  valuationMethods: Ref[];
  economicMeasures: Ref[];
  availStudyInfo: Ref[];
  valueTypes: Ref[];
  currencies: Ref[];
  units: Ref[];
  beneficiaryTypes: Ref[];
  beneficiaryUnits: Ref[];
  climateTypes: ClimateTypeRow[];
  countries: CountryRow[];
  protectedAreas: ProtectedAreaDto[];
  realms: RealmRow[];
  eservices: EServiceRow[];
}

// ---------------- users
export type MailCategory = 'IMPROVEMENT' | 'INQUIRY' | 'BUG_REPORT' | 'SUGGEST_A_STUDY';
export const mailCategoryLabels: Record<MailCategory, string> = {
  IMPROVEMENT: 'Suggest Improvements',
  INQUIRY: 'Inquiry',
  BUG_REPORT: 'Bug Report',
  SUGGEST_A_STUDY: 'Suggest a study',
};
export interface UserRow { id: number; email: string; lastName: string; firstName: string; institution: string; role: number; roleName: string }
export type RecipientState = 'NONE' | 'INVITED' | 'ACCEPTED';
export interface Recipient { email: string; categories: Record<MailCategory, RecipientState> }

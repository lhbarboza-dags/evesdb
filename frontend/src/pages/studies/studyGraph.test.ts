import { describe, expect, it } from 'vitest';
import { dmsToDecimal, emptyDataset, emptySite, emptyStudy, validateDataset, validateSite, validateSitesStep, validateStudy } from './studyGraph';

describe('dmsToDecimal (AddSiteController.calculate)', () => {
  it('converts degrees/minutes/seconds', () => {
    expect(dmsToDecimal('14', '30', '0')).toBe('14.5');
    expect(dmsToDecimal('121', '0', '36')).toBe('121.01');
  });
  it('keeps the sign of negative degrees (legacy bug fixed)', () => {
    expect(dmsToDecimal('-14', '30', '0')).toBe('-14.5');
    expect(dmsToDecimal('-0', '30', '0')).toBe('-0.5');
  });
  it('returns "0" when a part is missing or invalid', () => {
    expect(dmsToDecimal('14', '', '0')).toBe('0');
    expect(dmsToDecimal('a', '1', '1')).toBe('0');
  });
});

describe('validation rules from the zul constraints', () => {
  it('study title/author required, year 19xx/20xx', () => {
    const s = { ...emptyStudy(), pubYear: '1850' };
    const e = validateStudy(s);
    expect(e.title).toBeDefined();
    expect(e.author).toBeDefined();
    expect(e.pubYear).toBeDefined();
    expect(validateStudy({ ...s, title: 'T', author: 'A', pubYear: '' })).toEqual({});
    expect(validateStudy({ ...s, title: 'T', author: 'A', pubYear: '2020' }, true).abstractText).toBeDefined();
  });
  it('site needs name, country, scale and decimal coordinates', () => {
    const e = validateSite(emptySite());
    expect(Object.keys(e).sort()).toEqual(['country', 'latitude', 'longitude', 'name', 'scale']);
    const ok = { ...emptySite(), name: 'S', latitude: '14.5', longitude: '-121', scale: { id: 1 }, location: { id: null, name: '', countryInfo: { id: 3, countryId: 1 }, protectedStatus: null, protectedArea: null } };
    expect(validateSite(ok)).toEqual({});
  });
  it('dataset numeric fields and year', () => {
    const d = emptyDataset();
    expect(validateDataset(d)).toEqual({});
    d.value!.year = '99';
    expect(validateDataset(d).year).toBeDefined();
  });
  it('wizard needs a site and a valuation per site', () => {
    const s = emptyStudy();
    expect(validateSitesStep(s)).toBe('Please add site first.');
    const site = { ...emptySite(), name: 'A' };
    s.sites = [site];
    expect(validateSitesStep(s)).toContain('There is an empty valuation for a site');
    site.datasets = [emptyDataset()];
    expect(validateSitesStep(s)).toBeNull();
    site.datasets[0].deleted = true;
    expect(validateSitesStep(s)).not.toBeNull();
  });
});

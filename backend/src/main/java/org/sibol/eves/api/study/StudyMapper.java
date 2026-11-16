package org.sibol.eves.api.study;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.sibol.eves.api.common.Ref;
import org.sibol.eves.api.study.StudyDtos.*;

import model.*;

/**
 * Entity → DTO mapping for the study graph. Soft-deleted children are left out, like the ZK renderers did.
 */
public final class StudyMapper {
	private StudyMapper() {}

	public static Ref term(Cvterm t) {
		return t == null ? null : new Ref(t.getCvtermid(), t.getCode(), t.getName());
	}

	public static String userName(User u) {
		if (u == null)
			return "NA";
		return (nz(u.getFirstname()) + " " + nz(u.getLastname())).trim();
	}

	/**
	 * Live sites of a study: its own sites plus sites reached through its datasets. The legacy search only walked
	 * {@code studysrc.getSites()}, which misses sites whose {@code studysrcid} is null (36 rows in the local DB),
	 * while the edit page only walked datasets. Both are merged here.
	 */
	public static List<Site> liveSites(Studysrc s) {
		Map<Integer, Site> sites = new LinkedHashMap<>();
		if (s.getSites() != null)
			for (Site site : s.getSites())
				if (!site.isIsdeleted())
					sites.putIfAbsent(site.getSiteid(), site);
		if (s.getDatasets() != null)
			for (Dataset ds : s.getDatasets())
				if (!ds.isIsdeleted() && ds.getSite() != null && !ds.getSite().isIsdeleted())
					sites.putIfAbsent(ds.getSite().getSiteid(), ds.getSite());
		return new ArrayList<>(sites.values());
	}

	/** Live datasets of a site that belong to the given study. */
	public static List<Dataset> liveDatasets(Site site, Studysrc study) {
		List<Dataset> out = new ArrayList<>();
		if (site.getDatasets() != null)
			for (Dataset ds : site.getDatasets())
				if (!ds.isIsdeleted() && (ds.getStudysrc() == null || study == null
						|| ds.getStudysrc().getSrcid() == study.getSrcid()))
					out.add(ds);
		return out;
	}

	public static StudyDto toDto(Studysrc s) {
		List<SiteDto> sites = new ArrayList<>();
		for (Site site : liveSites(s))
			sites.add(site(site, s));
		return new StudyDto(s.getSrcid(), s.getTitle(), s.getAbstract_(), s.getAuthor(), term(s.getDoctype()),
				s.getPubyear() == 0 ? "" : String.valueOf(s.getPubyear()), s.getReference(), s.getDwnlinkext(),
				StudyStatus.of(s.getStatus()), StudyStatus.name(s.getStatus()), userName(s.getCodedby()),
				s.getCodeddate(), s.getReviewedby() == null ? null : userName(s.getReviewedby()), s.getRevieweddate(),
				sites);
	}

	public static StudyListRow toRow(Studysrc s) {
		return new StudyListRow(s.getSrcid(), s.getPubyear() == 0 ? null : s.getPubyear(), s.getTitle(),
				userName(s.getCodedby()), s.getCodeddate(), s.getReviewedby() == null ? "NA" : userName(s.getReviewedby()),
				s.getRevieweddate(), StudyStatus.of(s.getStatus()), StudyStatus.name(s.getStatus()));
	}

	public static SiteDto site(Site site, Studysrc study) {
		List<ClimateDto> climates = new ArrayList<>();
		if (site.getClimates() != null)
			for (Climate c : site.getClimates())
				if (!c.isIsdeleted())
					climates.add(new ClimateDto(c.getClimateid(), false,
							c.getClimatetype() == null ? null : Ref.of(c.getClimatetype().getCltid(), null, c.getClimatetype().getCltname()),
							c.getClimateclass() == null ? null
									: Ref.of(c.getClimateclass().getCcid(), c.getClimateclass().getCcCode(), c.getClimateclass().getCcName()),
							c.getClimatedesc()));
		List<SiteAttrDto> attrs = new ArrayList<>();
		if (site.getSiteattrs() != null)
			for (Siteattr a : site.getSiteattrs())
				if (!a.getIsdeleted())
					attrs.add(new SiteAttrDto(a.getSiteattrid(), false, term(a.getAreaattr()), a.getOvalue()));
		List<DatasetDto> datasets = new ArrayList<>();
		for (Dataset ds : liveDatasets(site, study))
			datasets.add(dataset(ds));
		return new SiteDto(site.getSiteid(), false, site.getName(), site.getLatitude(), site.getLongitude(),
				site.getArea(), site.getAreaunit(), term(site.getScale()), location(site.getLocation()), climates, attrs,
				datasets);
	}

	public static LocationDto location(Location l) {
		if (l == null)
			return null;
		CountryInfoDto ci = null;
		Countryinfo c = l.getCountryinfo();
		if (c != null) {
			Country country = c.getCountry();
			ci = new CountryInfoDto(c.getCiid(), country == null ? null : country.getCid(),
					country == null ? null : country.getCcode(), country == null ? null : country.getCname(), c.getYear(),
					c.getWbincome(), c.getGdpperincome(), c.getPopdensity());
		}
		Protectedarea pa = l.getProtectedarea();
		ProtectedAreaDto paDto = pa == null ? null
				: new ProtectedAreaDto(pa.getPaid(), pa.getName(), pa.getArea(), pa.getEstablished(), pa.getLocation());
		return new LocationDto(l.getLocationid(), l.getLocationname(), ci, term(l.getProtectedStatus()), paDto);
	}

	public static DatasetDto dataset(Dataset ds) {
		EcosystemDto eco = null;
		Ecosystem e = ds.getEcosystem();
		if (e != null)
			eco = new EcosystemDto(e.getEcosysid(),
					e.getRealm() == null ? null : Ref.of(e.getRealm().getRealmid(), e.getRealm().getCode(), e.getRealm().getName()),
					e.getRdesc(),
					e.getBiome() == null ? null : Ref.of(e.getBiome().getBiomeid(), e.getBiome().getBcode(), e.getBiome().getBname()),
					e.getBdesc(),
					e.getEfgrp() == null ? null : Ref.of(e.getEfgrp().getEfgrpid(), e.getEfgrp().getCode(), e.getEfgrp().getName()),
					e.getEfgdesc());
		ServiceDto serv = null;
		Service s = ds.getService();
		if (s != null)
			serv = new ServiceDto(s.getServiceid(),
					s.getEservice() == null ? null
							: Ref.of(s.getEservice().getEserviceid(), s.getEservice().getCode(), s.getEservice().getName()),
					s.getEsdesc(),
					s.getEsubservice() == null ? null
							: Ref.of(s.getEsubservice().getEsubserviceid(), s.getEsubservice().getCode(), s.getEsubservice().getName()),
					s.getEsubsdesc());
		MethodDto meth = null;
		Valuationmethod vm = ds.getValuationmethod();
		if (vm != null) {
			List<AvailInfoDto> avail = new ArrayList<>();
			if (vm.getMethdstudyinfos() != null)
				for (Methdstudyinfo m : vm.getMethdstudyinfos())
					if (!m.isIsdeleted() && m.getCvterm() != null)
						avail.add(new AvailInfoDto(m.getMethdstudyinfoid(), false, term(m.getCvterm())));
			meth = new MethodDto(vm.getVmethodid(), term(vm.getMethod()), vm.getValuedchange(), vm.getValuedeq(),
					term(vm.getEcomeasure()), vm.getSurveystudyinfo(), avail);
		}
		ValueDto val = null;
		Value v = ds.getValue();
		if (v != null)
			val = new ValueDto(v.getValueid(), term(v.getValuetype()), v.getValuept(), v.getVaggregate(),
					v.getValuerangelow(), v.getValuerangehigh(), term(v.getValue_currency()), term(v.getUnitid()), v.getYear(),
					v.getPvyears(), v.getPvdiscrate(), term(v.getBeneficiarytype()), v.getBeneficiarynumber(),
					term(v.getBeneficiaryunit()));
		return new DatasetDto(ds.getDatasetid(), false, eco, serv, meth, val);
	}

	private static String nz(String s) {
		return s == null ? "" : s;
	}
}

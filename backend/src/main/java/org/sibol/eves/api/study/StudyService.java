package org.sibol.eves.api.study;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.sibol.eves.api.common.ApiException;
import org.sibol.eves.api.common.Ref;
import org.sibol.eves.api.study.StudyDtos.*;
import org.sibol.eves.data.*;
import org.sibol.eves.security.AppUser;
import org.sibol.eves.security.CurrentUser;
import org.sibol.eves.security.Role;

import org.springframework.transaction.annotation.Transactional;

import model.*;

/**
 * Study data management. Ports AddStudyController.recordSave, EditStudyController.saveRecord,
 * ViewStudyController (list/search/approval/delete) and the in-memory edits done by AddSiteController,
 * AddSiteValuationController and EditSiteValuationController.
 *
 * Differences from ZK (see docs/migration/BEHAVIOUR_CHANGES.md): one transaction for the whole graph; new sites added
 * while editing get their studysrc; deleting a site also soft-deletes its datasets; approval uses ids, not row indexes.
 */
@org.springframework.stereotype.Service
@Transactional
public class StudyService {

	private final EntityManager em;
	private final StudyRepo studies;
	private final UserRepo users;
	private final CvtermRepo terms;
	private final SiteRepo sites;
	private final LocationRepo locations;
	private final ClimateRepo climates;
	private final ClimatetypeRepo climateTypes;
	private final SiteattrRepo siteAttrs;
	private final DatasetRepo datasets;
	private final EcosystemRepo ecosystems;
	private final ServiceRepo services;
	private final ValuationmethodRepo methods;
	private final MethdstudyinfoRepo methodInfos;
	private final ValueRepo values;
	private final CountryinfoRepo countryInfos;
	private final ProtectedareaRepo protectedAreas;
	private final RealmRepo realms;
	private final BiomeRepo biomes;
	private final EfgrpRepo efgs;
	private final EserviceRepo eservices;
	private final EsubserviceRepo esubservices;

	public StudyService(EntityManager em, StudyRepo studies, UserRepo users, CvtermRepo terms, SiteRepo sites,
			LocationRepo locations, ClimateRepo climates, ClimatetypeRepo climateTypes, SiteattrRepo siteAttrs,
			DatasetRepo datasets, EcosystemRepo ecosystems, ServiceRepo services, ValuationmethodRepo methods,
			MethdstudyinfoRepo methodInfos, ValueRepo values, CountryinfoRepo countryInfos,
			ProtectedareaRepo protectedAreas, RealmRepo realms, BiomeRepo biomes, EfgrpRepo efgs,
			EserviceRepo eservices, EsubserviceRepo esubservices) {
		this.em = em;
		this.studies = studies;
		this.users = users;
		this.terms = terms;
		this.sites = sites;
		this.locations = locations;
		this.climates = climates;
		this.climateTypes = climateTypes;
		this.siteAttrs = siteAttrs;
		this.datasets = datasets;
		this.ecosystems = ecosystems;
		this.services = services;
		this.methods = methods;
		this.methodInfos = methodInfos;
		this.values = values;
		this.countryInfos = countryInfos;
		this.protectedAreas = protectedAreas;
		this.realms = realms;
		this.biomes = biomes;
		this.efgs = efgs;
		this.eservices = eservices;
		this.esubservices = esubservices;
	}

	// ------------------------------------------------------------------ read

	@Transactional(readOnly = true)
	public Studysrc load(int id) {
		Studysrc s = studies.findById(id).orElseThrow(() -> ApiException.notFound("Study not found."));
		if (s.isIsdeleted())
			throw ApiException.notFound("Study not found.");
		return s;
	}

	@Transactional(readOnly = true)
	public StudyDto get(int id) {
		return StudyMapper.toDto(load(id));
	}

	/**
	 * ViewStudyController: combobox_approval (status filter) + btn_searchStudy (text filter).
	 * Text matches title (contains), publication year (equals), a site's country name, or a dataset's
	 * realm/biome/EFG/e-service/e-sub-service name (equals, case-insensitive).
	 */
	@Transactional(readOnly = true)
	public List<StudyListRow> list(Integer status, String q) {
		String text = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
		List<StudyListRow> out = new ArrayList<>();
		for (Studysrc s : studies.findActive()) {
			if (status != null && StudyStatus.of(s.getStatus()) != status)
				continue;
			if (!text.isEmpty() && !matchesText(s, text))
				continue;
			out.add(StudyMapper.toRow(s));
		}
		return out;
	}

	private boolean matchesText(Studysrc s, String text) {
		if (s.getTitle() != null && s.getTitle().toLowerCase(Locale.ROOT).contains(text))
			return true;
		if (String.valueOf(s.getPubyear()).equals(text))
			return true;
		for (Site site : StudyMapper.liveSites(s)) {
			Location l = site.getLocation();
			if (l != null && l.getCountryinfo() != null && l.getCountryinfo().getCountry() != null
					&& eq(l.getCountryinfo().getCountry().getCname(), text))
				return true;
			for (Dataset ds : StudyMapper.liveDatasets(site, s)) {
				Ecosystem e = ds.getEcosystem();
				if (e != null && ((e.getRealm() != null && eq(e.getRealm().getName(), text))
						|| (e.getBiome() != null && eq(e.getBiome().getBname(), text))
						|| (e.getEfgrp() != null && eq(e.getEfgrp().getName(), text))))
					return true;
				Service sv = ds.getService();
				if (sv != null && ((sv.getEservice() != null && eq(sv.getEservice().getName(), text))
						|| (sv.getEsubservice() != null && eq(sv.getEsubservice().getName(), text))))
					return true;
			}
		}
		return false;
	}

	private static boolean eq(String a, String lowerText) {
		return a != null && a.trim().toLowerCase(Locale.ROOT).equals(lowerText);
	}

	// ------------------------------------------------------------------ bulk actions (study_view.zul)

	/** btn_approve / btn_pending / btn_disapprove → studyApproval(n). Manager+ only. */
	public void setStatus(StatusRequest req) {
		if (req.status() < 0 || req.status() > 2)
			throw ApiException.badRequest("Unknown status.");
		if (req.ids() == null || req.ids().isEmpty())
			return;
		User reviewer = currentUserEntity();
		for (Integer id : req.ids()) {
			Studysrc s = load(id);
			s.setStatus(req.status());
			s.setReviewedby(reviewer);
			s.setRevieweddate(LocalDateTime.now().toString());
			studies.save(s);
		}
	}

	/** btn_delete: soft delete. Manager+ only. */
	public void delete(IdsRequest req) {
		if (req.ids() == null || req.ids().isEmpty())
			return;
		User reviewer = currentUserEntity();
		for (Integer id : req.ids()) {
			Studysrc s = load(id);
			s.setIsdeleted(true);
			s.setReviewedby(reviewer);
			s.setRevieweddate(LocalDateTime.now().toString());
			studies.save(s);
		}
	}

	// ------------------------------------------------------------------ create / update

	/** _study.zul btn_finish → AddStudyController.recordSave. */
	public StudyDto create(StudyDto dto) {
		validateGraph(dto);
		Studysrc src = new Studysrc();
		src.setCodedby(currentUserEntity());
		src.setCodeddate(LocalDateTime.now().toString());
		src.setReviewedby(null);
		src.setRevieweddate(null);
		src.setStatus(StudyStatus.PENDING);
		src.setIsdeleted(false);
		applyStudyFields(src, dto);
		src = studies.save(src);
		saveSites(src, dto.sites());
		return reload(src.getSrcid());
	}

	/** editstudy.zul buttonSave → EditStudyController.saveRecord. */
	public StudyDto update(int id, StudyDto dto) {
		Studysrc src = load(id);
		if (StudyStatus.of(src.getStatus()) == StudyStatus.APPROVED)
			throw ApiException.conflict("This study is already approved and so, it cannot be edited.");
		validateGraph(dto);
		applyStudyFields(src, dto);
		AppUser me = CurrentUser.require();
		if (dto.status() != null && dto.status() != StudyStatus.of(src.getStatus())) {
			if (me.getRole().id < Role.MANAGER.id)
				throw new org.springframework.security.access.AccessDeniedException("Only managers can change the status.");
			src.setStatus(dto.status());
			src.setReviewedby(currentUserEntity());
			src.setRevieweddate(LocalDateTime.now().toString());
		}
		studies.save(src);
		saveSites(src, dto.sites());
		return reload(src.getSrcid());
	}

	private StudyDto reload(int id) {
		em.flush();
		em.clear();
		return StudyMapper.toDto(studies.findById(id).orElseThrow());
	}

	/** btnNext_step3_b rules: "Please add site first." / "There is an empty valuation for a site." */
	private void validateGraph(StudyDto dto) {
		List<SiteDto> live = dto.sites() == null ? List.of() : dto.sites().stream().filter(s -> !s.deleted()).toList();
		if (live.isEmpty())
			throw ApiException.badRequest("Please add site first.");
		for (SiteDto s : live) {
			boolean hasValue = s.datasets() != null && s.datasets().stream().anyMatch(d -> !d.deleted());
			if (!hasValue)
				throw ApiException.badRequest("There is an empty valuation for a site: " + s.name());
		}
	}

	private void applyStudyFields(Studysrc src, StudyDto dto) {
		src.setTitle(dto.title().trim());
		src.setAbstract_(nz(dto.abstractText()));
		src.setAuthor(dto.author().trim());
		src.setDoctype(term(dto.docType()));
		src.setPubyear(dto.pubYear() == null || dto.pubYear().isBlank() ? 0 : Integer.parseInt(dto.pubYear().trim()));
		src.setReference(nz(dto.reference()));
		src.setDwnlinkext(nz(dto.externalLink()));
	}

	private void saveSites(Studysrc src, List<SiteDto> siteDtos) {
		if (siteDtos == null)
			return;
		for (SiteDto sd : siteDtos) {
			Site site = sd.id() == null ? new Site() : sites.findById(sd.id()).orElseThrow(() -> ApiException.notFound("Site not found."));
			site.setIsdeleted(sd.deleted());
			site.setName(sd.name().trim());
			site.setLatitude(sd.latitude().trim());
			site.setLongitude(sd.longitude().trim());
			site.setArea(nz(sd.area()));
			site.setAreaunit(nz(sd.areaUnit()));
			site.setScale(term(sd.scale()));
			site.setStudysrc(src);
			site.setLocation(saveLocation(site.getLocation(), sd.location()));
			site = sites.save(site);
			saveClimates(site, sd.climates());
			saveAttributes(site, sd.attributes());
			if (sd.datasets() != null)
				for (DatasetDto dd : sd.datasets())
					saveDataset(src, site, dd, sd.deleted());
		}
	}

	/** AddSiteController.buttonSave builds a Location with locationcode/latitude/longitude = "". */
	private Location saveLocation(Location loc, LocationDto dto) {
		if (dto == null)
			return loc;
		if (loc == null) {
			loc = new Location();
			loc.setLocationcode("");
			loc.setLatitude("");
			loc.setLongitude("");
		}
		loc.setLocationname(nz(dto.name()));
		loc.setCountryinfo(dto.countryInfo() == null || dto.countryInfo().id() == null ? null
				: countryInfos.findById(dto.countryInfo().id()).orElseThrow(() -> ApiException.badRequest("Unknown country year.")));
		loc.setProtectedStatus(term(dto.protectedStatus()));
		loc.setProtectedarea(dto.protectedArea() == null || dto.protectedArea().id() == null ? null
				: protectedAreas.findById(dto.protectedArea().id()).orElseThrow(() -> ApiException.badRequest("Unknown protected area.")));
		return locations.save(loc);
	}

	/** listbox_climate multi-select: kept rows revived, new ones created, unselected ones soft-deleted. */
	private void saveClimates(Site site, List<ClimateDto> wanted) {
		Set<Integer> wantedTypes = new HashSet<>();
		if (wanted != null)
			for (ClimateDto c : wanted)
				if (!c.deleted() && c.climateType() != null)
					wantedTypes.add(c.climateType().id());
		Map<Integer, Climate> existing = new HashMap<>();
		if (site.getClimates() != null)
			for (Climate c : site.getClimates())
				if (c.getClimatetype() != null)
					existing.putIfAbsent(c.getClimatetype().getCltid(), c);
		for (Map.Entry<Integer, Climate> e : existing.entrySet()) {
			e.getValue().setIsdeleted(!wantedTypes.contains(e.getKey()));
			climates.save(e.getValue());
		}
		for (Integer type : wantedTypes)
			if (!existing.containsKey(type)) {
				Climate c = new Climate();
				c.setSite(site);
				c.setClimatetype(climateTypes.findById(type).orElseThrow(() -> ApiException.badRequest("Unknown climate type.")));
				c.setIsdeleted(false);
				climates.save(c);
			}
	}

	/** listbox_attr multi-select (+ txtboxAttrOthers for the "Others" attribute). */
	private void saveAttributes(Site site, List<SiteAttrDto> wanted) {
		Map<Integer, String> wantedTerms = new HashMap<>();
		if (wanted != null)
			for (SiteAttrDto a : wanted)
				if (!a.deleted() && a.attribute() != null)
					wantedTerms.put(a.attribute().id(), nz(a.otherValue()));
		Map<Integer, Siteattr> existing = new HashMap<>();
		if (site.getSiteattrs() != null)
			for (Siteattr a : site.getSiteattrs())
				if (a.getAreaattr() != null)
					existing.putIfAbsent(a.getAreaattr().getCvtermid(), a);
		for (Map.Entry<Integer, Siteattr> e : existing.entrySet()) {
			Siteattr a = e.getValue();
			a.setIsdeleted(!wantedTerms.containsKey(e.getKey()));
			if (wantedTerms.containsKey(e.getKey()))
				a.setOvalue(wantedTerms.get(e.getKey()));
			siteAttrs.save(a);
		}
		for (Map.Entry<Integer, String> e : wantedTerms.entrySet())
			if (!existing.containsKey(e.getKey())) {
				Siteattr a = new Siteattr();
				a.setSite(site);
				a.setAreaattr(terms.findById(e.getKey()).orElseThrow(() -> ApiException.badRequest("Unknown site attribute.")));
				a.setOvalue(e.getValue());
				a.setIsdeleted(false);
				siteAttrs.save(a);
			}
	}

	private void saveDataset(Studysrc src, Site site, DatasetDto dd, boolean siteDeleted) {
		Dataset ds = dd.id() == null ? new Dataset() : datasets.findById(dd.id()).orElseThrow(() -> ApiException.notFound("Valuation not found."));
		ds.setIsdeleted(dd.deleted() || siteDeleted);
		ds.setStudysrc(src);
		ds.setSite(site);

		EcosystemDto ed = dd.ecosystem();
		if (ed != null) {
			Ecosystem e = ds.getEcosystem() != null ? ds.getEcosystem() : new Ecosystem();
			e.setRealm(ed.realm() == null || ed.realm().id() == null ? null : realms.findById(ed.realm().id()).orElse(null));
			e.setBiome(ed.biome() == null || ed.biome().id() == null ? null : biomes.findById(ed.biome().id()).orElse(null));
			e.setEfgrp(ed.efg() == null || ed.efg().id() == null ? null : efgs.findById(ed.efg().id()).orElse(null));
			e.setRdesc(nz(ed.realmDesc()));
			e.setBdesc(nz(ed.biomeDesc()));
			e.setEfgdesc(nz(ed.efgDesc()));
			ds.setEcosystem(ecosystems.save(e));
		}

		ServiceDto sd = dd.service();
		if (sd != null) {
			Service s = ds.getService() != null ? ds.getService() : new Service();
			s.setEservice(sd.eservice() == null || sd.eservice().id() == null ? null : eservices.findById(sd.eservice().id()).orElse(null));
			s.setEsubservice(sd.esubservice() == null || sd.esubservice().id() == null ? null
					: esubservices.findById(sd.esubservice().id()).orElse(null));
			s.setEsdesc(nz(sd.serviceDesc()));
			s.setEsubsdesc(nz(sd.subserviceDesc()));
			ds.setService(services.save(s));
		}

		MethodDto md = dd.method();
		if (md != null) {
			Valuationmethod vm = ds.getValuationmethod() != null ? ds.getValuationmethod() : new Valuationmethod();
			vm.setMethod(term(md.method()));
			vm.setEcomeasure(term(md.economicMeasure()));
			vm.setValuedchange(nz(md.valuedChange()));
			vm.setValuedeq(nz(md.valuedEq()));
			vm.setSurveystudyinfo(nz(md.surveyStudyInfo()));
			vm = methods.save(vm);
			saveAvailInfo(vm, md.availStudyInfo());
			ds.setValuationmethod(vm);
		}

		ValueDto vd = dd.value();
		if (vd != null) {
			Value v = ds.getValue() != null ? ds.getValue() : new Value();
			v.setValuetype(term(vd.valueType()));
			v.setValuept(nz(vd.valuePoint()));
			v.setVaggregate(vd.aggregate());
			v.setValuerangelow(vd.rangeLow());
			v.setValuerangehigh(vd.rangeHigh());
			v.setValue_currency(term(vd.currency()));
			v.setUnitid(term(vd.unit()));
			v.setYear(nz(vd.year()));
			v.setPvyears(nz(vd.pvYears()));
			v.setPvdiscrate(nz(vd.pvDiscountRate()));
			v.setBeneficiarytype(term(vd.beneficiaryType()));
			v.setBeneficiarynumber(vd.beneficiaryNumber());
			v.setBeneficiaryunit(term(vd.beneficiaryUnit()));
			ds.setValue(values.save(v));
		}
		datasets.save(ds);
	}

	/** listbox_availinfo multi-select → methdstudyinfo rows (revive / create / soft-delete). */
	private void saveAvailInfo(Valuationmethod vm, List<AvailInfoDto> wanted) {
		Set<Integer> wantedTerms = new HashSet<>();
		if (wanted != null)
			for (AvailInfoDto a : wanted)
				if (!a.deleted() && a.term() != null)
					wantedTerms.add(a.term().id());
		Map<Integer, Methdstudyinfo> existing = new HashMap<>();
		if (vm.getMethdstudyinfos() != null)
			for (Methdstudyinfo m : vm.getMethdstudyinfos())
				if (m.getCvterm() != null)
					existing.putIfAbsent(m.getCvterm().getCvtermid(), m);
		for (Map.Entry<Integer, Methdstudyinfo> e : existing.entrySet()) {
			e.getValue().setIsdeleted(!wantedTerms.contains(e.getKey()));
			methodInfos.save(e.getValue());
		}
		for (Integer t : wantedTerms)
			if (!existing.containsKey(t)) {
				Methdstudyinfo m = new Methdstudyinfo();
				m.setValuationmethod(vm);
				m.setCvterm(terms.findById(t).orElseThrow(() -> ApiException.badRequest("Unknown study info term.")));
				m.setIsdeleted(false);
				methodInfos.save(m);
			}
	}

	// ------------------------------------------------------------------ helpers

	private Cvterm term(Ref ref) {
		if (ref == null || ref.id() == null)
			return null;
		return terms.findById(ref.id()).orElseThrow(() -> ApiException.badRequest("Unknown term " + ref.id() + "."));
	}

	private User currentUserEntity() {
		return users.findById(CurrentUser.require().getUserId())
				.orElseThrow(() -> ApiException.unauthorized("Your session has expired. Please log in again."));
	}

	private static String nz(String s) {
		return s == null ? "" : s.trim();
	}
}

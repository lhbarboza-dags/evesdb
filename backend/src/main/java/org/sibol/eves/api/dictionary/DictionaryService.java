package org.sibol.eves.api.dictionary;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.sibol.eves.api.common.ApiException;
import org.sibol.eves.api.common.Ref;
import org.sibol.eves.api.dictionary.DictionaryDtos.*;
import org.sibol.eves.api.study.StudyDtos.ProtectedAreaDto;
import org.sibol.eves.api.study.StudyMapper;
import org.sibol.eves.data.*;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import model.*;

/**
 * Port of DictionaryController and the controllers in org.sibol.eves.controller.dictionary.
 * Deletes are soft (isdeleted=true) for the tables that have the flag, exactly as before.
 */
@org.springframework.stereotype.Service
@Transactional
public class DictionaryService {

	/**
	 * CVs editable from the dictionary page. "PA Category" is the union of PAC1/PAC2/PAC3 (the ZK renderer passed the
	 * non-existent CV name "PA Category", which made edit crash).
	 */
	public static final Map<String, List<String>> EDITABLE_CVS = Map.of(
			"Currency", List.of("Currency"),
			"Available study Info", List.of("Available study Info"),
			"Beneficiary Unit", List.of("Beneficiary Unit"),
			"Beneficiary Type", List.of("Beneficiary Type"),
			"Valuation Method", List.of("Valuation Method"),
			"PA Category", List.of("PAC1", "PAC2", "PAC3"));

	private final CvtermRepo terms;
	private final CountryRepo countries;
	private final CountryinfoRepo countryInfos;
	private final ProtectedareaRepo protectedAreas;
	private final ClimatetypeRepo climateTypes;
	private final ClimateclassRepo climateClasses;
	private final RealmRepo realms;
	private final BiomeRepo biomes;
	private final EfgrpRepo efgs;
	private final EserviceRepo eservices;
	private final EsubserviceRepo esubservices;

	public DictionaryService(CvtermRepo terms, CountryRepo countries, CountryinfoRepo countryInfos,
			ProtectedareaRepo protectedAreas, ClimatetypeRepo climateTypes, ClimateclassRepo climateClasses,
			RealmRepo realms, BiomeRepo biomes, EfgrpRepo efgs, EserviceRepo eservices, EsubserviceRepo esubservices) {
		this.terms = terms;
		this.countries = countries;
		this.countryInfos = countryInfos;
		this.protectedAreas = protectedAreas;
		this.climateTypes = climateTypes;
		this.climateClasses = climateClasses;
		this.realms = realms;
		this.biomes = biomes;
		this.efgs = efgs;
		this.eservices = eservices;
		this.esubservices = esubservices;
	}

	// ================================================================== lookups (read)

	@Transactional(readOnly = true)
	public Lookups lookups() {
		return new Lookups(refs("Document Type"), refs("Protected Status"), refs("Scale"), refs("Site Attribute"),
				refs("Valuation Method"), refs("Economic Measure"), refs("Available study Info"), refs("Value Type"),
				refs("Currency"), refs("Unit"), refs("Beneficiary Type"), refs("Beneficiary Unit"), climateTree(),
				countryList(), paList(), realmTree(), serviceTree());
	}

	private List<Ref> refs(String cv) {
		return terms.findByCvNames(List.of(cv), false).stream().map(StudyMapper::term).toList();
	}

	@Transactional(readOnly = true)
	public List<TermRow> terms(String group) {
		List<String> cvs = cvsOf(group);
		return terms.findByCvNames(cvs, false).stream()
				.map(t -> new TermRow(t.getCvtermid(), t.getCv() == null ? null : t.getCv().getCvname(), t.getCode(), t.getName(), t.getDesc()))
				.toList();
	}

	@Transactional(readOnly = true)
	public List<CountryRow> countryList() {
		List<CountryRow> out = new ArrayList<>();
		for (Country c : countries.findAll(Sort.by("cname"))) {
			if (c.isIsdeleted())
				continue;
			List<CountryInfoRow> infos = new ArrayList<>();
			if (c.getCountryinfos() != null)
				c.getCountryinfos().stream().filter(i -> !i.isDeleted()).sorted(Comparator.comparingInt(Countryinfo::getYear))
						.forEach(i -> infos.add(new CountryInfoRow(i.getCiid(), i.getYear(), i.getWbincome(), i.getGdpperincome(), i.getPopdensity())));
			out.add(new CountryRow(c.getCid(), c.getCcode(), c.getCname(), StudyMapper.term(c.getCurrency()), infos));
		}
		return out;
	}

	@Transactional(readOnly = true)
	public List<ProtectedAreaDto> paList() {
		return protectedAreas.findAll(Sort.by("name")).stream().filter(p -> !p.isIsdeleted())
				.map(p -> new ProtectedAreaDto(p.getPaid(), p.getName(), p.getArea(), p.getEstablished(), p.getLocation())).toList();
	}

	@Transactional(readOnly = true)
	public List<ClimateTypeRow> climateTree() {
		List<ClimateTypeRow> out = new ArrayList<>();
		for (Climatetype t : climateTypes.findAll(Sort.by("cltname"))) {
			List<ClimateClassRow> classes = new ArrayList<>();
			if (t.getClimateclasses() != null)
				t.getClimateclasses().stream().sorted(Comparator.comparing(c -> nz(c.getCcCode())))
						.forEach(c -> classes.add(new ClimateClassRow(c.getCcid(), c.getCcCode(), c.getCcName(), c.getCcDesc())));
			out.add(new ClimateTypeRow(t.getCltid(), t.getCltname(), t.getCltDesc(), classes));
		}
		return out;
	}

	@Transactional(readOnly = true)
	public List<RealmRow> realmTree() {
		List<RealmRow> out = new ArrayList<>();
		for (Realm r : realms.findAll(Sort.by("code"))) {
			List<BiomeRow> bl = new ArrayList<>();
			if (r.getBiomes() != null)
				r.getBiomes().stream().sorted(Comparator.comparing(b -> nz(b.getBcode()))).forEach(b -> {
					List<EfgRow> el = new ArrayList<>();
					if (b.getEfgrps() != null)
						b.getEfgrps().stream().sorted(Comparator.comparing(e -> nz(e.getCode())))
								.forEach(e -> el.add(new EfgRow(e.getEfgrpid(), e.getCode(), e.getName(), e.getDesc())));
					bl.add(new BiomeRow(b.getBiomeid(), b.getBcode(), b.getBname(), b.getBdesc(), el));
				});
			out.add(new RealmRow(r.getRealmid(), r.getCode(), r.getName(), r.getDesc(), bl));
		}
		return out;
	}

	@Transactional(readOnly = true)
	public List<EServiceRow> serviceTree() {
		List<EServiceRow> out = new ArrayList<>();
		for (Eservice s : eservices.findAll(Sort.by("code"))) {
			List<SubServiceRow> subs = new ArrayList<>();
			if (s.getEsubservices() != null)
				s.getEsubservices().stream().sorted(Comparator.comparing(x -> nz(x.getCode())))
						.forEach(x -> subs.add(new SubServiceRow(x.getEsubserviceid(), x.getCode(), x.getName(), x.getDesc())));
			out.add(new EServiceRow(s.getEserviceid(), s.getCode(), s.getName(), s.getDesc(), subs));
		}
		return out;
	}

	// ================================================================== CV terms

	/** Add*Dialog (Currency, Beneficiary Unit/Type, Method, Available study Info, PA Category) → CvTermService.save. */
	public TermRow createTerm(TermWrite w) {
		List<String> cvs = cvsOf(w.cv());
		String cvName = cvs.size() == 1 ? cvs.get(0) : w.cv();
		if (!cvs.contains(cvName))
			throw ApiException.badRequest("Choose one of " + String.join(", ", cvs) + ".");
		Cv cv = terms.findCv(cvName).stream().findFirst().orElseThrow(() -> ApiException.badRequest("Unknown vocabulary " + cvName + "."));
		Cvterm t = new Cvterm();
		t.setCv(cv); // fixed: MethodController never set the CV for new valuation methods
		t.setIsdeleted(false);
		applyTerm(t, w);
		t = terms.save(t);
		return new TermRow(t.getCvtermid(), cvName, t.getCode(), t.getName(), t.getDesc());
	}

	/** Row edit pencil → AddCvTermDialog (WHOController) → save. */
	public TermRow updateTerm(int id, TermWrite w) {
		Cvterm t = terms.findById(id).orElseThrow(() -> ApiException.notFound("Term not found."));
		applyTerm(t, w);
		terms.save(t);
		return new TermRow(t.getCvtermid(), t.getCv() == null ? null : t.getCv().getCvname(), t.getCode(), t.getName(), t.getDesc());
	}

	/** Row delete × → confirm → isdeleted = true. */
	public void deleteTerm(int id) {
		Cvterm t = terms.findById(id).orElseThrow(() -> ApiException.notFound("Term not found."));
		t.setIsdeleted(true);
		terms.save(t);
	}

	private void applyTerm(Cvterm t, TermWrite w) {
		t.setCode(w.code().trim());
		t.setName(w.name().trim());
		t.setDesc(nz(w.desc()).trim());
	}

	private static List<String> cvsOf(String group) {
		List<String> cvs = EDITABLE_CVS.get(group);
		if (cvs == null) {
			for (List<String> l : EDITABLE_CVS.values())
				if (l.contains(group))
					return l;
			throw ApiException.badRequest("This vocabulary cannot be edited here.");
		}
		return cvs;
	}

	// ================================================================== countries

	/** AddCountryDialog (new). */
	public CountryRow createCountry(CountryWrite w) {
		for (Country c : countries.findAll())
			if (!c.isIsdeleted() && (eq(c.getCcode(), w.code()) || eq(c.getCname(), w.name())))
				throw ApiException.conflict("Record already exists.");
		Country c = new Country();
		c.setIsdeleted(false);
		applyCountry(c, w);
		return toRow(countries.save(c));
	}

	/** AddCountryDialog (edit, arg "country"). */
	public CountryRow updateCountry(int id, CountryWrite w) {
		Country c = countries.findById(id).orElseThrow(() -> ApiException.notFound("Country not found."));
		applyCountry(c, w);
		return toRow(countries.save(c));
	}

	public void deleteCountry(int id) {
		Country c = countries.findById(id).orElseThrow(() -> ApiException.notFound("Country not found."));
		c.setIsdeleted(true);
		countries.save(c);
	}

	private void applyCountry(Country c, CountryWrite w) {
		c.setCcode(w.code().trim());
		c.setCname(w.name().trim());
		c.setCurrency(w.currency() == null || w.currency().id() == null ? null
				: terms.findById(w.currency().id()).orElseThrow(() -> ApiException.badRequest("Unknown currency.")));
	}

	private CountryRow toRow(Country c) {
		return new CountryRow(c.getCid(), c.getCcode(), c.getCname(), StudyMapper.term(c.getCurrency()), List.of());
	}

	/** AddCountryInfoDialog: staged years, duplicate check against staged rows and the country's existing years. */
	public void addCountryInfos(int countryId, CountryInfoBatch batch) {
		Country c = countries.findById(countryId).orElseThrow(() -> ApiException.notFound("Country not found."));
		Set<Integer> years = new HashSet<>();
		if (c.getCountryinfos() != null)
			c.getCountryinfos().stream().filter(i -> !i.isDeleted()).forEach(i -> years.add(i.getYear()));
		for (CountryInfoWrite w : batch.items()) {
			int y = Integer.parseInt(w.year());
			if (!years.add(y))
				throw ApiException.conflict("Record already exists: " + y + ".");
		}
		for (CountryInfoWrite w : batch.items()) {
			Countryinfo ci = new Countryinfo();
			ci.setCountry(c);
			ci.setDeleted(false);
			applyInfo(ci, w);
			countryInfos.save(ci);
		}
	}

	/** EditCountryInfoDialog. */
	public void updateCountryInfo(int id, CountryInfoWrite w) {
		Countryinfo ci = countryInfos.findById(id).orElseThrow(() -> ApiException.notFound("Record not found."));
		int y = Integer.parseInt(w.year());
		if (ci.getCountry() != null && ci.getCountry().getCountryinfos() != null)
			for (Countryinfo other : ci.getCountry().getCountryinfos())
				if (other.getCiid() != id && !other.isDeleted() && other.getYear() == y)
					throw ApiException.conflict("Record already exists.");
		applyInfo(ci, w);
		countryInfos.save(ci);
	}

	public void deleteCountryInfo(int id) {
		Countryinfo ci = countryInfos.findById(id).orElseThrow(() -> ApiException.notFound("Record not found."));
		ci.setDeleted(true);
		countryInfos.save(ci);
	}

	private void applyInfo(Countryinfo ci, CountryInfoWrite w) {
		ci.setYear(Integer.parseInt(w.year()));
		ci.setWbincome(nz(w.wbIncome()));
		ci.setGdpperincome(nz(w.gdpPerCapita()));
		ci.setPopdensity(nz(w.popDensity()));
	}

	// ================================================================== protected areas

	/** AddPADialog: staged list; duplicate name check. Fixed: location is saved. */
	public List<ProtectedAreaDto> addProtectedAreas(ProtectedAreaBatch batch) {
		Set<String> names = new HashSet<>();
		for (Protectedarea p : protectedAreas.findAll())
			if (!p.isIsdeleted())
				names.add(lc(p.getName()));
		for (ProtectedAreaWrite w : batch.items())
			if (!names.add(lc(w.name())))
				throw ApiException.conflict("Record already exists: " + w.name() + ".");
		List<ProtectedAreaDto> out = new ArrayList<>();
		for (ProtectedAreaWrite w : batch.items()) {
			Protectedarea p = new Protectedarea();
			p.setIsdeleted(false);
			applyPa(p, w);
			p = protectedAreas.save(p);
			out.add(new ProtectedAreaDto(p.getPaid(), p.getName(), p.getArea(), p.getEstablished(), p.getLocation()));
		}
		return out;
	}

	/** EditPADialog edit_buttonSave (now validated). */
	public void updateProtectedArea(int id, ProtectedAreaWrite w) {
		Protectedarea p = protectedAreas.findById(id).orElseThrow(() -> ApiException.notFound("Protected area not found."));
		applyPa(p, w);
		protectedAreas.save(p);
	}

	public void deleteProtectedArea(int id) {
		Protectedarea p = protectedAreas.findById(id).orElseThrow(() -> ApiException.notFound("Protected area not found."));
		p.setIsdeleted(true);
		protectedAreas.save(p);
	}

	private void applyPa(Protectedarea p, ProtectedAreaWrite w) {
		p.setName(w.name().trim());
		p.setArea(nz(w.area()));
		p.setEstablished(nz(w.established()));
		p.setLocation(nz(w.location()));
	}

	// ================================================================== climate

	/** AddClimateDialog: find-or-create the type by name, then add the staged classes (unique names per type). */
	public void addClimate(ClimateWrite w) {
		Climatetype type = climateTypes.findAll().stream().filter(t -> eq(t.getCltname(), w.typeName())).findFirst().orElse(null);
		if (type == null) {
			type = new Climatetype();
			type.setCltname(w.typeName().trim());
			type.setCltDesc(nz(w.typeDesc()));
			type = climateTypes.save(type);
		} else if (w.typeDesc() != null && !w.typeDesc().isBlank()) {
			type.setCltDesc(w.typeDesc().trim());
			climateTypes.save(type);
		}
		Set<String> names = new HashSet<>();
		if (type.getClimateclasses() != null)
			type.getClimateclasses().forEach(c -> names.add(lc(c.getCcName())));
		if (w.classes() != null)
			for (ClimateClassWrite cw : w.classes()) {
				if (!names.add(lc(cw.name())))
					throw ApiException.conflict("Climate class already exists: " + cw.name() + ".");
				Climateclass cc = new Climateclass();
				cc.setClimatetype(type);
				cc.setCcCode(cw.code().trim());
				cc.setCcName(cw.name().trim());
				cc.setCcDesc(nz(cw.desc()));
				climateClasses.save(cc);
			}
	}

	// ================================================================== ecosystem typology

	/** AddRealmDialog (fixed: Save was bound to onSelect and never persisted). */
	public void addRealms(CodeNameBatch batch) {
		Set<String> taken = new HashSet<>();
		realms.findAll().forEach(r -> { taken.add("c:" + lc(r.getCode())); taken.add("n:" + lc(r.getName())); });
		checkBatch(batch, taken);
		for (CodeNameWrite w : batch.items()) {
			Realm r = new Realm();
			r.setCode(w.code().trim());
			r.setName(w.name().trim());
			r.setDesc(nz(w.desc()));
			realms.save(r);
		}
	}

	/** AddBiomeDialog (parent = realm). */
	public void addBiomes(CodeNameBatch batch) {
		Realm realm = realms.findById(req(batch.parentId(), "Select a realm.")).orElseThrow(() -> ApiException.badRequest("Select a realm."));
		Set<String> taken = new HashSet<>();
		biomes.findAll().forEach(b -> { taken.add("c:" + lc(b.getBcode())); taken.add("n:" + lc(b.getBname())); });
		checkBatch(batch, taken);
		for (CodeNameWrite w : batch.items()) {
			Biome b = new Biome();
			b.setRealm(realm);
			b.setBcode(w.code().trim());
			b.setBname(w.name().trim());
			b.setBdesc(nz(w.desc()));
			biomes.save(b);
		}
	}

	/** AddEfgDialog (parent = biome). */
	public void addEfgs(CodeNameBatch batch) {
		Biome biome = biomes.findById(req(batch.parentId(), "Select a biome.")).orElseThrow(() -> ApiException.badRequest("Select a biome."));
		Set<String> taken = new HashSet<>();
		efgs.findAll().forEach(e -> { taken.add("c:" + lc(e.getCode())); taken.add("n:" + lc(e.getName())); });
		checkBatch(batch, taken);
		for (CodeNameWrite w : batch.items()) {
			Efgrp e = new Efgrp();
			e.setBiome(biome);
			e.setCode(w.code().trim());
			e.setName(w.name().trim());
			e.setDesc(nz(w.desc()));
			efgs.save(e);
		}
	}

	/** AddEcosystemDialog: find-or-create chain by code. */
	public void addEcosystemChain(EcosystemChain w) {
		Realm realm = realms.findAll().stream().filter(r -> eq(r.getCode(), w.realmCode())).findFirst().orElse(null);
		if (realm == null) {
			realm = new Realm();
			realm.setCode(w.realmCode().trim());
			realm.setName(nz(w.realmName()));
			realm.setDesc("");
			realm = realms.save(realm);
		}
		if (blank(w.biomeCode()))
			return;
		Biome biome = biomes.findAll().stream().filter(b -> eq(b.getBcode(), w.biomeCode())).findFirst().orElse(null);
		if (biome == null) {
			biome = new Biome();
			biome.setRealm(realm);
			biome.setBcode(w.biomeCode().trim());
			biome.setBname(nz(w.biomeName()));
			biome.setBdesc("");
			biome = biomes.save(biome);
		}
		if (blank(w.efgCode()))
			return;
		if (efgs.findAll().stream().noneMatch(e -> eq(e.getCode(), w.efgCode()))) {
			Efgrp e = new Efgrp();
			e.setBiome(biome);
			e.setCode(w.efgCode().trim());
			e.setName(nz(w.efgName()));
			e.setDesc("");
			efgs.save(e);
		}
	}

	// ================================================================== services

	/** AddEServiceDialog. */
	public void addEservices(CodeNameBatch batch) {
		Set<String> taken = new HashSet<>();
		eservices.findAll().forEach(s -> { taken.add("c:" + lc(s.getCode())); taken.add("n:" + lc(s.getName())); });
		checkBatch(batch, taken);
		for (CodeNameWrite w : batch.items()) {
			Eservice s = new Eservice();
			s.setCode(w.code().trim());
			s.setName(w.name().trim());
			s.setDesc(nz(w.desc()));
			eservices.save(s);
		}
	}

	/** AddEsubServiceDialog (parent = e-service). */
	public void addEsubservices(CodeNameBatch batch) {
		Eservice parent = eservices.findById(req(batch.parentId(), "Select an e-service."))
				.orElseThrow(() -> ApiException.badRequest("Select an e-service."));
		Set<String> taken = new HashSet<>();
		esubservices.findAll().forEach(s -> { taken.add("c:" + lc(s.getCode())); taken.add("n:" + lc(s.getName())); });
		checkBatch(batch, taken);
		for (CodeNameWrite w : batch.items()) {
			Esubservice s = new Esubservice();
			s.setEservice(parent);
			s.setCode(w.code().trim());
			s.setName(w.name().trim());
			s.setDesc(nz(w.desc()));
			esubservices.save(s);
		}
	}

	/** AddServiceDialog: find-or-create e-service, then e-sub-service, by code. */
	public void addServiceChain(ServiceChain w) {
		Eservice s = eservices.findAll().stream().filter(x -> eq(x.getCode(), w.serviceCode())).findFirst().orElse(null);
		if (s == null) {
			s = new Eservice();
			s.setCode(w.serviceCode().trim());
			s.setName(nz(w.serviceName()));
			s.setDesc(nz(w.desc()));
			s = eservices.save(s);
		}
		if (blank(w.subServiceCode()))
			return;
		if (esubservices.findAll().stream().noneMatch(x -> eq(x.getCode(), w.subServiceCode()))) {
			Esubservice sub = new Esubservice();
			sub.setEservice(s);
			sub.setCode(w.subServiceCode().trim());
			sub.setName(nz(w.subServiceName()));
			sub.setDesc(nz(w.desc()));
			esubservices.save(sub);
		}
	}

	// ================================================================== helpers

	/** Fixed duplicate check: the legacy dialogs compared against the textbox component, never the typed value. */
	private static void checkBatch(CodeNameBatch batch, Set<String> taken) {
		for (CodeNameWrite w : batch.items()) {
			if (!taken.add("c:" + lc(w.code())))
				throw ApiException.conflict("Code already exists: " + w.code() + ".");
			if (!taken.add("n:" + lc(w.name())))
				throw ApiException.conflict("Name already exists: " + w.name() + ".");
		}
	}

	private static int req(Integer id, String message) {
		if (id == null)
			throw ApiException.badRequest(message);
		return id;
	}

	private static boolean eq(String a, String b) {
		return a != null && b != null && a.trim().equalsIgnoreCase(b.trim());
	}

	private static boolean blank(String s) {
		return s == null || s.isBlank();
	}

	private static String lc(String s) {
		return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
	}

	private static String nz(String s) {
		return s == null ? "" : s.trim();
	}
}

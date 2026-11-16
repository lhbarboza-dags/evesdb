package org.sibol.eves.api.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.sibol.eves.api.common.ApiException;
import org.sibol.eves.api.search.SearchDtos.*;
import org.sibol.eves.api.study.StudyDtos.StudyDto;
import org.sibol.eves.api.study.StudyMapper;
import org.sibol.eves.api.study.StudyStatus;
import org.sibol.eves.data.CountryRepo;
import org.sibol.eves.data.CvtermRepo;
import org.sibol.eves.data.EserviceRepo;
import org.sibol.eves.data.RealmRepo;
import org.sibol.eves.data.StudyRepo;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import model.*;

/**
 * Port of SearchController (search(), getFilterByKeyword, getFilterByKeywordAndMatchAll, filterCode),
 * StudySrcRenderer (row popups) and ShowSummaryController.
 */
@org.springframework.stereotype.Service
@Transactional(readOnly = true)
public class SearchService {

	private final StudyRepo studies;
	private final RealmRepo realms;
	private final EserviceRepo eservices;
	private final CvtermRepo terms;
	private final CountryRepo countries;

	public SearchService(StudyRepo studies, RealmRepo realms, EserviceRepo eservices, CvtermRepo terms, CountryRepo countries) {
		this.studies = studies;
		this.realms = realms;
		this.eservices = eservices;
		this.terms = terms;
		this.countries = countries;
	}

	// ------------------------------------------------------------------ filter options

	public Filters filters() {
		List<EcoOption> eco = new ArrayList<>();
		for (Realm r : realms.findAll(Sort.by("code"))) {
			eco.add(new EcoOption(EcoLevel.REALM, r.getCode(), r.getName(), r.getCode() + " : " + r.getName() + " : (Realm)"));
			if (r.getBiomes() != null)
				for (Biome b : r.getBiomes()) {
					eco.add(new EcoOption(EcoLevel.BIOME, b.getBcode(), b.getBname(), b.getBcode() + " : " + b.getBname() + " : (Biome)"));
					if (b.getEfgrps() != null)
						for (Efgrp e : b.getEfgrps())
							eco.add(new EcoOption(EcoLevel.EFG, e.getCode(), e.getName(), e.getCode() + " : " + e.getName() + " : (EFG)"));
				}
		}
		List<ServiceOption> serv = new ArrayList<>();
		for (Eservice s : eservices.findAll(Sort.by("code"))) {
			serv.add(new ServiceOption(ServiceLevel.SERVICE, s.getCode(), s.getName(), s.getCode() + " : " + s.getName() + " : (Service)"));
			if (s.getEsubservices() != null)
				for (Esubservice sub : s.getEsubservices())
					if (sub.getCode() != null && !sub.getCode().isEmpty() && !sub.getCode().equals("-"))
						serv.add(new ServiceOption(ServiceLevel.SUB_SERVICE, sub.getCode(), sub.getName(),
								sub.getCode() + " : " + sub.getName() + " : (Sub Service)"));
		}
		List<MethodOption> meth = terms.findByCvNames(List.of("Valuation Method"), false).stream()
				.map(t -> new MethodOption(t.getCode(), t.getName(), t.getCode() + " : " + t.getName())).toList();
		List<String> cn = countries.findAll(Sort.by("cname")).stream().filter(c -> !c.isIsdeleted()).map(Country::getCname).toList();
		return new Filters(eco, serv, meth, cn);
	}

	// ------------------------------------------------------------------ search

	public SearchResult search(SearchRequest req) {
		String keyword = req.keyword() == null ? "" : req.keyword().trim();
		List<String> cs = nn(req.countries());
		List<EcoFilter> es = nn(req.ecosystems());
		List<ServiceFilter> ss = nn(req.services());
		List<String> ms = nn(req.methods());
		boolean emptyFilter = cs.isEmpty() && es.isEmpty() && ss.isEmpty() && ms.isEmpty();

		List<Studysrc> found = new ArrayList<>();
		for (Studysrc src : studies.findSearchable()) {
			if (keyword.isEmpty() && emptyFilter)
				found.add(src);
			else if (req.matchAll() ? matchAll(src, keyword, cs, es, ss, ms) : matchAny(src, keyword, cs, es, ss, ms))
				found.add(src);
		}
		found.sort((a, b) -> nz(a.getTitle()).compareToIgnoreCase(nz(b.getTitle())));

		List<StudyRow> rows = new ArrayList<>();
		int valueCount = 0;
		for (Studysrc s : found) {
			StudyRow row = row(s);
			valueCount += row.valueCount();
			rows.add(row);
		}
		return new SearchResult(found.size(), valueCount, filterCounts(found, es, ss), rows);
	}

	/**
	 * rad_all: keyword (title/author contains, year equals) OR any selected country/ecosystem/service/method.
	 * Fixed vs. legacy: every selected ecosystem is checked (ZK only read the first one) and the realm branch no
	 * longer returns before matching.
	 */
	private boolean matchAny(Studysrc src, String keyword, List<String> cs, List<EcoFilter> es, List<ServiceFilter> ss, List<String> ms) {
		if (!keyword.isEmpty() && keywordMatches(src, keyword))
			return true;
		Facets f = facets(src);
		for (String c : cs)
			if (f.countries.contains(lc(c)))
				return true;
		for (EcoFilter e : es)
			if (f.eco(e.level()).contains(lc(e.code())))
				return true;
		for (ServiceFilter s : ss)
			if (f.serv(s.level()).contains(lc(s.code())))
				return true;
		for (String m : ms)
			if (f.methods.contains(lc(m)))
				return true;
		return false;
	}

	/** rad_exact: keyword AND every selected country AND every ecosystem AND every service AND every method. */
	private boolean matchAll(Studysrc src, String keyword, List<String> cs, List<EcoFilter> es, List<ServiceFilter> ss, List<String> ms) {
		if (!keyword.isEmpty() && !keywordMatches(src, keyword))
			return false;
		Facets f = facets(src);
		for (String c : cs)
			if (!f.countries.contains(lc(c)))
				return false;
		for (EcoFilter e : es)
			if (!f.eco(e.level()).contains(lc(e.code())))
				return false;
		for (ServiceFilter s : ss)
			if (!f.serv(s.level()).contains(lc(s.code())))
				return false;
		for (String m : ms)
			if (!f.methods.contains(lc(m)))
				return false;
		return true;
	}

	private static boolean keywordMatches(Studysrc src, String keyword) {
		String k = lc(keyword);
		return lc(src.getTitle()).contains(k) || lc(src.getAuthor()).contains(k) || String.valueOf(src.getPubyear()).equals(keyword);
	}

	/** Lower-cased codes reachable from a study's live sites and datasets. */
	private static final class Facets {
		final Set<String> countries = new HashSet<>(), realms = new HashSet<>(), biomes = new HashSet<>(), efgs = new HashSet<>(),
				eservices = new HashSet<>(), esubservices = new HashSet<>(), methods = new HashSet<>();

		Set<String> eco(EcoLevel l) {
			return switch (l) { case REALM -> realms; case BIOME -> biomes; case EFG -> efgs; };
		}

		Set<String> serv(ServiceLevel l) {
			return l == ServiceLevel.SERVICE ? eservices : esubservices;
		}
	}

	private static Facets facets(Studysrc src) {
		Facets f = new Facets();
		for (Site site : StudyMapper.liveSites(src)) {
			Location l = site.getLocation();
			if (l != null && l.getCountryinfo() != null && !l.getCountryinfo().isDeleted() && l.getCountryinfo().getCountry() != null
					&& !l.getCountryinfo().getCountry().isIsdeleted())
				f.countries.add(lc(l.getCountryinfo().getCountry().getCname()));
			for (Dataset ds : StudyMapper.liveDatasets(site, src)) {
				Ecosystem e = ds.getEcosystem();
				if (e != null) {
					if (e.getRealm() != null) f.realms.add(lc(e.getRealm().getCode()));
					if (e.getBiome() != null) f.biomes.add(lc(e.getBiome().getBcode()));
					if (e.getEfgrp() != null) f.efgs.add(lc(e.getEfgrp().getCode()));
				}
				Service s = ds.getService();
				if (s != null) {
					if (s.getEservice() != null) f.eservices.add(lc(s.getEservice().getCode()));
					if (s.getEsubservice() != null) f.esubservices.add(lc(s.getEsubservice().getCode()));
				}
				Valuationmethod vm = ds.getValuationmethod();
				if (vm != null && vm.getMethod() != null && !vm.getMethod().isIsdeleted())
					f.methods.add(lc(vm.getMethod().getCode()));
			}
		}
		return f;
	}

	/** SearchController.filterCode → EcoCount label: values per selected ecosystem/service code. */
	private List<Count> filterCounts(List<Studysrc> found, List<EcoFilter> es, List<ServiceFilter> ss) {
		Map<String, Integer> counts = new LinkedHashMap<>();
		Map<String, String> names = new LinkedHashMap<>();
		for (EcoFilter e : es) counts.put("E:" + e.level() + ":" + lc(e.code()), 0);
		for (ServiceFilter s : ss) counts.put("S:" + s.level() + ":" + lc(s.code()), 0);
		if (counts.isEmpty())
			return List.of();
		for (Studysrc src : found)
			for (Site site : StudyMapper.liveSites(src))
				for (Dataset ds : StudyMapper.liveDatasets(site, src)) {
					Ecosystem e = ds.getEcosystem();
					if (e != null) {
						if (e.getRealm() != null) bump(counts, names, "E:REALM:" + lc(e.getRealm().getCode()), e.getRealm().getName());
						if (e.getBiome() != null) bump(counts, names, "E:BIOME:" + lc(e.getBiome().getBcode()), e.getBiome().getBname());
						if (e.getEfgrp() != null) bump(counts, names, "E:EFG:" + lc(e.getEfgrp().getCode()), e.getEfgrp().getName());
					}
					Service s = ds.getService();
					if (s != null) {
						if (s.getEservice() != null) bump(counts, names, "S:SERVICE:" + lc(s.getEservice().getCode()), s.getEservice().getName());
						if (s.getEsubservice() != null) bump(counts, names, "S:SUB_SERVICE:" + lc(s.getEsubservice().getCode()), s.getEsubservice().getName());
					}
				}
		List<Count> out = new ArrayList<>();
		counts.forEach((k, v) -> out.add(new Count(names.getOrDefault(k, k.substring(k.lastIndexOf(':') + 1).toUpperCase()), v)));
		return out;
	}

	private static void bump(Map<String, Integer> counts, Map<String, String> names, String key, String name) {
		if (counts.containsKey(key)) {
			counts.put(key, counts.get(key) + 1);
			names.putIfAbsent(key, name);
		}
	}

	/** StudySrcRenderer.render: byEcosystem / byService / byMethod / byValues popups. */
	private StudyRow row(Studysrc s) {
		Map<String, Integer> byEco = new TreeMap<>(), byServ = new TreeMap<>(), byMeth = new TreeMap<>(), byVal = new TreeMap<>();
		int total = 0;
		for (Site site : StudyMapper.liveSites(s))
			for (Dataset ds : StudyMapper.liveDatasets(site, s)) {
				String eco = "", serv = "";
				Ecosystem e = ds.getEcosystem();
				if (e != null) {
					eco = join(e.getRealm() == null ? null : "Realm: " + e.getRealm().getName(),
							e.getBiome() == null ? null : "Biome: " + e.getBiome().getBname(),
							e.getEfgrp() == null ? null : "EFG: " + e.getEfgrp().getName());
					byEco.merge(eco, 1, Integer::sum);
					total++;
				}
				Service sv = ds.getService();
				if (sv != null) {
					serv = join(sv.getEservice() == null ? null : "Service: " + sv.getEservice().getName(),
							sv.getEsubservice() == null ? null : "Sub service: " + sv.getEsubservice().getName());
					byServ.merge(serv, 1, Integer::sum);
				}
				if (ds.getValuationmethod() != null && ds.getValuationmethod().getMethod() != null)
					byMeth.merge(ds.getValuationmethod().getMethod().getName(), 1, Integer::sum);
				if (ds.getValue() != null)
					byVal.merge(join(eco.isEmpty() ? null : eco, serv.isEmpty() ? null : serv), 1, Integer::sum);
			}
		return new StudyRow(s.getSrcid(), s.getTitle(), s.getAuthor(), s.getPubyear() == 0 ? null : s.getPubyear(),
				s.getDoctype() == null ? null : s.getDoctype().getName(), total, counts(byEco), counts(byServ),
				counts(byMeth), counts(byVal));
	}

	// ------------------------------------------------------------------ summary (ShowSummary.zul)

	public Summary summary(List<Integer> ids) {
		Map<String, Integer> eco = new TreeMap<>(), ecoVal = new TreeMap<>(), serv = new TreeMap<>(), servVal = new TreeMap<>(),
				country = new TreeMap<>();
		for (Studysrc src : load(ids)) {
			Set<String> ecoSeen = new HashSet<>(), servSeen = new HashSet<>(), countrySeen = new HashSet<>();
			for (Site site : StudyMapper.liveSites(src)) {
				Location l = site.getLocation();
				if (l != null && l.getCountryinfo() != null && l.getCountryinfo().getCountry() != null && !l.getCountryinfo().isDeleted()
						&& !l.getCountryinfo().getCountry().isIsdeleted()) {
					Country c = l.getCountryinfo().getCountry();
					countrySeen.add(c.getCcode() + ":" + c.getCname());
				}
				for (Dataset ds : StudyMapper.liveDatasets(site, src)) {
					Ecosystem e = ds.getEcosystem();
					if (e != null) {
						if (e.getRealm() != null) countBoth(e.getRealm().getCode() + ":" + e.getRealm().getName(), ecoSeen, ecoVal);
						if (e.getBiome() != null) countBoth(e.getBiome().getBcode() + ":" + e.getBiome().getBname(), ecoSeen, ecoVal);
						if (e.getEfgrp() != null) countBoth(e.getEfgrp().getCode() + ":" + e.getEfgrp().getName(), ecoSeen, ecoVal);
					}
					Service s = ds.getService();
					if (s != null) {
						if (s.getEservice() != null) countBoth(s.getEservice().getCode() + ":" + s.getEservice().getName(), servSeen, servVal);
						if (s.getEsubservice() != null) countBoth(s.getEsubservice().getCode() + ":" + s.getEsubservice().getName(), servSeen, servVal);
					}
				}
			}
			ecoSeen.forEach(k -> eco.merge(k, 1, Integer::sum));
			servSeen.forEach(k -> serv.merge(k, 1, Integer::sum));
			countrySeen.forEach(k -> country.merge(k, 1, Integer::sum));
		}
		return new Summary(counts(country), counts(eco), counts(serv), counts(ecoVal), counts(servVal));
	}

	private static void countBoth(String key, Set<String> perStudy, Map<String, Integer> values) {
		perStudy.add(key);
		values.merge(key, 1, Integer::sum);
	}

	// ------------------------------------------------------------------ map + detail

	public List<MapSite> mapSites(List<Integer> ids) {
		List<MapSite> out = new ArrayList<>();
		for (Studysrc src : load(ids))
			for (Site site : StudyMapper.liveSites(src)) {
				List<String> vals = new ArrayList<>();
				for (Dataset ds : StudyMapper.liveDatasets(site, src)) {
					Value v = ds.getValue();
					if (v == null)
						continue;
					String serv = ds.getService() != null && ds.getService().getEservice() != null ? ds.getService().getEservice().getName() : "";
					String amount = v.getValuept() != null && !v.getValuept().isBlank() ? v.getValuept()
							: v.getVaggregate() != null ? v.getVaggregate().toPlainString() : "";
					String cur = v.getValue_currency() == null ? "" : v.getValue_currency().getName();
					String unit = v.getUnitid() == null ? "" : v.getUnitid().getName();
					vals.add((serv + ": " + amount + " " + cur + " " + unit).trim());
				}
				Location l = site.getLocation();
				String country = l != null && l.getCountryinfo() != null && l.getCountryinfo().getCountry() != null
						? l.getCountryinfo().getCountry().getCname() : "";
				out.add(new MapSite(src.getSrcid(), src.getTitle(), site.getSiteid(), site.getName(), country,
						parse(site.getLatitude()), parse(site.getLongitude()), site.getLatitude(), site.getLongitude(), vals));
			}
		return out;
	}

	/** Study detail tab (StudySrcRenderer row click / StudySrcDetailRenderer). Only searchable studies. */
	public StudyDto detail(int id) {
		Studysrc s = studies.findById(id).orElseThrow(() -> ApiException.notFound("Study not found."));
		int st = StudyStatus.of(s.getStatus());
		if (s.isIsdeleted() || (st != StudyStatus.PENDING && st != StudyStatus.APPROVED))
			throw ApiException.notFound("Study not found.");
		return StudyMapper.toDto(s);
	}

	public List<Studysrc> load(List<Integer> ids) {
		if (ids == null || ids.isEmpty())
			return List.of();
		Set<Integer> idSet = new HashSet<>(ids);
		return studies.findSearchable().stream().filter(s -> idSet.contains(s.getSrcid())).toList();
	}

	// ------------------------------------------------------------------ helpers

	private static List<Count> counts(Map<String, Integer> m) {
		List<Count> out = new ArrayList<>();
		m.forEach((k, v) -> out.add(new Count(k, v)));
		return out;
	}

	private static String join(String... parts) {
		StringBuilder sb = new StringBuilder();
		for (String p : parts)
			if (p != null && !p.isBlank())
				sb.append(sb.length() == 0 ? "" : " · ").append(p.trim());
		return sb.toString();
	}

	private static Double parse(String s) {
		try {
			return s == null || s.isBlank() ? null : Double.valueOf(s.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static <T> List<T> nn(List<T> l) {
		return l == null ? List.of() : l;
	}

	private static String lc(String s) {
		return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
	}

	private static String nz(String s) {
		return s == null ? "" : s;
	}
}

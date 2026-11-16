package org.sibol.eves.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.sibol.eves.api.common.ApiException;
import org.sibol.eves.api.common.Ref;
import org.sibol.eves.api.dictionary.DictionaryService;
import org.sibol.eves.api.search.ExportService;
import org.sibol.eves.api.search.SearchDtos.*;
import org.sibol.eves.api.search.SearchService;
import org.sibol.eves.api.study.StudyDtos.*;
import org.sibol.eves.api.study.StudyService;
import org.sibol.eves.data.UserRepo;
import org.sibol.eves.security.AppUser;
import org.sibol.eves.security.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import model.User;

/**
 * Runs against the real database (schema validated with ddl-auto=validate on startup).
 * Every test is @Transactional and rolled back, so nothing is written.
 * Enabled only when SPRING_DATASOURCE_URL is set, e.g. via backend/.env.test.
 */
@SpringBootTest
@Transactional
@EnabledIfEnvironmentVariable(named = "SPRING_DATASOURCE_URL", matches = ".+")
class DatabaseIT {

	@Autowired JdbcTemplate jdbc;
	@Autowired SearchService search;
	@Autowired StudyService studies;
	@Autowired DictionaryService dictionary;
	@Autowired ExportService export;
	@Autowired UserRepo users;

	@BeforeEach
	void loginAsAdmin() {
		User admin = users.findByRole(Role.ADMIN.id).get(0);
		AppUser p = new AppUser(admin.getUserid(), admin.getEmailadd(), admin.getFirstname(), admin.getLastname(), Role.ADMIN);
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(p, null, p.getAuthorities()));
	}

	@AfterEach
	void logout() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void emptySearchReturnsAllPendingAndApprovedStudies() {
		Integer expected = jdbc.queryForObject(
				"select count(*) from studysrc where isdeleted = false and status in (0,1)", Integer.class);
		SearchResult r = search.search(new SearchRequest("", false, null, null, null, null));
		assertThat(r.studyCount()).isEqualTo(expected);
		assertThat(r.studies()).hasSize(expected);
	}

	@Test
	void keywordAndFiltersNarrowResults() {
		SearchResult all = search.search(new SearchRequest("", false, null, null, null, null));
		StudyRow first = all.studies().get(0);
		String word = first.title().split("\\s+")[0];
		SearchResult byTitle = search.search(new SearchRequest(word, true, null, null, null, null));
		assertThat(byTitle.studies()).extracting(StudyRow::id).contains(first.id());
		assertThat(byTitle.studyCount()).isLessThanOrEqualTo(all.studyCount());

		Filters f = search.filters();
		assertThat(f.ecosystems()).isNotEmpty();
		assertThat(f.countries()).isNotEmpty();
		EcoOption realm = f.ecosystems().stream().filter(e -> e.level() == EcoLevel.REALM).findFirst().orElseThrow();
		SearchResult any = search.search(new SearchRequest("", false, null, List.of(new EcoFilter(EcoLevel.REALM, realm.code())), null, null));
		SearchResult exact = search.search(new SearchRequest("", true, null, List.of(new EcoFilter(EcoLevel.REALM, realm.code())), null, null));
		assertThat(any.studyCount()).isEqualTo(exact.studyCount()); // one filter: any == all
		assertThat(any.filterCounts()).hasSize(1);
	}

	@Test
	void summaryMapAndExport() throws Exception {
		SearchResult all = search.search(new SearchRequest("", false, null, null, null, null));
		List<Integer> ids = all.studies().stream().map(StudyRow::id).limit(5).toList();
		Summary s = search.summary(ids);
		assertThat(s.countries()).isNotEmpty();
		assertThat(search.mapSites(ids)).isNotEmpty();

		byte[] xlsx = export.export(ids);
		Integer expectedRows = jdbc.queryForObject(
				"select count(*) from \"v_fullSummary\" where studyid in (" + ids.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElseThrow() + ")",
				Integer.class);
		try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(xlsx))) {
			assertThat(wb.getSheet("STUDY DATA").getLastRowNum()).isEqualTo(expectedRows);
			assertThat(wb.getSheet("STUDY DATA").getRow(0).getLastCellNum()).isEqualTo((short) 58);
		}
	}

	@Test
	void createEditApproveDeleteStudyGraph() {
		var lk = dictionary.lookups();
		var country = lk.countries().stream().filter(c -> !c.infos().isEmpty()).findFirst().orElseThrow();
		var realm = lk.realms().stream().filter(r -> !r.biomes().isEmpty()).findFirst().orElseThrow();
		var biome = realm.biomes().get(0);
		var es = lk.eservices().stream().filter(x -> !x.subservices().isEmpty()).findFirst().orElseThrow();

		ValueDto value = new ValueDto(null, lk.valueTypes().get(0), "12.5", new BigDecimal("100"), null, null,
				lk.currencies().get(0), lk.units().get(0), "2020", "", "", null, null, null);
		MethodDto method = new MethodDto(null, lk.valuationMethods().get(0), "change", "", lk.economicMeasures().get(0), "",
				List.of(new AvailInfoDto(null, false, lk.availStudyInfo().get(0))));
		DatasetDto ds = new DatasetDto(null, false,
				new EcosystemDto(null, new Ref(realm.id(), null, null), "", new Ref(biome.id(), null, null), "", null, ""),
				new ServiceDto(null, new Ref(es.id(), null, null), "", new Ref(es.subservices().get(0).id(), null, null), ""),
				method, value);
		SiteDto site = new SiteDto(null, false, "IT site", "14.5", "121.0", "10", "ha", lk.scales().get(0),
				new LocationDto(null, "Somewhere", new CountryInfoDto(country.infos().get(0).id(), null, null, null, null, null, null, null), null, null),
				List.of(new ClimateDto(null, false, new Ref(lk.climateTypes().get(0).id(), null, null), null, null)),
				List.of(new SiteAttrDto(null, false, lk.siteAttributes().get(0), "")), List.of(ds));
		StudyDto in = new StudyDto(null, "Integration test study", "abs", "Tester", lk.docTypes().get(0), "2021", "ref", "",
				null, null, null, null, null, null, List.of(site));

		StudyDto created = studies.create(in);
		assertThat(created.id()).isNotNull();
		assertThat(created.status()).isZero();
		assertThat(created.sites()).hasSize(1);
		SiteDto savedSite = created.sites().get(0);
		assertThat(savedSite.climates()).hasSize(1);
		assertThat(savedSite.attributes()).hasSize(1);
		assertThat(savedSite.datasets()).hasSize(1);
		assertThat(savedSite.datasets().get(0).method().availStudyInfo()).hasSize(1);
		assertThat(savedSite.location().countryInfo().countryName()).isEqualTo(country.name());

		// edit: rename, drop the attribute, add a second valuation
		DatasetDto second = new DatasetDto(null, false, savedSite.datasets().get(0).ecosystem() == null ? null
				: new EcosystemDto(null, new Ref(realm.id(), null, null), "", null, "", null, ""), null, null,
				new ValueDto(null, null, "1", null, null, null, null, null, "", "", "", null, null, null));
		SiteDto editedSite = new SiteDto(savedSite.id(), false, "IT site renamed", savedSite.latitude(), savedSite.longitude(),
				savedSite.area(), savedSite.areaUnit(), savedSite.scale(), savedSite.location(), savedSite.climates(), List.of(),
				List.of(savedSite.datasets().get(0), second));
		StudyDto edited = studies.update(created.id(), new StudyDto(created.id(), "Integration test study 2", created.abstractText(),
				created.author(), created.docType(), created.pubYear(), created.reference(), created.externalLink(), 0, null, null,
				null, null, null, List.of(editedSite)));
		assertThat(edited.title()).isEqualTo("Integration test study 2");
		assertThat(edited.sites().get(0).name()).isEqualTo("IT site renamed");
		assertThat(edited.sites().get(0).attributes()).isEmpty();
		assertThat(edited.sites().get(0).datasets()).hasSize(2);

		assertThat(studies.list(0, "integration test study 2")).extracting(StudyListRow::id).contains(created.id());

		studies.setStatus(new StatusRequest(List.of(created.id()), 1));
		assertThatThrownBy(() -> studies.update(created.id(), edited)).isInstanceOf(ApiException.class)
				.hasMessageContaining("already approved");
		studies.delete(new org.sibol.eves.api.study.StudyDtos.IdsRequest(List.of(created.id())));
		assertThat(studies.list(null, null)).extracting(StudyListRow::id).doesNotContain(created.id());
	}

	@Test
	void studyRulesFromTheWizard() {
		StudyDto noSite = new StudyDto(null, "t", "", "a", null, "", "", "", null, null, null, null, null, null, List.of());
		assertThatThrownBy(() -> studies.create(noSite)).hasMessage("Please add site first.");
	}

	@Test
	void dictionaryTermLifecycleAndPaCategory() {
		var created = dictionary.createTerm(new org.sibol.eves.api.dictionary.DictionaryDtos.TermWrite("Valuation Method", "ZZ", "IT method", "d"));
		assertThat(created.cv()).isEqualTo("Valuation Method");
		assertThat(dictionary.terms("Valuation Method")).extracting(t -> t.id()).contains(created.id());
		dictionary.deleteTerm(created.id());
		assertThat(dictionary.terms("Valuation Method")).extracting(t -> t.id()).doesNotContain(created.id());
		assertThat(dictionary.terms("PA Category")).isNotEmpty();
	}
}

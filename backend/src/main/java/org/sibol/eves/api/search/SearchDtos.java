package org.sibol.eves.api.search;

import java.util.List;

public final class SearchDtos {
	private SearchDtos() {}

	/** Ecosystem level as encoded in the legacy chosenbox labels "code : name : (Realm|Biome|EFG)". */
	public enum EcoLevel { REALM, BIOME, EFG }

	/** Service level as encoded in "code : name: (Service|Sub Service)". */
	public enum ServiceLevel { SERVICE, SUB_SERVICE }

	public record EcoOption(EcoLevel level, String code, String name, String label) {}

	public record ServiceOption(ServiceLevel level, String code, String name, String label) {}

	public record MethodOption(String code, String name, String label) {}

	/** Models of chosen_Ecosytem, chosen_Service, chosen_Method and chosen_country (SearchController.doAfterCompose). */
	public record Filters(List<EcoOption> ecosystems, List<ServiceOption> services, List<MethodOption> methods,
			List<String> countries) {}

	public record EcoFilter(EcoLevel level, String code) {}

	public record ServiceFilter(ServiceLevel level, String code) {}

	/** keywordBox + chosenboxes + radiogroup searchOptions (rad_all = matchAll false, rad_exact = matchAll true). */
	public record SearchRequest(String keyword, boolean matchAll, List<String> countries, List<EcoFilter> ecosystems,
			List<ServiceFilter> services, List<String> methods) {}

	public record Count(String label, int count) {}

	/** One result row (StudySrcRenderer): title, author, and the 4 summary popups. */
	public record StudyRow(int id, String title, String author, Integer pubYear, String docType, int valueCount,
			List<Count> byEcosystem, List<Count> byService, List<Count> byMethod, List<Count> byEcosystemService) {}

	/** results (study count), valueNum (values), EcoCount (counts for the selected ecosystem/service codes). */
	public record SearchResult(int studyCount, int valueCount, List<Count> filterCounts, List<StudyRow> studies) {}

	/** ShowSummary.zul grids. */
	public record Summary(List<Count> countries, List<Count> ecosystems, List<Count> services, List<Count> ecosystemValues,
			List<Count> serviceValues) {}

	/** Map tab (SearchController.createPanel + GmapSiteItemRenderer + addMarker). */
	public record MapSite(int studyId, String title, int siteId, String siteName, String country, Double lat, Double lng,
			String latitude, String longitude, List<String> values) {}

	public record IdsRequest(List<Integer> ids) {}

	public record ExportRequest(List<Integer> ids, String fileName, boolean agreed) {}
}

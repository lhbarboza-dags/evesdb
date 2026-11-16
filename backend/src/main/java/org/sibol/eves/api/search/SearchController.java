package org.sibol.eves.api.search;

import java.io.IOException;
import java.util.List;

import org.sibol.eves.api.common.ApiException;
import org.sibol.eves.api.search.SearchDtos.*;
import org.sibol.eves.api.study.StudyDtos.StudyDto;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** search.zul (SearchInit: login required). All /api/** endpoints require an authenticated session. */
@RestController
@RequestMapping("/api/search")
public class SearchController {

	private final SearchService search;
	private final ExportService export;

	public SearchController(SearchService search, ExportService export) {
		this.search = search;
		this.export = export;
	}

	@GetMapping("/filters")
	public Filters filters() {
		return search.filters();
	}

	/** search2_btn (and the initial search() on page load with an empty request). */
	@PostMapping
	public SearchResult search(@RequestBody SearchRequest req) {
		return search.search(req);
	}

	/** buttonSummary → ShowSummary.zul. */
	@PostMapping("/summary")
	public Summary summary(@RequestBody IdsRequest req) {
		return search.summary(req.ids());
	}

	/** btn_gmap → map tab. */
	@PostMapping("/map")
	public List<MapSite> map(@RequestBody IdsRequest req) {
		return search.mapSites(req.ids());
	}

	/** Result row click → study detail tab. */
	@GetMapping("/studies/{id}")
	public StudyDto detail(@PathVariable int id) {
		return search.detail(id);
	}

	/** btn_export → AgreementDownloadDialog (agreed) → inputDownloadDialog (fileName) → XLSX download. */
	@PostMapping("/export")
	public ResponseEntity<byte[]> export(@RequestBody ExportRequest req) throws IOException {
		if (!req.agreed())
			throw ApiException.badRequest("You must accept the data use agreement before downloading.");
		List<Integer> ids = search.load(req.ids()).stream().map(model.Studysrc::getSrcid).toList();
		byte[] body = export.export(ids);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION,
						ContentDisposition.attachment().filename(ExportService.fileName(req.fileName())).build().toString())
				.contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(body);
	}
}

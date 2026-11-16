package org.sibol.eves.api.study;

import java.util.List;

import javax.validation.Valid;

import org.sibol.eves.api.study.StudyDtos.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Data management (upload.zul / study_.zul guarded by DataMgtInit: Curator+). Status changes and delete are Manager+
 * (ViewStudyRenderer showed hbox_approvalBtns only for role >= MANAGER).
 */
@RestController
@RequestMapping("/api/studies")
@PreAuthorize("hasAuthority('CURATOR')")
public class StudyController {

	private final StudyService service;

	public StudyController(StudyService service) {
		this.service = service;
	}

	@GetMapping
	public List<StudyListRow> list(@RequestParam(required = false) Integer status, @RequestParam(required = false) String q) {
		return service.list(status, q);
	}

	@GetMapping("/{id}")
	public StudyDto get(@PathVariable int id) {
		return service.get(id);
	}

	@PostMapping
	public StudyDto create(@Valid @RequestBody StudyDto dto) {
		return service.create(dto);
	}

	@PutMapping("/{id}")
	public StudyDto update(@PathVariable int id, @Valid @RequestBody StudyDto dto) {
		return service.update(id, dto);
	}

	@PostMapping("/status")
	@PreAuthorize("hasAuthority('MANAGER')")
	public void status(@RequestBody StatusRequest req) {
		service.setStatus(req);
	}

	@PostMapping("/delete")
	@PreAuthorize("hasAuthority('MANAGER')")
	public void delete(@RequestBody IdsRequest req) {
		service.delete(req);
	}
}

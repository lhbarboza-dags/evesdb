package org.sibol.eves.api.dictionary;

import java.util.List;

import javax.validation.Valid;

import org.sibol.eves.api.dictionary.DictionaryDtos.*;
import org.sibol.eves.api.study.StudyDtos.ProtectedAreaDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * dictionary.zul / _dictionary.zul and its dialogs. The ZK menu showed Dictionary to Manager+ while DataMgtInit let
 * Curators in by URL; the API now enforces Manager+ for writes. Reads are open to Curators because the study forms
 * (AddSiteDialog → AddPADialog) and the lookups need them.
 */
@RestController
@RequestMapping("/api")
public class DictionaryController {

	private final DictionaryService service;

	public DictionaryController(DictionaryService service) {
		this.service = service;
	}

	// ---- reads
	@GetMapping("/lookups")
	public Lookups lookups() {
		return service.lookups();
	}

	@GetMapping("/dictionary/terms")
	@PreAuthorize("hasAuthority('CURATOR')")
	public List<TermRow> terms(@RequestParam String cv) {
		return service.terms(cv);
	}

	@GetMapping("/dictionary/countries")
	@PreAuthorize("hasAuthority('CURATOR')")
	public List<CountryRow> countries() {
		return service.countryList();
	}

	@GetMapping("/dictionary/protected-areas")
	@PreAuthorize("hasAuthority('CURATOR')")
	public List<ProtectedAreaDto> protectedAreas() {
		return service.paList();
	}

	@GetMapping("/dictionary/climate-types")
	@PreAuthorize("hasAuthority('CURATOR')")
	public List<ClimateTypeRow> climateTypes() {
		return service.climateTree();
	}

	@GetMapping("/dictionary/realms")
	@PreAuthorize("hasAuthority('CURATOR')")
	public List<RealmRow> realms() {
		return service.realmTree();
	}

	@GetMapping("/dictionary/eservices")
	@PreAuthorize("hasAuthority('CURATOR')")
	public List<EServiceRow> eservices() {
		return service.serviceTree();
	}

	// ---- CV terms
	@PostMapping("/dictionary/terms")
	@PreAuthorize("hasAuthority('MANAGER')")
	public TermRow createTerm(@Valid @RequestBody TermWrite w) {
		return service.createTerm(w);
	}

	@PutMapping("/dictionary/terms/{id}")
	@PreAuthorize("hasAuthority('MANAGER')")
	public TermRow updateTerm(@PathVariable int id, @Valid @RequestBody TermWrite w) {
		return service.updateTerm(id, w);
	}

	@DeleteMapping("/dictionary/terms/{id}")
	@PreAuthorize("hasAuthority('MANAGER')")
	public void deleteTerm(@PathVariable int id) {
		service.deleteTerm(id);
	}

	// ---- countries
	@PostMapping("/dictionary/countries")
	@PreAuthorize("hasAuthority('MANAGER')")
	public CountryRow createCountry(@Valid @RequestBody CountryWrite w) {
		return service.createCountry(w);
	}

	@PutMapping("/dictionary/countries/{id}")
	@PreAuthorize("hasAuthority('MANAGER')")
	public CountryRow updateCountry(@PathVariable int id, @Valid @RequestBody CountryWrite w) {
		return service.updateCountry(id, w);
	}

	@DeleteMapping("/dictionary/countries/{id}")
	@PreAuthorize("hasAuthority('MANAGER')")
	public void deleteCountry(@PathVariable int id) {
		service.deleteCountry(id);
	}

	@PostMapping("/dictionary/countries/{id}/infos")
	@PreAuthorize("hasAuthority('MANAGER')")
	public void addCountryInfos(@PathVariable int id, @Valid @RequestBody CountryInfoBatch batch) {
		service.addCountryInfos(id, batch);
	}

	@PutMapping("/dictionary/country-infos/{id}")
	@PreAuthorize("hasAuthority('MANAGER')")
	public void updateCountryInfo(@PathVariable int id, @Valid @RequestBody CountryInfoWrite w) {
		service.updateCountryInfo(id, w);
	}

	@DeleteMapping("/dictionary/country-infos/{id}")
	@PreAuthorize("hasAuthority('MANAGER')")
	public void deleteCountryInfo(@PathVariable int id) {
		service.deleteCountryInfo(id);
	}

	// ---- protected areas (AddSiteDialog addBtn_Pa also opens AddPADialog, so Curators may add)
	@PostMapping("/dictionary/protected-areas")
	@PreAuthorize("hasAuthority('CURATOR')")
	public List<ProtectedAreaDto> addProtectedAreas(@Valid @RequestBody ProtectedAreaBatch batch) {
		return service.addProtectedAreas(batch);
	}

	@PutMapping("/dictionary/protected-areas/{id}")
	@PreAuthorize("hasAuthority('MANAGER')")
	public void updateProtectedArea(@PathVariable int id, @Valid @RequestBody ProtectedAreaWrite w) {
		service.updateProtectedArea(id, w);
	}

	@DeleteMapping("/dictionary/protected-areas/{id}")
	@PreAuthorize("hasAuthority('MANAGER')")
	public void deleteProtectedArea(@PathVariable int id) {
		service.deleteProtectedArea(id);
	}

	// ---- climate
	@PostMapping("/dictionary/climate-types")
	@PreAuthorize("hasAuthority('MANAGER')")
	public void addClimate(@Valid @RequestBody ClimateWrite w) {
		service.addClimate(w);
	}

	// ---- ecosystem typology
	@PostMapping("/dictionary/realms")
	@PreAuthorize("hasAuthority('MANAGER')")
	public void addRealms(@Valid @RequestBody CodeNameBatch b) {
		service.addRealms(b);
	}

	@PostMapping("/dictionary/biomes")
	@PreAuthorize("hasAuthority('MANAGER')")
	public void addBiomes(@Valid @RequestBody CodeNameBatch b) {
		service.addBiomes(b);
	}

	@PostMapping("/dictionary/efgs")
	@PreAuthorize("hasAuthority('MANAGER')")
	public void addEfgs(@Valid @RequestBody CodeNameBatch b) {
		service.addEfgs(b);
	}

	@PostMapping("/dictionary/ecosystems")
	@PreAuthorize("hasAuthority('MANAGER')")
	public void addEcosystem(@Valid @RequestBody EcosystemChain w) {
		service.addEcosystemChain(w);
	}

	// ---- services
	@PostMapping("/dictionary/eservices")
	@PreAuthorize("hasAuthority('MANAGER')")
	public void addEservices(@Valid @RequestBody CodeNameBatch b) {
		service.addEservices(b);
	}

	@PostMapping("/dictionary/esubservices")
	@PreAuthorize("hasAuthority('MANAGER')")
	public void addEsubservices(@Valid @RequestBody CodeNameBatch b) {
		service.addEsubservices(b);
	}

	@PostMapping("/dictionary/services")
	@PreAuthorize("hasAuthority('MANAGER')")
	public void addService(@Valid @RequestBody ServiceChain w) {
		service.addServiceChain(w);
	}
}

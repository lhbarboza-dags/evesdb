package org.sibol.eves.service;

import java.util.ArrayList;
import java.util.List;

import org.sibol.eves.repositories.ProtectedAreasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import model.Protectedarea;

@Service("PaService")
public class PaService {
	@Autowired
	private ProtectedAreasRepository repository;

	public List<Protectedarea> getAllProtectedArea() {
		Iterable<Protectedarea> iterable = repository.findAll(Sort.by(Sort.Direction.ASC, "name"));

		List<Protectedarea> result = new ArrayList<Protectedarea>();
		iterable.forEach(result::add);

		return result;
	}

	public List<Protectedarea> getAllProtectedAreaIsDeleted(boolean isDeleted) {

		return repository.findByisdeleted(isDeleted, Sort.by(Sort.Direction.ASC, "name"));
	}

	public Protectedarea save(Protectedarea pa) {

		return repository.save(pa);
	}

	public List<Protectedarea> getProtectedAreaByName(String name) {

		return repository.findByName(name);
	}

	public void deletePA(Protectedarea pa) {
		repository.delete(pa);

	}

//	public void test() {
//		// Save a new Country
//		Country newCountry = new Country();
//		newCountry.setCountryName("Philippines");
//		newCountry.setCountryCode("PHL");
//
//		repository.save(newCountry);
//
//		// Find a Country by ID
////		List<Country> result = repository.findById(1);
////		result.if Present(Country -> System.out.println(Country));
//
//		// Find Countrys by last name
//		List<Country> Countrys = repository.findByCountryName("Philippines");
//		Countrys.forEach(Country -> System.out.println(Country));
//
//		// List all Countrys
//		Iterable<Country> iterator = repository.findAll();
//		iterator.forEach(Country -> System.out.println(Country));
//
//		// Count number of Country
//		long count = repository.count();
//		System.out.println("Number of Countrys: " + count);
//	}
}
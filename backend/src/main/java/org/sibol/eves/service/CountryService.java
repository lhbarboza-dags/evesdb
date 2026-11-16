package org.sibol.eves.service;

import java.util.ArrayList;
import java.util.List;

import org.sibol.eves.repositories.CountryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import model.Country;

@Service("CountryService")
@Transactional
public class CountryService {
	@Autowired
	private CountryRepository repository;

	public List<Country> getAllCountry() {
		Iterable<Country> iterable = repository.findAll(Sort.by(Sort.Direction.ASC, "ccode"));

		List<Country> result = new ArrayList<Country>();
		
		iterable.forEach(result::add);

		return result;
	}
	
	public List<Country> getAllCountryIsDeleted(boolean isDeleted) {
		

		return repository.findByisdeleted(isDeleted, Sort.by(Sort.Direction.ASC, "cname"));
	}

	public Country save(Country country) {

		return repository.save(country);
	}

	public List<Country> getCountryByName(String countryName) {

		return repository.findBycname(countryName);
	}

	public List<Country> getCountryById(int countryId) {
		return repository.findBycid(countryId);
	}
	
	public List<Country> getDeleteById(int countryId) {
		return repository.findBycid(countryId);
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
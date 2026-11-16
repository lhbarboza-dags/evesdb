package org.sibol.eves.service;

import java.util.ArrayList;
import java.util.List;

import org.sibol.eves.repositories.CvTermRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import model.Country;
import model.Cvterm;

@Service("CvtermService")
public class CvTermService {
	@Autowired
	private CvTermRepository repository;

	public List<Cvterm> getAllCvterm() {
		Iterable<Cvterm> iterable = repository.findAll();

		List<Cvterm> result = new ArrayList<Cvterm>();
		iterable.forEach(result::add);

		return result;
	}

	public List<Cvterm> getAllCvtermIsDeleted(boolean isDeleted) {

		return repository.findByisdeleted(isDeleted, Sort.by(Sort.Direction.ASC, "name"));
	}

	public Cvterm save(Cvterm Cvterm) {

		return repository.save(Cvterm);
	}

	public List<Cvterm> getAllById(int id) {

		return repository.findBycvtermid(id);
	}

	public List<Cvterm> getAllByTerm(int id) {

		return repository.findBycvtermid(id);
	}

	public List<Cvterm> getAllByCvtermname(String name) {

		return repository.findByname(name);
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
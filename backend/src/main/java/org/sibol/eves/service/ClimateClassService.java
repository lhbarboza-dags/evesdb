package org.sibol.eves.service;

import java.util.ArrayList;
import java.util.List;

import org.sibol.eves.repositories.ClimateClassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import model.Climateclass;

@Service("ClimateClassService")
public class ClimateClassService {
	@Autowired
	private ClimateClassRepository repository;

	public List<Climateclass> getAllCountryInfo() {
		Iterable<Climateclass> iterable = repository.findAll();

		List<Climateclass> result = new ArrayList<Climateclass>();
		iterable.forEach(result::add);

		return result;
	}

	public Climateclass save(Climateclass climateClass) {

		return repository.save(climateClass);
	}

}
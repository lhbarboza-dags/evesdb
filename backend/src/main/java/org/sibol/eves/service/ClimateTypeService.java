package org.sibol.eves.service;

import java.util.ArrayList;
import java.util.List;

import org.sibol.eves.repositories.ClimateTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import model.Climatetype;

@Service("ClimateTypeService")
public class ClimateTypeService {
	@Autowired
	private ClimateTypeRepository repository;

	public List<Climatetype> getAllClimateType() {
		Iterable<Climatetype> iterable = repository.findAll(Sort.by(Sort.Direction.ASC, "cltname"));

		List<Climatetype> result = new ArrayList<Climatetype>();
		iterable.forEach(result::add);

		return result;
	}

	public Climatetype save(Climatetype climateType) {

		return repository.save(climateType);
	}

}
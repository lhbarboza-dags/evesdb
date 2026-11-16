package org.sibol.eves.service;

import java.util.ArrayList;
import java.util.List;

import org.sibol.eves.repositories.ClimateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import model.Climate;

@Service("ClimateService")
@Transactional
public class ClimateService {
	@Autowired
	private ClimateRepository repository;

	public List<Climate> getAllClimate() {
		Iterable<Climate> iterable = repository.findAll();

		List<Climate> result = new ArrayList<Climate>();
		iterable.forEach(result::add);

		return result;
	}

	public Climate save(Climate climate) {

		return repository.save(climate);
	}

}
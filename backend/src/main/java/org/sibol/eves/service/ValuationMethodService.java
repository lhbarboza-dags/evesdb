package org.sibol.eves.service;

import java.util.ArrayList;
import java.util.List;

import org.sibol.eves.repositories.ValuationMethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import model.Valuationmethod;

@Service("ValuationMethodService")
@Transactional
public class ValuationMethodService {
	@Autowired
	private ValuationMethodRepository repository;

	public List<Valuationmethod> getAllValuationmethod() {
		Iterable<Valuationmethod> iterable = repository.findAll();

		List<Valuationmethod> result = new ArrayList<Valuationmethod>();
		iterable.forEach(result::add);

		return result;
	}

	public Valuationmethod save(Valuationmethod vm) {

		return repository.save(vm);
	}

}
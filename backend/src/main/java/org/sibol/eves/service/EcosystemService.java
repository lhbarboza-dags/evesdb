package org.sibol.eves.service;

import java.util.ArrayList;
import java.util.List;

import org.sibol.eves.repositories.EcosystemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import model.Ecosystem;

@Service("EcosystemService")
@Transactional
public class EcosystemService {
	@Autowired
	private EcosystemRepository repository;

	public List<Ecosystem> getAllEcosytem() {
		Iterable<Ecosystem> iterable = repository.findAll();

		List<Ecosystem> result = new ArrayList<Ecosystem>();
		iterable.forEach(result::add);

		return result;
	}

	public Ecosystem save(Ecosystem eco) {

		return repository.save(eco);
	}

}
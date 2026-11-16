package org.sibol.eves.service;

import java.util.ArrayList;
import java.util.List;

import org.sibol.eves.repositories.EServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import model.Eservice;

@Service("EService")
public class EServiceService {
	@Autowired
	private EServiceRepository repository;

	public List<Eservice> getAllEService() {
		Iterable<Eservice> iterable = repository.findAll(Sort.by(Sort.Direction.ASC, "code"));

		List<Eservice> result = new ArrayList<Eservice>();
		iterable.forEach(result::add);

		return result;
	}

	public Eservice save(Eservice serv) {

		return repository.save(serv);
	}

	public List<Eservice> getByEserviceCode(String code) {

		return repository.findBycode(code);
	}

	public Eservice getEcoServiceId(int ecoService_id) {
		return repository.findByeserviceid(ecoService_id).get(0);
	}

	public List<Eservice> getByCodeOrName(String value, String name) {
		return repository.findBycodeOrName(value, name);
	}

}
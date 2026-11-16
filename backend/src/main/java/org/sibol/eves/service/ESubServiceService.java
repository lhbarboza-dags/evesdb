package org.sibol.eves.service;

import java.util.ArrayList;
import java.util.List;

import org.sibol.eves.repositories.SubServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import model.Esubservice;

@Service("ESubService")
public class ESubServiceService {
	@Autowired
	private SubServiceRepository repository;

	public List<Esubservice> getAllEcoSubService() {
		Iterable<Esubservice> iterable = repository.findAll(Sort.by(Sort.Direction.ASC, "code"));

		List<Esubservice> result = new ArrayList<Esubservice>();
		iterable.forEach(result::add);

		return result;
	}

	public Esubservice save(Esubservice serv) {

		return repository.save(serv);
	}

	public List<Esubservice> getByEcoSubServiceCode(String value) {
		return repository.findBycode(value);
	}

//	public List<Esubservice> getByServiceId(int ecoService_id) {
//		return repository.findByeserviceid(ecoService_id);
//	}

}
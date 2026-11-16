package org.sibol.eves.repositories;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import model.Eservice;

public interface EServiceRepository extends CrudRepository<Eservice, Long> {
	
	List<Eservice> findBycode(String code);
	
	List<Eservice> findByeserviceid(int eserviceid);

	List<Eservice> findBycodeOrName(String value, String name);

	Iterable<Eservice> findAll(Sort by);
	

}

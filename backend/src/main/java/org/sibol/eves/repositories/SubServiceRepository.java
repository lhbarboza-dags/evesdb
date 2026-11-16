package org.sibol.eves.repositories;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import model.Esubservice;

public interface SubServiceRepository extends CrudRepository<Esubservice, Long> {
	List<Esubservice> findByname(String realmName);

	List<Esubservice> findBycode(String value);

	List<Esubservice> findByesubserviceid(int esubService_id);

	Iterable<Esubservice> findAll(Sort by);
	
//	List<Esubservice> findByeserviceid(int eService_id);

}

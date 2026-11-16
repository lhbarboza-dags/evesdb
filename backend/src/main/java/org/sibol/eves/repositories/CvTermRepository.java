package org.sibol.eves.repositories;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import model.Cvterm;

public interface CvTermRepository extends CrudRepository<Cvterm, Long> {
	List<Cvterm> findByname(String cvtermName);

	List<Cvterm> findBycvtermid(int id);

	List<Cvterm> findByisdeleted(boolean isDeleted, Sort by);



}

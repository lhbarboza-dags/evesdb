package org.sibol.eves.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import model.Cv;



public interface CvRepository extends CrudRepository<Cv, Long> {
	List<Cv> findBycvname(String cvName);

	
}

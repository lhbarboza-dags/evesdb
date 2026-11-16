package org.sibol.eves.repositories;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import model.Protectedarea;



public interface ProtectedAreasRepository extends CrudRepository<Protectedarea, Long> {
	List<Protectedarea> findByName(String paName);

	Iterable<Protectedarea> findAll(Sort by);

	List<Protectedarea> findByisdeleted(boolean isDeleted, Sort by);

}

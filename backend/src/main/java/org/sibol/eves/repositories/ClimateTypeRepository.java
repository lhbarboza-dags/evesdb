package org.sibol.eves.repositories;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import model.Climatetype;

public interface ClimateTypeRepository extends CrudRepository<Climatetype, Long> {
	List<Climatetype> findBycltid(Integer id);

	Iterable<Climatetype> findAll(Sort by);

}

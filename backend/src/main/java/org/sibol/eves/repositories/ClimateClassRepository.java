package org.sibol.eves.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import model.Climateclass;

public interface ClimateClassRepository extends CrudRepository<Climateclass, Long> {

	List<Climateclass> findByccid(Integer id);

}

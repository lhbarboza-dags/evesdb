package org.sibol.eves.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import model.Dataset;

public interface DatasetRepository extends CrudRepository<Dataset, Long> {

	List<Dataset> findBystudysrc(int studyid);

	List<Dataset> findBydatasetid(int datasetid);
	
	

}

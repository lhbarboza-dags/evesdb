package org.sibol.eves.service;

import java.util.ArrayList;
import java.util.List;

import org.sibol.eves.repositories.DatasetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import model.Dataset;

@Service("DatasetService")
public class DatasetService {
	@Autowired
	private DatasetRepository repository;

	public List<Dataset> getAllStudySource_Site() {
		Iterable<Dataset> iterable = repository.findAll();

		List<Dataset> result = new ArrayList<Dataset>();
		iterable.forEach(result::add);

		return result;
	}

	public Dataset save(Dataset stdySrcSite) {

		return repository.save(stdySrcSite);
	}

	public List<Dataset> getDSByStudySrcId(int studyid) {
		return repository.findBystudysrc(studyid);
	}
	
	public Dataset getDSBydatasetId(int datasetid) {
		return repository.findBydatasetid(datasetid).get(0);
	}
	
	public void deleteById(int datasetid) {
		repository.delete(getDSBydatasetId(datasetid));
	}

	public void deleteDs(Dataset data) {
		repository.delete(data);
		
	}

}
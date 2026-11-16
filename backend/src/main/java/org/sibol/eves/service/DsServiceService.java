package org.sibol.eves.service;

import java.util.ArrayList;
import java.util.List;

import org.sibol.eves.repositories.DsServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("DsService")
@Transactional
public class DsServiceService {
	@Autowired
	private DsServiceRepository repository;

	public List<model.Service> getAllEcosytem() {
		Iterable<model.Service> iterable = repository.findAll();

		List<model.Service> result = new ArrayList<model.Service>();
		iterable.forEach(result::add);

		return result;
	}

	public model.Service save(model.Service eco) {

		return repository.save(eco);
	}

}
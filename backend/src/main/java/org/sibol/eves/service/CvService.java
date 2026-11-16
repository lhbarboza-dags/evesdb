package org.sibol.eves.service;

import java.util.ArrayList;
import java.util.List;

import org.sibol.eves.repositories.CvRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import model.Cv;

@Service("CvService")
public class CvService {
	@Autowired
	private CvRepository repository;

	public List<Cv> getAllCv() {
		Iterable<Cv> iterable = repository.findAll();

		List<Cv> result = new ArrayList<Cv>();
		iterable.forEach(result::add);

		return result;
	}

	public Cv save(Cv cv) {

		return repository.save(cv);
	}

	public List<Cv> getByCvName(String name) {

		return repository.findBycvname(name);

	}

}
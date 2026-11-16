package org.sibol.eves.service;

import java.util.ArrayList;
import java.util.List;

import org.sibol.eves.repositories.SiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import model.Site;

@Service("StudySiteService")
public class SiteService {
	@Autowired
	private SiteRepository repository;

	public List<Site> getAllStudySite() {
		Iterable<Site> iterable = repository.findAll();

		List<Site> result = new ArrayList<Site>();
		iterable.forEach(result::add);

		return result;
	}

	public Site save(Site studySite) {

		return repository.save(studySite);
	}

	public Site getById(int studySite_id) {
		return repository.findBysiteid(studySite_id);

	}

}
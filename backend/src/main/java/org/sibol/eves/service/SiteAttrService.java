package org.sibol.eves.service;

import java.util.ArrayList;
import java.util.List;

import org.sibol.eves.repositories.SiteAttrRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import model.Siteattr;

@Service("SiteAttrService")
@Transactional
public class SiteAttrService {
	@Autowired
	private SiteAttrRepository repository;

	public List<Siteattr> getAllSiteAttr() {
		Iterable<Siteattr> iterable = repository.findAll();

		List<Siteattr> result = new ArrayList<Siteattr>();
		iterable.forEach(result::add);

		return result;
	}

	public Siteattr save(Siteattr sa) {

		return repository.save(sa);
	}

}
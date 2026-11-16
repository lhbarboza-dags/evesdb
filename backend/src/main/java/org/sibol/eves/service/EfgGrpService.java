package org.sibol.eves.service;

import java.util.ArrayList;
import java.util.List;

import org.sibol.eves.repositories.EfgGrpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import model.Efgrp;

@Service("EfgGrpService")
@Transactional
public class EfgGrpService {
	@Autowired
	private EfgGrpRepository repository;

	public List<Efgrp> getAllEfgGrp() {
		Iterable<Efgrp> iterable = repository.findAll();

		List<Efgrp> result = new ArrayList<Efgrp>();
		iterable.forEach(result::add);

		return result;
	}

	public Efgrp save(Efgrp EfgGrp) {

		return repository.save(EfgGrp);
	}

	public List<Efgrp> getByEfgCode(String value) {
		return repository.findBycode(value);
	}

	public List<Efgrp> getByBiomeId(int biomeId) {
		return repository.findBybiome(biomeId);
	}

}
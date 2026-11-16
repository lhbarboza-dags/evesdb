package org.sibol.eves.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import model.Efgrp;


public interface EfgGrpRepository extends CrudRepository<Efgrp, Long> {
	List<Efgrp> findByname(String efgName);

	List<Efgrp> findBycode(String value);

	List<Efgrp> findBybiome(int biomeId);

}

package org.sibol.eves.repositories;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import model.Countryinfo;

public interface CountryInfoRepository extends CrudRepository<Countryinfo, Long> {

	List<Countryinfo> findByYear(Integer year);

	void deleteByciid(int ciid);

	List<Countryinfo> findByisdeleted(boolean isDeleted, Sort by);

	

	//List<Countryinfo> findByYearAndcountryid_cid(Integer year, Integer country);

}

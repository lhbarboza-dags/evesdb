package org.sibol.eves.repositories;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import model.Country;



public interface CountryRepository extends CrudRepository<Country, Long> {

	List<Country> findBycname(String countryName);

	List<Country> findBycid(int countryId);

	Iterable<Country> findAll(Sort by);

	List<Country> findByisdeleted(boolean isDeleted);

	List<Country> findByisdeleted(boolean isDeleted, Sort by);

}

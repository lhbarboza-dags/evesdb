package org.sibol.eves.service;

import java.util.ArrayList;
import java.util.List;

import org.sibol.eves.repositories.CountryInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import model.Country;
import model.Countryinfo;

@Service("CountryInfoService")
@Transactional
public class CountryInfoService {
	@Autowired
	private CountryInfoRepository repository;

	public List<Countryinfo> getAllCountryInfo() {
		Iterable<Countryinfo> iterable = repository.findAll();

		List<Countryinfo> result = new ArrayList<Countryinfo>();
		iterable.forEach(result::add);

		return result;
	}

	public List<Countryinfo> getAllCountryInfoIsDeleted(boolean isDeleted) {

		return repository.findByisdeleted(isDeleted, Sort.by(Sort.Direction.ASC, "year"));
	}

	public Countryinfo save(Countryinfo country) {

		return repository.save(country);
	}

	public List<Countryinfo> getCountryInfoByYear(int year) {

		return repository.findByYear(year);
	}

	public void deleteCountryInfo(Countryinfo ci) {
		repository.delete(ci);
	}

//	public List<Countryinfo> getCountryInfoByYearAndCountry(Integer year, Integer country) {
//
//		return repository.findByYearAndcountryid_cid(year, country);
//	}

}
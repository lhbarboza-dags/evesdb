package org.sibol.eves.service;

import java.util.ArrayList;
import java.util.List;

import org.sibol.eves.repositories.BiomeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import model.Biome;
import model.Realm;

@Service("BiomeService")
@Transactional
public class BiomeService {
	@Autowired
	private BiomeRepository repository;

	public List<Biome> getAllBiome() {
		Iterable<Biome> iterable = repository.findAll();

		List<Biome> result = new ArrayList<Biome>();
		iterable.forEach(result::add);

		return result;
	}

	public Biome save(Biome Biome) {

		return repository.save(Biome);
	}

	public Biome getById(int biomeId) {
		return repository.findBybiomeid(biomeId).get(0);

	}

	public List<Biome> getByBiomeCode(String value) {
		return repository.findBybcode(value);
	}

	public List<Biome> getByRealm(Realm realm) {
		return repository.findByrealm(realm);
	}

	public List<Biome> getByCodeOrName(String code, String name) {
		return repository.findBybcodeOrBname(code, name);
	}

}
package org.sibol.eves.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import model.Biome;
import model.Realm;

public interface BiomeRepository extends CrudRepository<Biome, Long> {
	List<Biome> findBybname(String name);

	List<Biome> findBybiomeid(Integer id);

	List<Biome> findBybcode(String value);

	//List<Biome> findByrealmid(int realmId);
	
	List<Biome> findByrealm(Realm realm);

	List<Biome> findBybcodeOrBname(String code, String name);

}

package org.sibol.eves.repositories;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import model.Realm;

public interface RealmRepository extends CrudRepository<Realm, Long> {
	List<Realm> findByname(String realmName);

	List<Realm> findByrealmid(int realmid);

	List<Realm> findBycode(String code);

	List<Realm> findBycodeOrName(String code, String name);

	Iterable<Realm> findAll(Sort by);

	

}

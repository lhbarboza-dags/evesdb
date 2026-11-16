package org.sibol.eves.service;

import java.util.ArrayList;
import java.util.List;

import org.sibol.eves.repositories.RealmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import model.Realm;

@Service("RealmService")
@Transactional
public class RealmService {
	@Autowired
	private RealmRepository repository;

	public List<Realm> getAllRealm() {
		Iterable<Realm> iterable = repository.findAll(Sort.by(Sort.Direction.ASC, "code"));

		List<Realm> result = new ArrayList<Realm>();
		iterable.forEach(result::add);

		return result;
	}

	public Realm save(Realm realm) {

		return repository.save(realm);
	}

//	public Realm getById(int realmId) {
//
//		return repository.findByrealmid(realmId).get(0);
//	}

	public List<Realm> getByRealmCode(String code) {
		return repository.findBycode(code);

	}

	public Realm getByRealmId(int realmId) {
		return repository.findByrealmid(realmId).get(0);
	}

	public List<Realm> getByCodeOrName(String code, String name) {
		return repository.findBycodeOrName(code, name);
	}

}
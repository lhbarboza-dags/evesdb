package org.sibol.eves.service;

import java.util.ArrayList;
import java.util.List;

import org.sibol.eves.repositories.ValueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import model.Value;

@Service("ValueService")
public class ValueService {
	@Autowired
	private ValueRepository repository;

	public List<Value> getAllValuation() {
		Iterable<Value> iterable = repository.findAll();

		List<Value> result = new ArrayList<Value>();
		iterable.forEach(result::add);

		return result;
	}

	public Value save(Value val) {

		return repository.save(val);
	}

	public Value getById(int valueId) {
		return repository.findByvalueid(valueId);
	}

}
package org.sibol.eves.repositories;

import org.springframework.data.repository.CrudRepository;

import model.Value;

public interface ValueRepository extends CrudRepository<Value, Long> {

	Value findByvalueid(int valuation);

}

package org.sibol.eves.repositories;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.repository.CrudRepository;

import model.Studysrc;

public interface StudySourceRepository extends CrudRepository<Studysrc, Long> {

	List<Studysrc> findBysrcid(int id);

	List<Studysrc> findBystatus(int i);

	List<Studysrc> findAll(Direction asc, String string);

	List<Studysrc> findByisdeleted(boolean isDeleted, Sort by);
	
	List<Studysrc> findByStatusAndIsdeleted(int i, boolean isdeleted, Sort by);

}

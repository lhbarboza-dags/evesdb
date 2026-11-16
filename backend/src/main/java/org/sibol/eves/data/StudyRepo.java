package org.sibol.eves.data;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import model.Studysrc;

public interface StudyRepo extends JpaRepository<Studysrc, Integer> {
	@Query("select s from Studysrc s where s.isdeleted = false order by lower(s.title)")
	List<Studysrc> findActive();

	@Query("select s from Studysrc s where s.isdeleted = false and s.status in (0, 1)")
	List<Studysrc> findSearchable();
}

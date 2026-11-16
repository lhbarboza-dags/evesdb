package org.sibol.eves.data;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import model.Cv;
import model.Cvterm;

public interface CvtermRepo extends JpaRepository<Cvterm, Integer> {
	@Query("select t from Cvterm t where t.cv.cvname in :cvNames and (:withDeleted = true or t.isdeleted = false) order by t.code")
	List<Cvterm> findByCvNames(@Param("cvNames") List<String> cvNames, @Param("withDeleted") boolean withDeleted);

	@Query("select c from Cv c where c.cvname = :name")
	List<Cv> findCv(@Param("name") String name);
}

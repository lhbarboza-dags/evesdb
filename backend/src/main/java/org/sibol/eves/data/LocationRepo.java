package org.sibol.eves.data;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepo extends JpaRepository<model.Location, Integer> {
}

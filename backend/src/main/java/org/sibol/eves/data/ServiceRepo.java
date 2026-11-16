package org.sibol.eves.data;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepo extends JpaRepository<model.Service, Integer> {
}

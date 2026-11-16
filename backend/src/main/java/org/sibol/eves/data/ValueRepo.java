package org.sibol.eves.data;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ValueRepo extends JpaRepository<model.Value, Integer> {
}

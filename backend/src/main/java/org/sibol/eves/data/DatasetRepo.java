package org.sibol.eves.data;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasetRepo extends JpaRepository<model.Dataset, Integer> {
}

package org.sibol.eves.data;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepo extends JpaRepository<model.Country, Integer> {
}

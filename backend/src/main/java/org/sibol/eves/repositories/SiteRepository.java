package org.sibol.eves.repositories;

import org.springframework.data.repository.CrudRepository;

import model.Site;

public interface SiteRepository extends CrudRepository<Site, Long> {

	Site findBysiteid(int studySite_id);

}

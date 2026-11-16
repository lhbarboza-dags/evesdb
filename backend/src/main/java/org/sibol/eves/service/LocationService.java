package org.sibol.eves.service;

import java.util.ArrayList;
import java.util.List;

import org.sibol.eves.repositories.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import model.Location;

@Service("StudyLocationService")
public class LocationService {
	@Autowired
	private LocationRepository repository;

	public List<Location> getAllStudyLocation() {
		Iterable<Location> iterable = repository.findAll();

		List<Location> result = new ArrayList<Location>();
		iterable.forEach(result::add);

		return result;
	}

	public Location save(Location studyloc) {

		return repository.save(studyloc);
	}

}
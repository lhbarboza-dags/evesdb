package org.sibol.eves.service;

import java.util.ArrayList;
import java.util.List;

import org.sibol.eves.repositories.StudySourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import model.Country;
import model.Studysrc;

@Service("StudySourceService")
public class StudySourceService {
	@Autowired
	private StudySourceRepository repository;

	public List<Studysrc> getAllStudySrc() {
		Iterable<Studysrc> iterable = repository.findAll(Sort.Direction.ASC, "title");

		List<Studysrc> result = new ArrayList<Studysrc>();
		iterable.forEach(result::add);

		return result;
	}

	public List<Studysrc> getAllStudySrcIsDeleted(boolean isDeleted) {

		return repository.findByisdeleted(isDeleted, Sort.by(Sort.Direction.ASC, "title"));
	}

	public Studysrc save(Studysrc studySrc) {

		return repository.save(studySrc);
	}

	public List<Studysrc> getByStudySrcId(int studysrcid) {

		return repository.findBysrcid(studysrcid);
	}

	public List<Studysrc> getStudyByStatus(int i) {
		return repository.findBystatus(i);
	}

	public List<Studysrc> getStudyByStatusAndIsdeleted(int i, boolean isdeleted) {
		return repository.findByStatusAndIsdeleted(i, isdeleted, Sort.by(Sort.Direction.ASC, "title"));
	}

}
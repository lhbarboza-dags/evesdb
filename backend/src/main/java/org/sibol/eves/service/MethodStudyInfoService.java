package org.sibol.eves.service;

import java.util.ArrayList;
import java.util.List;

import org.sibol.eves.repositories.MethodStudyInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import model.Methdstudyinfo;

@Service("MethodStudyInfoService")
@Transactional
public class MethodStudyInfoService {
	@Autowired
	private MethodStudyInfoRepository repository;

	public List<Methdstudyinfo> getAllMethodStudyInfo() {
		Iterable<Methdstudyinfo> iterable = repository.findAll();

		List<Methdstudyinfo> result = new ArrayList<Methdstudyinfo>();
		iterable.forEach(result::add);

		return result;
	}

	public Methdstudyinfo save(Methdstudyinfo vm) {

		return repository.save(vm);
	}

}
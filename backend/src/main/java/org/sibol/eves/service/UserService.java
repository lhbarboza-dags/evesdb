package org.sibol.eves.service;

import java.util.ArrayList;
import java.util.List;

import org.sibol.eves.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import model.User;

@Service("UserService")
public class UserService {
	@Autowired
	private UserRepository repository;

	public List<User> getAllUser() {
		Iterable<User> iterable = repository.findAll();

		List<User> result = new ArrayList<User>();
		iterable.forEach(result::add);

		return result;
	}

	public User save(User User) {

		return repository.save(User);
	}

	public User getById(int UserId) {
		return repository.findByuserid(UserId).get(0);

	}

	public List<User> getByEmailAddress(String nm) {
		return repository.findByemailadd(nm);

	}
	
	public List<User> getByRole(Integer roleid) {
		return repository.findByrole(roleid);

	}

	public List<User> getByValidationToken(String token) {
		return repository.findByvalidation(token);
		
	}
	
	public List<User> getByValidationBugReport(String token) {
		return repository.findBybugreport(token);
		
	}
	
	public List<User> getByValidationImprovement(String token) {
		return repository.findByimprovement(token);
		
	}
	
	public List<User> getByValidationSuggestAstudy(String token) {
		return repository.findBysuggestastudy(token);
		
	}
	
	public List<User> getByValidationInquiry(String token) {
		return repository.findByinquiry(token);
		
	}

}
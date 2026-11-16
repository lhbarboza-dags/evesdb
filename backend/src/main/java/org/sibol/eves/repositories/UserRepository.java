package org.sibol.eves.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import model.User;

public interface UserRepository extends CrudRepository<User, Long> {

	List<User> findByuserid(int userId);

	List<User> findByemailadd(String nm);

	List<User> findByvalidation(String token);

	List<User> findByrole(Integer roleid);

	List<User> findBybugreport(String token);

	List<User> findByimprovement(String token);

	List<User> findBysuggestastudy(String token);

	List<User> findByinquiry(String token);

}

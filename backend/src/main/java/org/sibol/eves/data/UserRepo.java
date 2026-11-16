package org.sibol.eves.data;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import model.User;

public interface UserRepo extends JpaRepository<User, Integer> {
	@Query("select u from User u where lower(trim(u.emailadd)) = lower(trim(:email))")
	List<User> findByEmail(@Param("email") String email);

	default Optional<User> findOneByEmail(String email) {
		return findByEmail(email).stream().findFirst();
	}

	List<User> findByValidation(String validation);

	List<User> findByRole(int role);
}

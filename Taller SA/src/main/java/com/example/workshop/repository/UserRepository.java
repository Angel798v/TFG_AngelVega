package com.example.workshop.repository;

import com.example.workshop.model.Role;
import com.example.workshop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

	User findByUsername(String username);

	User findByEmail(String email);

	List<User> findByRole(Role role);

	void deleteById(long id);

}
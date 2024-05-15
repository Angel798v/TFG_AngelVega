package com.example.workshop.repository;

import com.example.workshop.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role,Integer> {

	Role findByRoleName(String roleName);

}
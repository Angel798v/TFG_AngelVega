package com.example.workshop.service;

import com.example.workshop.dto.UserDtoPsw;
import com.example.workshop.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import com.example.workshop.dto.RoleDto;
import java.util.List;

public interface UserService extends UserDetailsService {

	public User save(UserDtoPsw userDtoPsw);

	public List<User> listUsers();

	boolean registryAdmin();

	void createRoles(List<RoleDto> roles);

	void adminComprobation();

}
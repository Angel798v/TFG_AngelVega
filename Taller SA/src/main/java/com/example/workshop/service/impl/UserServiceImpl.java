package com.example.workshop.service.impl;

import com.example.workshop.dto.RoleDto;
import com.example.workshop.dto.UserDtoPsw;
import com.example.workshop.model.Role;
import com.example.workshop.model.User;
import com.example.workshop.repository.RoleRepository;
import com.example.workshop.repository.UserRepository;
import com.example.workshop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

	private boolean adminComprobacion = false;


	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private BCryptPasswordEncoder encoder;


	@Override
	public User save(UserDtoPsw userDtoPsw) {

		Role rol = roleRepository.findByRoleName("ROLE_USER");

		User user = new User(
			userDtoPsw.getUsername(),
			encoder.encode(userDtoPsw.getPassword()),
			userDtoPsw.getEmail(),
			userDtoPsw.getDireccion(),
			userDtoPsw.getApellidos(),
			rol);

		return userRepository.save(user);
	}


	@Override
	public List<User> listUsers() {

		return userRepository.findAll();
	}


	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		User user = userRepository.findByUsername(username);

		if(user == null){
			throw new UsernameNotFoundException("The user doesnt exists");
		}

		Set<GrantedAuthority> rol = new HashSet<>();
		rol.add(new SimpleGrantedAuthority(user.getRole().getRoleName()));

		return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), rol);
	}


	@Override
	public boolean registryAdmin(){

		Role roleAdmin = roleRepository.findByRoleName("ROLE_ADMIN");
		List<User> admins = userRepository.findByRole(roleAdmin);
		for(User usr : admins){
			return false;
		}

		User admin = new User(
			"angelAdmin",
			encoder.encode("admin"),
			"angel@gmail.com",
			"roquedp",
			"vega",
			roleAdmin);

		userRepository.save(admin);

		return true;
	}


	@Override
	public void createRoles(List<RoleDto> roles){

		for(RoleDto role : roles) {
			if (roleRepository.findByRoleName(role.getRoleName()) == null) {
				roleRepository.save(new Role(role.getId(), "ROLE_" + role.getRoleName().toUpperCase()));
			}
		}

	}


	@Override
	public void adminComprobation(){

		if(!adminComprobacion) {
			List<RoleDto> roles = new ArrayList<RoleDto>();
			RoleDto admin = new RoleDto( 1,"ADMIN");
			roles.add(admin);
			RoleDto user = new RoleDto( 2,"USER");
			roles.add(user);
			createRoles(roles);
			registryAdmin();
			adminComprobacion = true;
		}
	}

}
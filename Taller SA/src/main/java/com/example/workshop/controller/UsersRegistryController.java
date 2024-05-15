package com.example.workshop.controller;

import com.example.workshop.dto.UserDtoPsw;
import com.example.workshop.model.User;
import com.example.workshop.repository.RoleRepository;
import com.example.workshop.repository.UserRepository;
import com.example.workshop.service.UrlService;
import com.example.workshop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UsersRegistryController {

	@Autowired
	private UserService userService;

	@Autowired
	private UrlService urlService;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BCryptPasswordEncoder encoder;


	@ModelAttribute("usuario")
	public UserDtoPsw newUserDtoPsw(){
		return new UserDtoPsw();
	}


	@GetMapping("/registro")
	public String mostrarRegistro(){

		urlService.setUrl("/registro");
		return "/Users/registry";
	}

	@PostMapping("/registro")
	public String registryUser(@ModelAttribute("usuario") UserDtoPsw userDtoPsw){

		if(userRepository.findByUsername(userDtoPsw.getUsername()) != null ||
			userRepository.findByEmail(userDtoPsw.getEmail()) != null){
			return "redirect:/registro?error";
		}

		userService.save(userDtoPsw);

		return "redirect:/login?exito";
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@GetMapping("/usuarios/registry")
	public String userRegistryAdmin(Model model){

		urlService.setUrl("/usuarios/registry");
		model.addAttribute("roles", roleRepository.findAll());

		return "/Users/admin/registryAdmin";
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@PostMapping("/usuarios/registry")
	public String userRegistryAdmin(UserDtoPsw userDtoPsw){

		if(userRepository.findByUsername(userDtoPsw.getUsername()) != null ||
			userRepository.findByEmail(userDtoPsw.getEmail()) != null){
			return "redirect:/usuarios?fallo";
		}

		User user = new User(userDtoPsw.getUsername(),
			encoder.encode(userDtoPsw.getPassword()),
			userDtoPsw.getEmail(),
			userDtoPsw.getDireccion(),
			userDtoPsw.getApellidos(),
			roleRepository.findByRoleName(userDtoPsw.getRole()));

		userRepository.save(user);

		return "redirect:/usuarios?exito";
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@PostMapping("/usuarios/delete/{id}")
	public String userDeleteAdmin(@PathVariable(name = "id") long id){

		userRepository.deleteById(id);

		return "redirect:/usuarios?exitoDelete";
	}

}
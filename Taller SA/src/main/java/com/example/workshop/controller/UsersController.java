package com.example.workshop.controller;

import com.example.workshop.service.UrlService;
import com.example.workshop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UsersController {

	@Autowired
	private UserService userService;

	@Autowired
	private UrlService urlService;

	@GetMapping("/login")
	public String vistaLogin(){

		urlService.setUrl("/login");

		return "/Users/login";
	}


	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@GetMapping("/usuarios")
	public String vistaUsuariosAdmin(Model model){

		urlService.setUrl("/usuarios");

		model.addAttribute("usuarios",userService.listUsers());

		return "/Users/admin/users";
	}

}
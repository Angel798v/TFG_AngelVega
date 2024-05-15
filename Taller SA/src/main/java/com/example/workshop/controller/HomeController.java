package com.example.workshop.controller;

import com.example.workshop.service.UrlService;
import com.example.workshop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

	@Autowired
	private UserService userService;

	@Autowired
	private UrlService urlService;


	@GetMapping("/")
	public String vistaPrincipal(){

		urlService.setUrl("/index");
		userService.adminComprobation();

		return "index";
	}

	@GetMapping("/index")
	public String vistaIndex(){

		urlService.setUrl("/index");
		userService.adminComprobation();

		return "index";
	}

	@GetMapping("/loginlogout")
	public String loginlogout(){

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if(auth.getPrincipal() == "anonymousUser"){
				return "redirect:/login";
			}else{
				return "redirect:/logout";
			}
		}

}
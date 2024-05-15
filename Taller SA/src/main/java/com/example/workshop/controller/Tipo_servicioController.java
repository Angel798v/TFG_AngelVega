package com.example.workshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.*;
import com.example.workshop.service.UrlService;
import com.example.workshop.model.Tipo_servicio;
import com.example.workshop.dto.Tipo_servicioDto;
import com.example.workshop.repository.Tipo_servicioRepository;
import com.example.workshop.model.Reparacion;
import com.example.workshop.dto.ReparacionDto;
import com.example.workshop.repository.ReparacionRepository;
import com.example.workshop.model.User;
import com.example.workshop.repository.UserRepository;


@Controller
@RequestMapping("/Tipo_servicio")
public class Tipo_servicioController {

	@Autowired
	private Tipo_servicioRepository tipo_servicioRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UrlService urlService;

	//Principal entity operations view

	@GetMapping
	public String principalTipo_servicio(){

		urlService.setUrl("/Tipo_servicio");

		return "/Tipo_servicio/principalTipo_servicio";
	}

	//Operation GET

	@GetMapping("/Get")
	public String operationGetTipo_servicio(Model model){

		urlService.setUrl("/Tipo_servicio/Get");
		model.addAttribute("listAll", tipo_servicioRepository.findAll());

		return "/Tipo_servicio/operationGet/operationGetTipo_servicio";
	}

	//Operation POST

	@GetMapping("/Post")
	public String operationPostTipo_servicio(Model model){

		urlService.setUrl("/Tipo_servicio/Post");
		model.addAttribute("object", new Tipo_servicioDto());

		return "/Tipo_servicio/operationPost/operationPostTipo_servicio";
	}

	@PostMapping("/Post")
	public String operationPost(Tipo_servicioDto tipo_servicioDto){

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		org.springframework.security.core.userdetails.User userConnected = (org.springframework.security.core.userdetails.User) auth.getPrincipal();
		User user = userRepository.findByUsername(userConnected.getUsername());

		Tipo_servicio tipo_servicio = new Tipo_servicio();
		tipo_servicio.setUser(user);

		tipo_servicioRepository.save(tipo_servicio);

		return "redirect:/Tipo_servicio?successPost";
	}

	//Operation PUT

	@GetMapping("/Put")
	public String operationPutTipo_servicio(Model model){

		urlService.setUrl("/Tipo_servicio/Put");
		model.addAttribute("listAll", tipo_servicioRepository.findAll());

		return "/Tipo_servicio/operationPut/operationPutTipo_servicioView";
	}

	@GetMapping("/Put/{id}")
	public String operationPutTipo_servicio(@PathVariable("id") long id, Model model){

		Tipo_servicio tipo_servicio = tipo_servicioRepository.findById(id);
		Tipo_servicioDto tipo_servicioDto = new Tipo_servicioDto();

		tipo_servicioDto.setId_tipo_servicio(tipo_servicio.getId_tipo_servicio());

		model.addAttribute("object", tipo_servicioDto);

		return "/Tipo_servicio/operationPut/operationPutTipo_servicio";
	}

	@PostMapping("/Put/{id}")
	public String operationPut(@PathVariable("id") long id, Tipo_servicioDto tipo_servicioDto){

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		org.springframework.security.core.userdetails.User userConnected = (org.springframework.security.core.userdetails.User) auth.getPrincipal();
		User user = userRepository.findByUsername(userConnected.getUsername());

		Tipo_servicio tipo_servicio = tipo_servicioRepository.findById(id);
		tipo_servicio.setUser(user);

		tipo_servicioRepository.save(tipo_servicio);

		return "redirect:/Tipo_servicio?successPut";
	}

	//Operation DELETE

	@GetMapping("/Delete")
	public String operationDeleteTipo_servicio(Model model){

		urlService.setUrl("/Tipo_servicio/Delete");
		model.addAttribute("listAll", tipo_servicioRepository.findAll());

		return "/Tipo_servicio/operationDelete/operationDeleteTipo_servicio";
	}

	@PostMapping("/Delete/{id}")
	public String operationDelete(@PathVariable("id") long id){

		try {
			tipo_servicioRepository.deleteById(id);
		}catch (Exception ex){
			return "redirect:/Tipo_servicio?failDelete";
		}

		return "redirect:/Tipo_servicio?successDelete";
	}


}
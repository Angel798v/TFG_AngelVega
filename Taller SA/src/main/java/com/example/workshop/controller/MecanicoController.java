package com.example.workshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.*;
import com.example.workshop.service.UrlService;
import com.example.workshop.model.Mecanico;
import com.example.workshop.dto.MecanicoDto;
import com.example.workshop.repository.MecanicoRepository;
import com.example.workshop.model.Reparacion;
import com.example.workshop.dto.ReparacionDto;
import com.example.workshop.repository.ReparacionRepository;
import com.example.workshop.model.User;
import com.example.workshop.repository.UserRepository;


@Controller
@RequestMapping("/Mecanico")
public class MecanicoController {

	@Autowired
	private MecanicoRepository mecanicoRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UrlService urlService;

	//Principal entity operations view

	@GetMapping
	public String principalMecanico(){

		urlService.setUrl("/Mecanico");

		return "/Mecanico/principalMecanico";
	}

	//Operation GET

	@GetMapping("/Get")
	public String operationGetMecanico(Model model){

		urlService.setUrl("/Mecanico/Get");
		model.addAttribute("listAll", mecanicoRepository.findAll());

		return "/Mecanico/operationGet/operationGetMecanico";
	}

	//Operation POST

	@GetMapping("/Post")
	public String operationPostMecanico(Model model){

		urlService.setUrl("/Mecanico/Post");
		model.addAttribute("object", new MecanicoDto());

		return "/Mecanico/operationPost/operationPostMecanico";
	}

	@PostMapping("/Post")
	public String operationPost(MecanicoDto mecanicoDto){

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		org.springframework.security.core.userdetails.User userConnected = (org.springframework.security.core.userdetails.User) auth.getPrincipal();
		User user = userRepository.findByUsername(userConnected.getUsername());

		Mecanico mecanico = new Mecanico();
		mecanico.setNombre(mecanicoDto.getNombre());
		mecanico.setUser(user);

		mecanicoRepository.save(mecanico);

		return "redirect:/Mecanico?successPost";
	}

	//Operation PUT

	@GetMapping("/Put")
	public String operationPutMecanico(Model model){

		urlService.setUrl("/Mecanico/Put");
		model.addAttribute("listAll", mecanicoRepository.findAll());

		return "/Mecanico/operationPut/operationPutMecanicoView";
	}

	@GetMapping("/Put/{id}")
	public String operationPutMecanico(@PathVariable("id") long id, Model model){

		Mecanico mecanico = mecanicoRepository.findById(id);
		MecanicoDto mecanicoDto = new MecanicoDto();

		mecanicoDto.setId_mecanico(mecanico.getId_mecanico());
		mecanicoDto.setNombre(mecanico.getNombre());

		model.addAttribute("object", mecanicoDto);

		return "/Mecanico/operationPut/operationPutMecanico";
	}

	@PostMapping("/Put/{id}")
	public String operationPut(@PathVariable("id") long id, MecanicoDto mecanicoDto){

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		org.springframework.security.core.userdetails.User userConnected = (org.springframework.security.core.userdetails.User) auth.getPrincipal();
		User user = userRepository.findByUsername(userConnected.getUsername());

		Mecanico mecanico = mecanicoRepository.findById(id);
		mecanico.setNombre(mecanicoDto.getNombre());
		mecanico.setUser(user);

		mecanicoRepository.save(mecanico);

		return "redirect:/Mecanico?successPut";
	}

	//Operation DELETE

	@GetMapping("/Delete")
	public String operationDeleteMecanico(Model model){

		urlService.setUrl("/Mecanico/Delete");
		model.addAttribute("listAll", mecanicoRepository.findAll());

		return "/Mecanico/operationDelete/operationDeleteMecanico";
	}

	@PostMapping("/Delete/{id}")
	public String operationDelete(@PathVariable("id") long id){

		try {
			mecanicoRepository.deleteById(id);
		}catch (Exception ex){
			return "redirect:/Mecanico?failDelete";
		}

		return "redirect:/Mecanico?successDelete";
	}


}
package com.example.workshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.*;
import com.example.workshop.service.UrlService;
import com.example.workshop.model.Cliente;
import com.example.workshop.dto.ClienteDto;
import com.example.workshop.repository.ClienteRepository;
import com.example.workshop.model.Reparacion;
import com.example.workshop.dto.ReparacionDto;
import com.example.workshop.repository.ReparacionRepository;
import com.example.workshop.model.User;
import com.example.workshop.repository.UserRepository;


@Controller
@RequestMapping("/Cliente")
public class ClienteController {

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UrlService urlService;

	//Principal entity operations view

	@GetMapping
	public String principalCliente(){

		urlService.setUrl("/Cliente");

		return "/Cliente/principalCliente";
	}

	//Operation GET

	@GetMapping("/Get")
	public String operationGetCliente(Model model){

		urlService.setUrl("/Cliente/Get");
		model.addAttribute("listAll", clienteRepository.findAll());

		return "/Cliente/operationGet/operationGetCliente";
	}

	//Operation POST

	@GetMapping("/Post")
	public String operationPostCliente(Model model){

		urlService.setUrl("/Cliente/Post");
		model.addAttribute("object", new ClienteDto());

		return "/Cliente/operationPost/operationPostCliente";
	}

	@PostMapping("/Post")
	public String operationPost(ClienteDto clienteDto){

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		org.springframework.security.core.userdetails.User userConnected = (org.springframework.security.core.userdetails.User) auth.getPrincipal();
		User user = userRepository.findByUsername(userConnected.getUsername());

		Cliente cliente = new Cliente();
		cliente.setUser(user);

		clienteRepository.save(cliente);

		return "redirect:/Cliente?successPost";
	}

	//Operation PUT

	@GetMapping("/Put")
	public String operationPutCliente(Model model){

		urlService.setUrl("/Cliente/Put");
		model.addAttribute("listAll", clienteRepository.findAll());

		return "/Cliente/operationPut/operationPutClienteView";
	}

	@GetMapping("/Put/{id}")
	public String operationPutCliente(@PathVariable("id") long id, Model model){

		Cliente cliente = clienteRepository.findById(id);
		ClienteDto clienteDto = new ClienteDto();

		clienteDto.setId_cliente(cliente.getId_cliente());

		model.addAttribute("object", clienteDto);

		return "/Cliente/operationPut/operationPutCliente";
	}

	@PostMapping("/Put/{id}")
	public String operationPut(@PathVariable("id") long id, ClienteDto clienteDto){

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		org.springframework.security.core.userdetails.User userConnected = (org.springframework.security.core.userdetails.User) auth.getPrincipal();
		User user = userRepository.findByUsername(userConnected.getUsername());

		Cliente cliente = clienteRepository.findById(id);
		cliente.setUser(user);

		clienteRepository.save(cliente);

		return "redirect:/Cliente?successPut";
	}

	//Operation DELETE

	@GetMapping("/Delete")
	public String operationDeleteCliente(Model model){

		urlService.setUrl("/Cliente/Delete");
		model.addAttribute("listAll", clienteRepository.findAll());

		return "/Cliente/operationDelete/operationDeleteCliente";
	}

	@PostMapping("/Delete/{id}")
	public String operationDelete(@PathVariable("id") long id){

		try {
			clienteRepository.deleteById(id);
		}catch (Exception ex){
			return "redirect:/Cliente?failDelete";
		}

		return "redirect:/Cliente?successDelete";
	}


}
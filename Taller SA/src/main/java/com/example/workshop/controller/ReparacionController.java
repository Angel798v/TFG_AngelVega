package com.example.workshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.*;
import com.example.workshop.service.UrlService;
import com.example.workshop.model.Reparacion;
import com.example.workshop.dto.ReparacionDto;
import com.example.workshop.repository.ReparacionRepository;
import com.example.workshop.model.Mecanico;
import com.example.workshop.dto.MecanicoDto;
import com.example.workshop.repository.MecanicoRepository;
import com.example.workshop.model.Cliente;
import com.example.workshop.dto.ClienteDto;
import com.example.workshop.repository.ClienteRepository;
import com.example.workshop.model.Tipo_servicio;
import com.example.workshop.dto.Tipo_servicioDto;
import com.example.workshop.repository.Tipo_servicioRepository;
import com.example.workshop.model.User;
import com.example.workshop.repository.UserRepository;


@Controller
@RequestMapping("/Reparacion")
public class ReparacionController {

	@Autowired
	private ReparacionRepository reparacionRepository;

	@Autowired
	private MecanicoRepository mecanicoRepository;

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private Tipo_servicioRepository tipo_servicioRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UrlService urlService;

	//Principal entity operations view

	@GetMapping
	public String principalReparacion(){

		urlService.setUrl("/Reparacion");

		return "/Reparacion/principalReparacion";
	}

	//Operation GET

	@GetMapping("/Get")
	public String operationGetReparacion(Model model){

		urlService.setUrl("/Reparacion/Get");
		model.addAttribute("listAll", reparacionRepository.findAll());

		return "/Reparacion/operationGet/operationGetReparacion";
	}

	//Operation POST

	@GetMapping("/Post")
	public String operationPostReparacion(Model model){

		urlService.setUrl("/Reparacion/Post");
		model.addAttribute("object", new ReparacionDto());
		model.addAttribute("listMecanico", mecanicoRepository.findAll());
		model.addAttribute("listCliente", clienteRepository.findAll());
		model.addAttribute("listTipo_servicio", tipo_servicioRepository.findAll());

		return "/Reparacion/operationPost/operationPostReparacion";
	}

	@PostMapping("/Post")
	public String operationPost(ReparacionDto reparacionDto){

		//Relation - ManyToOne with Mecanico
		Mecanico mecanico = mecanicoRepository.findById(reparacionDto.getMecanicoDto().getId_mecanico());

		//Relation - ManyToOne with Cliente
		Cliente cliente = clienteRepository.findById(reparacionDto.getClienteDto().getId_cliente());

		//Relation - ManyToMany with Tipo_servicio
		Set<Tipo_servicio> tipo_servicios = new HashSet<Tipo_servicio>();
		for(String tipo_servicioDtoId : reparacionDto.getTipo_servicioDto()){
			tipo_servicios.add(tipo_servicioRepository.findById(Long.parseLong(tipo_servicioDtoId)));
		}

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		org.springframework.security.core.userdetails.User userConnected = (org.springframework.security.core.userdetails.User) auth.getPrincipal();
		User user = userRepository.findByUsername(userConnected.getUsername());

		Reparacion reparacion = new Reparacion();
		reparacion.setMecanico(mecanico);
		reparacion.setCliente(cliente);
		for(Tipo_servicio tipo_servicio : tipo_servicios){
			reparacion.addTipo_servicio(tipo_servicio);
		}
		reparacion.setUser(user);

		reparacionRepository.save(reparacion);

		return "redirect:/Reparacion?successPost";
	}

	//Operation PUT

	@GetMapping("/Put")
	public String operationPutReparacion(Model model){

		urlService.setUrl("/Reparacion/Put");
		model.addAttribute("listAll", reparacionRepository.findAll());

		return "/Reparacion/operationPut/operationPutReparacionView";
	}

	@GetMapping("/Put/{id}")
	public String operationPutReparacion(@PathVariable("id") long id, Model model){

		Reparacion reparacion = reparacionRepository.findById(id);
		ReparacionDto reparacionDto = new ReparacionDto();

		reparacionDto.setId_reparacion(reparacion.getId_reparacion());

		MecanicoDto mecanicoDto = new MecanicoDto();
		mecanicoDto.setId_mecanico(reparacion.getMecanico().getId_mecanico());
		reparacionDto.setMecanicoDto(mecanicoDto);
		model.addAttribute("listMecanico", mecanicoRepository.findAll());

		ClienteDto clienteDto = new ClienteDto();
		clienteDto.setId_cliente(reparacion.getCliente().getId_cliente());
		reparacionDto.setClienteDto(clienteDto);
		model.addAttribute("listCliente", clienteRepository.findAll());

		String[] tipo_servicioDto = new String[]{};
		reparacionDto.setTipo_servicioDto(tipo_servicioDto);
		model.addAttribute("listTipo_servicio", tipo_servicioRepository.findAll());

		model.addAttribute("object", reparacionDto);

		return "/Reparacion/operationPut/operationPutReparacion";
	}

	@PostMapping("/Put/{id}")
	public String operationPut(@PathVariable("id") long id, ReparacionDto reparacionDto){

		//Relation - ManyToOne with Mecanico
		Mecanico mecanico = mecanicoRepository.findById(reparacionDto.getMecanicoDto().getId_mecanico());

		//Relation - ManyToOne with Cliente
		Cliente cliente = clienteRepository.findById(reparacionDto.getClienteDto().getId_cliente());

		//Relation - ManyToMany with Tipo_servicio
		Set<Tipo_servicio> tipo_servicios = new HashSet<Tipo_servicio>();
		for(String tipo_servicioDtoId : reparacionDto.getTipo_servicioDto()){
			tipo_servicios.add(tipo_servicioRepository.findById(Long.parseLong(tipo_servicioDtoId)));
		}

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		org.springframework.security.core.userdetails.User userConnected = (org.springframework.security.core.userdetails.User) auth.getPrincipal();
		User user = userRepository.findByUsername(userConnected.getUsername());

		Reparacion reparacion = reparacionRepository.findById(id);
		reparacion.setMecanico(mecanico);
		reparacion.setCliente(cliente);
		reparacion.setTipo_servicios(tipo_servicios);
		reparacion.setUser(user);

		reparacionRepository.save(reparacion);

		return "redirect:/Reparacion?successPut";
	}

	//Operation DELETE

	@GetMapping("/Delete")
	public String operationDeleteReparacion(Model model){

		urlService.setUrl("/Reparacion/Delete");
		model.addAttribute("listAll", reparacionRepository.findAll());

		return "/Reparacion/operationDelete/operationDeleteReparacion";
	}

	@PostMapping("/Delete/{id}")
	public String operationDelete(@PathVariable("id") long id){

		try {
			reparacionRepository.deleteById(id);
		}catch (Exception ex){
			return "redirect:/Reparacion?failDelete";
		}

		return "redirect:/Reparacion?successDelete";
	}


}
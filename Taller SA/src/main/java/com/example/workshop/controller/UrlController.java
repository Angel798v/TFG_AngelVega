package com.example.workshop.controller;

import com.example.workshop.service.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UrlController {

	@Autowired
	private UrlService urlService;

	@GetMapping("/redirectEs")
	public String redirectEs(){
		return "redirect:" + urlService.getUrlEs();
	}


	@GetMapping("/redirectEn")
	public String redirectEn(){
		return "redirect:" + urlService.getUrlEn();
	}


}
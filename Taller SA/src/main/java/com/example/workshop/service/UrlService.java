package com.example.workshop.service;

import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
@Getter
public class UrlService {

	private String urlEs;

	private String urlEn;


	public void setUrl(String url){

		this.urlEs = url + "?lang=es";
		this.urlEn = url + "?lang=en";
	}

}
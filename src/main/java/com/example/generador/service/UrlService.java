package com.example.generador.service;

import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
@Getter
public class UrlService {

    private String urlEs;

    private String urlEn;


    /**
     * Establece la url con el idioma en castellano
     * @param url Url
     */
    public void setUrl(String url){

        this.urlEs = url + "?lang=es";
        this.urlEn = url + "?lang=en";
    }


}

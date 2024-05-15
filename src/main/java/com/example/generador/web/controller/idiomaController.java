package com.example.generador.web.controller;

import com.example.generador.dto.EntidadDto;
import com.example.generador.dto.Idioma;
import com.example.generador.service.EntidadesService;
import com.example.generador.service.IdiomaService;
import com.example.generador.service.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class idiomaController {

    @Autowired
    private IdiomaService idiomaService;

    @Autowired
    private UrlService urlService;

    @Autowired
    private EntidadesService entidadesService;


    /**
     * Vista que muestra el numero de idiomas de literales que hay registrados para el proyecto
     * @param model Añade la lista con el numero de idiomas de literales
     * @return Vista
     */
    @GetMapping("/idiomas")
    public String vistaIdiomas(Model model){

        urlService.setUrl("/idiomas");

        model.addAttribute("idiomas",idiomaService.getIdiomas());
        model.addAttribute("idiomaDefault", idiomaService.getIdiomaDefault());


        return "/Generador/Idiomas/Idiomas";
    }


    /**
     * Vista para la creación de un idioma, para el proyecto a generar
     * @param model Model
     * @return Vista
     */
    @GetMapping("/creacionIdioma")
    public String vistaCreacionIdioma(Model model){

        urlService.setUrl("/creacionIdioma");

        List<EntidadDto> entidades = entidadesService.getEntidades();


        model.addAttribute("entidades",entidades);
        model.addAttribute("idioma",new Idioma());


        return "/Generador/Idiomas/creacionIdioma";
    }

    /**
     * Método POST que obtiene el objeto del idioma a introducir en la aplicación.
     * Comprueba si no esta repetido y si no lo está, lo añade
     * @param idioma Idioma a añadir
     * @return Vista idiomas
     */
    @PostMapping("/creacionIdioma")
    public String creacionIdioma(Idioma idioma){

        if(idioma.getAbreviatura() == null){
            return "redirect:/idiomas?falloAbreviatura";
        }

        boolean exito = idiomaService.addIdioma(idioma);


        if(exito) {
            return "redirect:/idiomas?exito";
        }else{
            return "redirect:/idiomas?fallo";
        }
    }


    /**
     * Vista para establecer un idioma por defecto de entre los idiomas registrados.
     * @param idioma Idioma
     * @param model Model
     * @return Vista
     */
    @GetMapping("/setIdiomaDefault")
    public String vistaIdiomaDefault(@ModelAttribute("idioma") Idioma idioma, Model model){

        urlService.setUrl("/setIdiomaDefault");

        model.addAttribute("idiomas",idiomaService.getIdiomas());

        return "/Generador/Idiomas/setIdiomaDefault";
    }


    /**
     * Método POST que registra el idioma por defecto.
     * @param idioma Idioma
     * @return Vista idiomas
     */
    @PostMapping("/setIdiomaDefault")
    public String setIdiomaDefault(Idioma idioma){

        idioma = idiomaService.findByName(idioma.getNombreIdioma());

        idiomaService.setIdiomaDefault(idioma);

        return "redirect:/idiomas?exitoIdiomaDefault";
    }


    /**
     * Elimina los idiomas registrados.
     * @return Vista idiomas
     */
    @GetMapping("/clearIdiomas")
    public String clearIdiomas(){

        idiomaService.clearIdiomas();
        idiomaService.setIdiomaDefault(null);

        return "redirect:/idiomas?clearIdiomas";
    }


}

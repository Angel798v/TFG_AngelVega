package com.example.generador.web.controller;

import com.example.generador.service.DesignService;
import com.example.generador.service.UrlService;
import com.example.generador.util.ColorPicker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.awt.*;

@Controller
public class DesignController {

    @Autowired
    private DesignService designService;

    @Autowired
    private UrlService urlService;


    /**
     * Vista con el apartado del diseño de la aplicación a generar
     * @param model Model
     * @return Vista
     */
    @GetMapping("/Design")
    public String vistaDesign(Model model){

        urlService.setUrl("/Design");

        if(designService.getColores() == null){
            designService.setColores(new ColorPicker("#8ED8F1","#FFFFFF"));
            model.addAttribute("colores",designService.getColores());
        }else{
            model.addAttribute("colores",designService.getColores());
        }

        model.addAttribute("nav", designService.isNav());

        return "/Generador/Diseño/Design";
    }


    /**
     * Vista que permite seleccionar los colores principales de la aplicación a generar.
     * @param colorPicker Objeto ColorPicker
     * @return Vista
     */
    @GetMapping("/colorPicker")
    public String vistaColorPicker(@ModelAttribute("colorPicker")ColorPicker colorPicker){

        urlService.setUrl("/colorPicker");

        return "Generador/Diseño/ColorPicker";
    }


    /**
     * Método que establece los colores principales de la aplicación a generar.
     * @param colorPicker Objeto ColorPicker
     * @return Vista design
     */
    @PostMapping("/colorPicker")
    public String colorPicker(ColorPicker colorPicker){

        designService.setColores(colorPicker);

        return "redirect:/Design";
    }

    /**
     * Método que modifica el valor del atributo nav.
     * @return Vista design
     */
    @PostMapping("/switchNav")
    public String switchNav(){

        designService.switchNav();

        return "redirect:/Design";
    }

}

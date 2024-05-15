package com.example.generador.web.controller;

import com.example.generador.dto.AtributoDto;
import com.example.generador.dto.EntidadDto;
import com.example.generador.service.AtributosService;
import com.example.generador.service.EntidadesService;
import com.example.generador.service.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AtributosController {

    @Autowired
    private EntidadesService entidadesService;

    @Autowired
    private AtributosService atributosService;

    @Autowired
    private UrlService urlService;

    private EntidadDto entidad;


    /**
     * Vista que muestra los atributos de una entidad
     * @param model Model
     * @return Vista
     */
    @GetMapping("/mostrarAtributos")
    public String mostrarAtributos(Model model) {

        urlService.setUrl("/mostrarAtributos");

        model.addAttribute("entidad",entidad);

        return "/Generador/Atributos/MuestraAtributos";
    }



    /**
     * Captura el nombre de la entidad sobre la que se desea mostrar los atributos
     * Una vez capturada la entidad, redirige a la vista que muestra los atributos
     * Se le pasa como parametro el nombre de la Entidad referencia
     * @param nombre String
     * @return Vista
     */
    @GetMapping("/mostrarAtributos/{nombre}")
    public String mostrarAtributosNombre(@PathVariable String nombre) {

        if(entidadesService.findByNombre(nombre) != null) {
            entidad = entidadesService.findByNombre(nombre);
        }

        return "redirect:/mostrarAtributos";
    }


    /**
     * Vista para añadir un atributo a una entidad
     * @param atributo AtributoDto
     * @return Vista
     */
    @GetMapping("/addAtributo")
    public String vistaAddAtributo(@ModelAttribute("atributo") AtributoDto atributo) {

        urlService.setUrl("/addAtributo");

        return "/Generador/Atributos/CreacionAtributo";
    }


    /**
     * Añade un atributo a una entidad
     * @param atributo AtributoDto
     * @return
     */
    @PostMapping("/addAtributo")
    public String addAtributo(@ModelAttribute("atributo") AtributoDto atributo) {

        String letraInicial, resto, nombreCompleto = null;

        nombreCompleto = atributo.getNombre().replace(" ","");

        letraInicial = nombreCompleto.substring(0,1).toLowerCase();
        resto = nombreCompleto.substring(1);
        nombreCompleto = letraInicial + resto;

        atributo.setNombre(nombreCompleto);

        boolean exito = atributosService.addAtributo(entidad,atributo);

        if(exito){
            return "redirect:/mostrarAtributos?exito";
        }else{
            return "redirect:/mostrarAtributos?fallo";
        }


    }

    /**
     * Elimina los atributos de una entidad
     * @return vista mostrarAtributos
     */
    @GetMapping("/clearAtributos")
    public String clearAtributos(){

        atributosService.clearAtributos(entidad);
        AtributoDto clavePrimaria = new AtributoDto("id_" + entidad.getNombre().toLowerCase(),"long",
                true, false,false,false,"",false,"",0);
        atributosService.addAtributo(entidad,clavePrimaria);

        return "redirect:/mostrarAtributos?clear";
    }

}



package com.example.generador.web.controller;

import com.example.generador.dto.EntidadDto;
import com.example.generador.dto.Relacion;
import com.example.generador.service.EntidadesService;
import com.example.generador.service.RelacionesService;
import com.example.generador.service.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class RelacionController {

    @Autowired
    private EntidadesService entidadesService;

    @Autowired
    private RelacionesService relacionesService;

    @Autowired
    private UrlService urlService;


    /**
     * Colección que contiene las entidades
     * @return Colección
     */
    @ModelAttribute("entidades")
    public List<EntidadDto> entidades(){
        return entidadesService.getEntidades();
    }

    /**
     * Colección que contiene las relaciones
     * @return Colección
     */
    @ModelAttribute("relaciones")
    public List<Relacion> relaciones(){
        return relacionesService.getRelaciones();
    }

    /**
     * Vista que muestra la creacion de una relación
     * @param model Model
     * @return Vista
     */
    @GetMapping("/creacionRelacion")
    public String vistaCreacionRelacion(Model model){

        urlService.setUrl("/creacionRelacion");

        Relacion relacion = new Relacion();

        model.addAttribute("relacion",relacion);

        return "Generador/Relacion/CreacionRelacion";
    }


    /**
     * Operacion de tipo POST que crea una relacion.
     * @param relacion Relacion a crear
     * @return Vista Generacion
     */
    @PostMapping("/creacionRelacion")
    public String creacionRelacion(Relacion relacion){

        if(relacion.getNameA() == null || relacion.getNameB() == null || relacion.getCardinalityA() == null || relacion.getCardinalityB() == null){
            return "redirect:/Generacion?errorRelacion";
        }

        EntidadDto A = entidadesService.findByNombre(relacion.getNameA());
        EntidadDto B = entidadesService.findByNombre(relacion.getNameB());

        if(A != B) {
            relacion.setA(A);
            relacion.setB(B);
        }else{
            return "redirect:/Generacion?errorRelacion";
        }

        boolean exito = relacionesService.addRelacion(relacion);

        if(exito) {
            return "redirect:/Generacion?exitoRelacion";
        }else{
            return "redirect:/Generacion?existeRelacion";
        }
    }


    @GetMapping("/clearRelaciones")
    public String clearRelaciones(){

        relacionesService.clearRelaciones();

        return "redirect:/Generacion";
    }

}

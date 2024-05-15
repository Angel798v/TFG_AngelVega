package com.example.generador.web.controller;

import com.example.generador.dto.AtributoDto;
import com.example.generador.dto.EntidadDto;
import com.example.generador.dto.RoleDto;
import com.example.generador.model.Role;
import com.example.generador.service.AtributosService;
import com.example.generador.service.EntidadesService;
import com.example.generador.service.RolesService;
import com.example.generador.service.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class EntidadesController {

    @Autowired
    private EntidadesService entidadesService;

    @Autowired
    private UrlService urlService;

    @Autowired
    private RolesService rolesService;

    @Autowired
    private AtributosService atributosService;

    /**
     * Vista para la creacion de una entidad
     * @return Vista
     */
    @GetMapping("/AddEntidad")
    public String vistaAddEntidad(){

        urlService.setUrl("/AddEntidad");

        return "/Generador/CreacionEntidad";
    }

    /**
     * Objeto entidad que se le pasa al formulario
     * @return entidad
     */
    @ModelAttribute("entidad")
    public EntidadDto entidadDto(){
        return new EntidadDto();
    }



    /**
     * Método POST que recoge los datos para registrar la nueva entidad para la aplicación a generar
     * @param entidad Entidad a generar
     * @return Vista generación de la aplicación
     */
    @PostMapping("/AddEntidad")
    public String addEntidad(@ModelAttribute("entidad") EntidadDto entidad){

        String letraInicial, resto, nombreCompleto = null;

        nombreCompleto = entidad.getNombre().replace(" ","");

        letraInicial = nombreCompleto.substring(0,1).toUpperCase();
        resto = nombreCompleto.substring(1);
        nombreCompleto = letraInicial + resto;

        entidad.setNombre(nombreCompleto);

        AtributoDto clavePrimaria = new AtributoDto("id_" + entidad.getNombre().toLowerCase(),"long",
                true, false,false,false,"",false,"",0);
        atributosService.addAtributo(entidad,clavePrimaria);


        RoleDto admin = rolesService.findByRoleName("ADMIN");
        entidad.getPermisosRoles().addRole(admin,"get");
        entidad.getPermisosRoles().addRole(admin,"post");
        entidad.getPermisosRoles().addRole(admin,"put");
        entidad.getPermisosRoles().addRole(admin,"delete");

        boolean exito = entidadesService.addEntidad(entidad);

        if(exito) {
            return "redirect:/Generacion?exito";
        }else{
            return "redirect:/Generacion?fallo";
        }

    }


    /**
     * Borra todas las entidades de la lista de entidades a generar para la aplicación
     * @return Vista Generacion
     */
    @GetMapping("/ClearEntidades")
    public String clearEntidades(){

        entidadesService.clearEntidades();

        return "redirect:/Generacion?clear";
    }


    /**
     * Cambia el valor del booleano vistaCompacta.
     * Dicho booleano indica en la aplicación a generar si el modelo de entidades dispone de una vista con iconos grandes,
     * o una vista más compacta.
     * @return vista Generacion
     */
    @PostMapping("/switchVistaCompacta")
    public String switchVistaCompacta(){

        entidadesService.switchVistaCompacta();

        return "redirect:/Generacion";
    }

}

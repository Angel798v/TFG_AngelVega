package com.example.generador.web.controller;

import com.example.generador.dto.RoleDto;
import com.example.generador.service.UrlService;
import com.example.generador.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;


@Controller
public class HomeController {

    @Autowired
    private UsuarioService usuarioService;


    /**
     * Booleano que indica si se ha realizado la comprobación de si hay un administrador
     */
    private boolean adminComprobacion = false;


    @Autowired
    private UrlService urlService;


    /**
     * Vista principal al arrancar la aplicación
     * @param model Model
     * @return Vista
     */
    @GetMapping("/")
    public String vistaPrincipal(Model model){

        urlService.setUrl("/index");

        boolean adminRegistrado = false;

        if(!adminComprobacion) {
            adminRegistrado = usuarioService.registryAdmin();
            adminComprobacion = true;
            //Para el generador
            //List<RoleDto> roles = new ArrayList<RoleDto>();
            //RoleDto nombre = new RoleDto(0,"");
            //roles.add(nombre);
            //usuarioService.createRoles(roles);
        }

        if(adminRegistrado){
            System.out.println("Usuario administrador registrado en la BBDD");
            adminRegistrado = false;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth.getPrincipal() == "anonymousUser"){
            model.addAttribute("userConnected", false);
        }else{
            model.addAttribute("userConnected", true);
        }

        return "index";
    }

    /**
     * Vista principal del index
     * @param model Model
     * @return Vista
     */
    @GetMapping("/index")
    public String vistaIndex(Model model){

        urlService.setUrl("/index");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if(auth.getPrincipal() == "anonymousUser"){
            model.addAttribute("userConnected", false);
        }else{
            model.addAttribute("userConnected", true);
        }

        return "index";
    }

    /**
     * Redirige a la vista de login o logout dependiendo si hay usuario conectado o no.
     * @return Vista login o logout
     */
    @GetMapping("/loginlogout")
    public String loginlogout(){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth.getPrincipal() == "anonymousUser"){
            return "redirect:/login";
        }else{
            return "redirect:/logout";
        }
    }

    @GetMapping("/about")
    public String vistaAbout(){

        return "about";
    }

    @GetMapping("/contact")
    public String vistaContact(){

        return "contact";
    }

    @GetMapping("/layout21")
    public String layout21(){
        return "layout21";
    }

    @GetMapping("/layout22")
    public String layout22(){
        return "layout22";
    }

    @GetMapping("/error2")
    public String errorPrueba(){
        return "error2";
    }

    @GetMapping("/index2")
    public String vistaIndex2(){
        return "index2";
    }






}

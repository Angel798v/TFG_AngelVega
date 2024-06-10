package com.example.generador.web.controller;

import com.example.generador.dto.AtributoDto;
import com.example.generador.service.UrlService;
import com.example.generador.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class UsuariosController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UrlService urlService;



    /**
     * Login
     * Vista para el inicio de sesión
     * @return Vista
     */
    @GetMapping("/login")
    public String vistaLogin(){

        urlService.setUrl("/login");

        return "/Usuarios/login";
    }


    /**
     * Vista de los usuarios registrados en el sistema.
     * Vista habilitada solo para usuarios con el rol de admin
     * @param model Objeto model
     * @return Vista
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/usuarios")
    public String vistaUsuariosAdmin(Model model){

        urlService.setUrl("/usuarios");

        model.addAttribute("usuarios",usuarioService.listarUsuarios());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User usuarioConectado = (User) auth.getPrincipal();

        model.addAttribute("usuarioConectado", usuarioConectado);

        return "/Usuarios/admin/usuarios";
    }


    /**
     * Vista que muestra los atributos de la entidad Usuario
     * @param model Model
     * @return Vista
     */
    @GetMapping("/atributosUsuario")
    public String vistaAtributosUsuario(Model model){

        urlService.setUrl("/atributosUsuario");

        model.addAttribute("atributosUsuario", usuarioService.getAtributosUsuario());

        return "Usuarios/MuestraAtributosUsuario";
    }


    /**
     * Vista para añadir un atributo para la entidad Usuario
     * @param atributo Atributo
     * @return Vista
     */
    @GetMapping("/addAtributoUsuario")
    public String vistaCreacionAtributosUsuario(@ModelAttribute("atributo")AtributoDto atributo){

        urlService.setUrl("/addAtributoUsuario");

        return "Usuarios/CreacionAtributoUsuario";
    }

    /**
     * Método POST que añade un atributo a la entidad Usuario
     * @param atributo Atributo
     * @return Vista atributos usuario
     */
    @PostMapping("/addAtributoUsuario")
    public String creacionAtributosUsuario(AtributoDto atributo){

        String letraInicial, resto, nombreCompleto;

        nombreCompleto = atributo.getNombre().replace(" ","");

        letraInicial = nombreCompleto.substring(0,1).toUpperCase();
        resto = nombreCompleto.substring(1);
        nombreCompleto = letraInicial + resto;

        atributo.setNombre(nombreCompleto);

        if(usuarioService.addAtributosUsuario(atributo)){
            usuarioService.setFlagAdminUser(false);
            usuarioService.getAdmin().clearAdmin();
            return "redirect:/atributosUsuario?exito";
        }else{
            return "redirect:/atributosUsuario?fallo";
        }
    }

    /**
     * Elimina los atributos de la entidad Usuario
     * @return Vista atributos usuario
     */
    @GetMapping("/clearAtributosUsuario")
    public String clearAtributosUsuario(){

        usuarioService.clearAtributosUsuario();
        usuarioService.setFlagAdminUser(false);
        usuarioService.getAdmin().clearAdmin();

        return "redirect:/atributosUsuario?clear";
    }


}

package com.example.generador.web.controller;

import com.example.generador.dto.UsuarioDtoPsw;
import com.example.generador.model.Usuario;
import com.example.generador.repository.RoleRepository;
import com.example.generador.repository.UsuarioRepository;
import com.example.generador.service.UrlService;
import com.example.generador.service.UsuarioService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegistroUsuariosController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UrlService urlService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;


    /**
     * Clase DTO de datos de un usuario con contraseña
     * @return objeto dto
     */
    @ModelAttribute("usuario")
    public UsuarioDtoPsw nuevoUsuarioDtoPsw(){

        return new UsuarioDtoPsw();
    }


    /**
     * Vista del registro de usuarios
     * @return Vista
     */
    @GetMapping("/registro")
    public String mostrarRegistro(){

        urlService.setUrl("/registro");

        return "/Usuarios/registro";
    }

    /**
     * Registra un usuario en la base de datos
     * @param usuarioDtoPsw Dto del usuario a registrar
     * @return Vista con mensaje de exito
     */
    @PostMapping("/registro")
    public String registrarUsuario(@ModelAttribute("usuario") UsuarioDtoPsw usuarioDtoPsw){

        if(usuarioRepository.findByNombreUsuario(usuarioDtoPsw.getNombreUsuario()) != null ||
                usuarioRepository.findByEmail(usuarioDtoPsw.getEmail()) != null){
            return "redirect:/registro?error";
        }

        usuarioService.guardar(usuarioDtoPsw);

        return "redirect:/login?exito";
    }


    /**
     * Vista que permite al usuario administrador registrar usuarios con cualquier tipo de rol
     * @param model Model
     * @return Vista
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/usuarios/registry")
    public String userRegistryAdmin(Model model){

        urlService.setUrl("/usuarios/registry");
        model.addAttribute("roles", roleRepository.findAll());

        return "/Usuarios/admin/registryAdmin";
    }


    /**
     * Registra un usuario
     * @param usuarioDtoPsw UsuarioDto
     * @return
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/usuarios/registry")
    public String userRegistryAdmin(UsuarioDtoPsw usuarioDtoPsw){

        if(usuarioRepository.findByNombreUsuario(usuarioDtoPsw.getNombreUsuario()) != null ||
            usuarioRepository.findByEmail(usuarioDtoPsw.getEmail()) != null){
            return "redirect:/usuarios?fallo";
        }

        Usuario usuario = new Usuario(usuarioDtoPsw.getNombreUsuario(),
                encoder.encode(usuarioDtoPsw.getPassword()),
                usuarioDtoPsw.getEmail(),
                roleRepository.findByRoleName(usuarioDtoPsw.getRole()));

        usuarioRepository.save(usuario);

        return "redirect:/usuarios?exito";
    }

    /**
     * Elimina el usuario correspondiente al id que recibe como argumento
     * @param id Id de usuario
     * @return vista Usuarios
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/usuarios/delete/{id}")
    public String userDeleteAdmin(@PathVariable(name = "id") long id){

        usuarioRepository.deleteById(id);

        return "redirect:/usuarios?exitoDelete";
    }

}


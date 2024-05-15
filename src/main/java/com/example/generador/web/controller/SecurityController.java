package com.example.generador.web.controller;


import com.example.generador.dto.AtributoDto;
import com.example.generador.dto.EntidadDto;
import com.example.generador.dto.RoleDto;
import com.example.generador.service.EntidadesService;
import com.example.generador.service.RolesService;
import com.example.generador.service.UrlService;
import com.example.generador.service.UsuarioService;
import com.example.generador.util.RoleOperation;
import com.example.generador.util.UsuarioAdminCredentials;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@Controller
@Getter
@Setter
public class SecurityController {


    private boolean flagAdminUser = false;

    @Autowired
    private RolesService rolesService;


    @Autowired
    private UrlService urlService;


    @Autowired
    private EntidadesService entidadesService;

    @Autowired
    private UsuarioService usuarioService;



    @GetMapping("/SecurityProperties")
    public String vistaSecurityProperties(Model model){

        urlService.setUrl("/SecurityProperties");

        model.addAttribute("adminUser", usuarioService.getAdmin());
        model.addAttribute("isAdminUser",isFlagAdminUser());
        model.addAttribute("roles",rolesService.getRoles());
        model.addAttribute("entidades",entidadesService.getEntidades());
        model.addAttribute("defaultRole", rolesService.getDefaultRole());
        model.addAttribute("registrarUsuarios", rolesService.isRegistrarUsuarios());
        model.addAttribute("changeDefaultRole", new RoleDto());

        return "/Security/securityProperties";
    }


    /**
     * Vista que permite registrar un usuario administrador al proyecto a generar.
     * @param model model
     * @return Vista
     */
    @GetMapping("/registrarAdmin")
    public String vistaRegistrarAdmin(Model model){

        if(usuarioService.getAdmin() != null){
            model.addAttribute("usuarioAdmin",usuarioService.getAdmin());
        }else{
            model.addAttribute("usuarioAdmin",new UsuarioAdminCredentials());
        }

        urlService.setUrl("/registrarAdmin");

        return "/Security/createAdmin";
    }


    /**
     * Obtiene los datos del usuario administrador a registrar.
     * @param usuarioAdmin Datos
     * @return Vista SecurityProperties
     */
    @PostMapping("/registrarAdmin")
    public String registrarAdmin(UsuarioAdminCredentials usuarioAdmin){

        usuarioAdmin.setAtributos(usuarioService.getAdmin().getAtributos());

        setFlagAdminUser(true);
        usuarioService.setAdmin(usuarioAdmin);

        return "redirect:/SecurityProperties?exitoAdmin";
    }


    /**
     * Elimina el usuario administrador registrado
     * @return Vista SecurityProperties
     */
    @GetMapping("/eliminarUserAdmin")
    public String removeUserAdmin(){

        setFlagAdminUser(false);
        usuarioService.setAdmin(null);

        return "redirect:/SecurityProperties?removedAdmin";
    }


    /**
     * Vista para añadir un Rol
     * @return Vista
     */
    @GetMapping("/addRole")
    public String vistaAddRole(@ModelAttribute("rol")RoleDto roleDto){

        urlService.setUrl("/addRole");

        return "/Security/addRol";
    }


    /**
     * Método POST para añadir un rol
     * @param roleDto Objeto Rol
     * @return Vista SecurityProperties
     */
    @PostMapping("/addRole")
    public String addRole(RoleDto roleDto){

        roleDto.setRoleName(roleDto.getRoleName().toUpperCase());

        boolean exito = rolesService.addRole(roleDto);

        if(exito) {
            return "redirect:/SecurityProperties?exitoRol";
        }else{
            return "redirect:/SecurityProperties?errorRol";
        }
    }


    /**
     * Elimina los roles
     * @return Vista SecurityProperties
     */
    @GetMapping("/clearRoles")
    public String clearRoles(){

        rolesService.clearRoles();
        rolesService.addRole(new RoleDto(1,"ADMIN"));

        for(EntidadDto entidad : entidadesService.getEntidades()){
            entidad.getPermisosRoles().setOperationGET(new ArrayList<RoleDto>());
            entidad.getPermisosRoles().addRole(rolesService.findByRoleName("ADMIN"),"get");
            entidad.getPermisosRoles().setOperationPOST(new ArrayList<RoleDto>());
            entidad.getPermisosRoles().addRole(rolesService.findByRoleName("ADMIN"),"post");
            entidad.getPermisosRoles().setOperationPUT(new ArrayList<RoleDto>());
            entidad.getPermisosRoles().addRole(rolesService.findByRoleName("ADMIN"),"put");
            entidad.getPermisosRoles().setOperationDELETE(new ArrayList<RoleDto>());
            entidad.getPermisosRoles().addRole(rolesService.findByRoleName("ADMIN"),"delete");
        }

        return "redirect:/SecurityProperties?removedRoles";
    }


    /**
     * Vista que muestra los permisos que tienen los roles para cada CRUD operation de una entidad
     * @param nombre Nombre de la entidad
     * @param model Model
     * @return Vista
     */
    @GetMapping("/roles/{nombre}")
    public String rolesEntidad(@PathVariable("nombre") String nombre, Model model){

        urlService.setUrl("/roles/" + nombre);

        model.addAttribute("entidad",entidadesService.findByNombre(nombre));

        return "/Security/rolesEntidad";
    }


    /**
     * Vista para darle permiso a un rol sobre una CRUD operation de una entidad
     * @param nombre Nombre de la entidad
     * @param roleOperation Objeto que contiene nombre del rol y tipo de operación
     * @param model Model
     * @return Vista
     */
    @GetMapping("/roles/{nombre}/addRole")
    public String vistaAddRoleEntidad(@PathVariable("nombre") String nombre, @ModelAttribute("roleOperation")RoleOperation roleOperation, Model model){

        urlService.setUrl("/roles/" + nombre + "/addRole");

        model.addAttribute("roles",rolesService.getRoles());
        model.addAttribute("entidad",entidadesService.findByNombre(nombre));

        return "/Security/addRolEntidad";
    }


    /**
     * Operación POST para darle permiso a un rol sobre una CRUD operation de una entidad
     * @param nombre Nombre de la entidad
     * @param roleOperation Objeto que contiene nombre del rol y tipo de operación
     * @return Vista /roles/nombreEntidad
     */
    @PostMapping("/roles/{nombre}/addRole")
    public String addRoleEntidad(@PathVariable("nombre") String nombre, RoleOperation roleOperation){

        EntidadDto entidad = entidadesService.findByNombre(nombre);

        boolean exito = entidad.getPermisosRoles().addRole(rolesService.findByRoleName(roleOperation.getRoleName()),roleOperation.getOperation());

        String url = "redirect:/roles/" + nombre;

        if(exito) {
            url += "?exitoAdd";
        }else{
            url += "?errorAdd";
        }

        return url;
    }


    /**
     * Vista para eliminar un permiso a un rol sobre una CRUD operation de una entidad
     * @param nombre Nombre de la entidad
     * @param roleOperation Objeto que contiene nombre del rol y tipo de operación
     * @param model Model
     * @return Vista
     */
    @GetMapping("/roles/{nombre}/deleteRole")
    public String vistaDeleteRoleEntidad(@PathVariable("nombre") String nombre, @ModelAttribute("roleOperation")RoleOperation roleOperation, Model model){

        urlService.setUrl("/roles/" + nombre + "/deleteRole");

        model.addAttribute("roles",rolesService.getRoles());
        model.addAttribute("entidad",entidadesService.findByNombre(nombre));

        return "/Security/deleteRolEntidad";
    }


    /**
     * Operación DELETE para eliminar el permiso a un rol sobre una CRUD operation de una entidad
     * @param nombre Nombre de la entidad
     * @param roleOperation Objeto que contiene nombre del rol y tipo de operación
     * @return Vista /roles/nombreEntidad
     */
    @PostMapping("/roles/{nombre}/deleteRole")
    public String deleteRoleEntidad(@PathVariable("nombre") String nombre, RoleOperation roleOperation){

        EntidadDto entidad = entidadesService.findByNombre(nombre);

        boolean exito = entidad.getPermisosRoles().deleteRole(rolesService.findByRoleName(roleOperation.getRoleName()),roleOperation.getOperation());

        String url = "redirect:/roles/" + nombre;

        if(exito) {
            url += "?exitoDelete";
        }else{
            url += "?errorDelete";
        }

        return url;
    }


    /**
     * Modifica el valor booleano que indica si la aplicación va a disponer de registro de usuarios
     * @return Vista SecurityProperties
     */
    @PostMapping("/SecurityProperties/switchRegistryValue")
    public String switchRegistryValue(){

        if(rolesService.isRegistrarUsuarios()) {
            rolesService.setRegistrarUsuarios(false);
            return "redirect:/SecurityProperties?switchFalse";
        }else{
            rolesService.setRegistrarUsuarios(true);
            return "redirect:/SecurityProperties?switchTrue";
        }
    }


    /**
     * Modifica el rol por defecto
     * @param role Rol
     * @return Vista SecurityProperties
     */
    @PostMapping("/SecurityProperties/changeDefaultRole")
    public String changeDefaultRole(RoleDto role){

        role = rolesService.findByRoleName(role.getRoleName());

        rolesService.setDefaultRole(role);

        return "redirect:/SecurityProperties?roleChanged";
    }



}

package com.example.generador.service;

import com.example.generador.dto.RoleDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Getter
@Setter
public class RolesService {


    /**
     * Lista con los roles que va a tener la aplicación a generar
     */
    private List<RoleDto> roles = new ArrayList<RoleDto>();


    /**
     * Rol que se usará por defecto en la aplicación a generar.
     */
    private RoleDto defaultRole;


    /**
     * Boolean que indica si se van a poder registrar usuarios en la aplicación a generar.
     */
    private boolean registrarUsuarios = true;


    /**
     * Añade un rol a la lista de roles si no está repetido
     * @param roleDto Objeto rol
     * @return True si se ha añadido, False en caso contrario
     */
    public boolean addRole(RoleDto roleDto){

        if(roleDto.getId() == 0){
            return false;
        }

        for(RoleDto rol : roles){
            if(rol.getRoleName().equals(roleDto.getRoleName())){
                return false;
            }
            if(rol.getId() == roleDto.getId()){
                return false;
            }
        }
        roles.add(roleDto);
        return true;
    }


    /**
     * Vacía la lista de roles.
     */
    public void clearRoles(){
        roles.clear();
    }


    /**
     * Busca el rol en la lista de roles cuyo nombre coincide con el nombre pasado por parámetro.
     * @param name Nombre
     * @return Rol que coindice, Null si no existe
     */
    public RoleDto findByRoleName(String name){

        for(RoleDto role : roles){
            if(role.getRoleName().equals(name)){
                return role;
            }
        }

        return null;
    }





}

package com.example.generador.util;

import com.example.generador.dto.RoleDto;
import com.example.generador.service.RolesService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class PermisosRoles {

    private List<RoleDto> operationGET = new ArrayList<RoleDto>();

    private List<RoleDto> operationPOST = new ArrayList<RoleDto>();

    private List<RoleDto> operationPUT = new ArrayList<RoleDto>();

    private List<RoleDto> operationDELETE = new ArrayList<RoleDto>();


    /**
     * Añade un rol a la lista de operaciones permitidas. Si la operacion introducida no es permitida, devuelve false.
     * @param role Rol
     * @param operation Tipo de operacion. Valores permitidos: GET, POST, PUT y DELETE.
     * @return True si se ha añadido, False en caso contrario.
     */
    public boolean addRole(RoleDto role, String operation){

        operation = operation.toUpperCase();

        switch (operation){
            case "GET":
                if(!operationGET.contains(role)) {
                    operationGET.add(role);
                    return true;
                }
                break;

            case "POST":
                if(!operationPOST.contains(role)) {
                    operationPOST.add(role);
                    return true;
                }
                break;

            case "PUT":
                if(!operationPUT.contains(role)) {
                    operationPUT.add(role);
                    return true;
                }
                break;

            case "DELETE":
                if(!operationDELETE.contains(role)) {
                    operationDELETE.add(role);
                    return true;
                }
                break;

            default:
                return false;
        }

        return false;
    }

    /**
     * Elimina un rol a la lista de operaciones permitidas. Si la operacion introducida no es permitida, devuelve false.
     * @param role Rol
     * @param operation Tipo de operacion. Valores permitidos: GET, POST, PUT y DELETE.
     * @return True si se ha eliminado, False en caso contrario.
     */
    public boolean deleteRole(RoleDto role, String operation){

        if(role.getRoleName().equals("ADMIN")){
            return false;
        }

        operation = operation.toUpperCase();

        switch (operation){
            case "GET":
                if(operationGET.contains(role)) {
                    operationGET.remove(role);
                    return true;
                }
                break;

            case "POST":
                if(operationPOST.contains(role)) {
                    operationPOST.remove(role);
                    return true;
                }
                break;

            case "PUT":
                if(operationPUT.contains(role)) {
                    operationPUT.remove(role);
                    return true;
                }
                break;

            case "DELETE":
                if(operationDELETE.contains(role)) {
                    operationDELETE.remove(role);
                    return true;
                }
                break;

            default:
                return false;
        }

        return false;

    }

}

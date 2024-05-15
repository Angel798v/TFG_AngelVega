package com.example.generador.dto;

import com.example.generador.util.PermisosRoles;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EntidadDto {

    /**
     * Nombre de la entidad
     */
    private String nombre;


    /**
     * Atributos de la entidad
     */
    private List<AtributoDto> atributos;


    /**
     * Roles que pueden acceder a cada operación CRUD de la entidad.
     */
    private PermisosRoles permisosRoles = new PermisosRoles();


}

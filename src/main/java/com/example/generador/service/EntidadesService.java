package com.example.generador.service;

import com.example.generador.dto.EntidadDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class EntidadesService {

    @Getter
    @Setter
    public boolean vistaCompacta = false;

    @Autowired
    private RelacionesService relacionesService;

    /**
     * Entidades de la aplicación que se va a generar
     */
    @Getter
    private List<EntidadDto> entidades = new ArrayList<EntidadDto>();


    /**
     * Añade una entidad a la lista de entidades de la aplicacion
     * @param entidad Entidad que se desea añadir a la generación de la aplicación
     * @return True si se ha añadido la entidad y False si ya existia y no se ha añadido
     */
    public boolean addEntidad(EntidadDto entidad){

        for(EntidadDto ent:entidades){
            if(Objects.equals(ent.getNombre(), entidad.getNombre())) {
                return false;
            }
        }

        return entidades.add(entidad);

    }

    /**
     * Borra la lista con las entidades que se van a generar
     */
    public void clearEntidades(){

        entidades.clear();
        relacionesService.getRelaciones().clear();
    }


    /**
     * Busca una entidad de la lista de entidades por nombre de Entidad
     * @param nombre Nombre de la entidad
     * @return Entidad
     */
    public EntidadDto findByNombre(String nombre){

        for(EntidadDto entidad:entidades){
            if(Objects.equals(entidad.getNombre(),nombre)){
                return entidad;
            }
        }

        return null;
    }


    /**
     * Cambia el valor del booleano vistaCompacta.
     * Dicho booleano indica en la aplicación a generar si el modelo de entidades dispone de una vista con iconos grandes,
     * o una vista más compacta.
     */
    public void switchVistaCompacta(){
        if(vistaCompacta){
            vistaCompacta = false;
        }else{
            vistaCompacta = true;
        }
    }
}

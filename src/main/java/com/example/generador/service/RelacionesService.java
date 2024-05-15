package com.example.generador.service;

import com.example.generador.dto.EntidadDto;
import com.example.generador.dto.Relacion;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RelacionesService {

    /**
     * Lista que contiene todas las relaciones entre las entidades del proyecto a generar
     */
    private List<Relacion> relaciones;

    /**
     * Añade una relacion a la lista de relaciones
     * @param relacion Relación
     * @return booleano .True si se ha añadido la relacion, y false en caso contrario
     */
    public boolean addRelacion(Relacion relacion){

        if(relaciones == null){
            relaciones = new ArrayList<Relacion>();
        }

        boolean exito = true;

        String nombreRelacionA = relacion.getA().getNombre();
        String nombreRelacionB = relacion.getB().getNombre();

        String nombreA = "";
        String nombreB = "";

        for(Relacion rel : relaciones){

            nombreA = rel.getA().getNombre();
            nombreB = rel.getB().getNombre();

            if(nombreA.equals(nombreRelacionA) && nombreB.equals(nombreRelacionB) ||
            nombreB.equals(nombreRelacionA) && nombreA.equals(nombreRelacionB) ||
            nombreRelacionA.isEmpty() || nombreRelacionB.isEmpty()){
                exito = false;
            }
        }

        if(exito){
            relaciones.add(relacion);
        }

        return exito;
    }


    /**
     * Devuelve el listado de las relaciones del proyecto
     * @return listado
     */
    public List<Relacion> getRelaciones(){

        if (relaciones == null){
            relaciones = new ArrayList<Relacion>();
        }
        return relaciones;
    }


    /**
     * Elimina todas las relaciones creadas.
     */
    public void clearRelaciones(){
        relaciones.clear();
    }

}

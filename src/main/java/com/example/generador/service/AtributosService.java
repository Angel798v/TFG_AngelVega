package com.example.generador.service;

import com.example.generador.dto.AtributoDto;
import com.example.generador.dto.EntidadDto;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AtributosService {

    /**
     * Añade un atributo a la lista de atributos de una Entidad
     * @param entidad Entidad
     * @param atributo Atributo
     * @return True si se ha añadido y False en caso contrario
     */
    public boolean addAtributo(EntidadDto entidad, AtributoDto atributo){

        List<AtributoDto> aux = null;

        if(entidad.getAtributos() == null){
            aux = new ArrayList<AtributoDto>();
        }

        if(aux == null) {
            for (AtributoDto atr : entidad.getAtributos()) {
                if (Objects.equals(atr.getNombre(), atributo.getNombre())) {
                    return false;
                }
            }
        }else{
            entidad.setAtributos(aux);
        }


        if(atributo != null) {
            return entidad.getAtributos().add(atributo);
        }else{
            return false;
        }
    }

    /**
     * Inicializa una lista vacía como la lista de atributos de una Entidad
     * @param entidad
     */
    public void clearAtributos(EntidadDto entidad){

        if(entidad.getAtributos() != null) {
            List<AtributoDto> aux = new ArrayList<AtributoDto>();
            entidad.setAtributos(aux);
        }
    }
}

package com.example.generador.service;

import com.example.generador.dto.Idioma;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Getter
@Service
public class IdiomaService {

    /**
     * Lista de objetos de tipo Idioma
     * Cada objeto, contiene los literales de un idioma
     */
    private List<Idioma> idiomas = new ArrayList<Idioma>();


    /**
     * Idioma que se usará por defecto en la aplicación a generar
     */
    @Setter
    private Idioma idiomaDefault;


    /**
     * Añade un idioma a los idiomas del proyecto a generar
     * @param idioma Idioma a añadir
     * @return True si se ha añadido, False en caso contrario
     */
    public boolean addIdioma(Idioma idioma){

        for(Idioma lan : idiomas){
            if(lan.getNombreIdioma().equals(idioma.getNombreIdioma()) || lan.getAbreviatura().equals(idioma.getAbreviatura())){
                return false;
            }
        }
        idiomas.add(idioma);
        return true;
    }


    /**
     * Borra la lista de idiomas.
     */
    public void clearIdiomas(){

        idiomas.clear();
    }


    /**
     * Devuelve el idioma cuyo nombre coincide con el nombre pasado como parámetro-
     * @param name Nombre
     * @return Idioma coincidente, False en caso de que no lo haya
     */
    public Idioma findByName(String name){

        for (Idioma idioma : idiomas){
            if(idioma.getNombreIdioma().equals(name)){
                return idioma;
            }
        }
        return null;
    }






}

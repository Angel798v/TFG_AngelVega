package com.example.jpa_formacion.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EtiquetaEmbeddableDto {

    private long id;


    private String name;

    //Faltan relaciones

    private String descripcion;


}
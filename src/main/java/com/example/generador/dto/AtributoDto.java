package com.example.generador.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AtributoDto {

    private String nombre;

    private String tipo;

    private boolean primaryKey;

    private boolean unique;

    private boolean nullable;

    private boolean check;
    private String checkValue;

    private boolean defaultValue;
    private String value;

    private int sizeText;
}

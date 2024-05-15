package com.example.generador.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Idioma {

    @Setter
    private String nombreIdioma;

    private String abreviatura;

    @Setter
    private String button_GET;

    @Setter
    private String button_POST;

    @Setter
    private String button_PUT;

    @Setter
    private String button_DELETE;

    @Setter
    private String inicio;

    @Setter
    private String servicios;

    @Setter
    private String paginaError;

    @Setter
    private String username;

    @Setter
    private String password;

    @Setter
    private String email;

    @Setter
    private String rol;

    @Setter
    private String usuarios;

    @Setter
    private String save;

    @Setter
    private String back;

    @Setter
    private String login;

    @Setter
    private String logout;

    @Setter
    private String signup;

    @Setter
    private String registrarUsuario;

    @Setter
    private String idioma;


    /**
     * Método set del atributo abreviatura. Solo modifica el valor si la longitud del nombre es igual a 2.
     * @param nombre Abreviatura
     */
    public void setAbreviatura(String nombre){

        String c1;
        String c2;

        if(nombre.length() == 2){
            c1 = nombre.substring(0,1);
            c2 = nombre.substring(1,2);
            if(c1.matches("[a-zA-Z]") && c2.matches("[a-zA-Z]")) {
                abreviatura = nombre;
            }
        }
    }


}

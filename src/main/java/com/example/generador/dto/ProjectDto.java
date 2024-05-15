package com.example.generador.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProjectDto {

    private int id;

    private String title;

    private UsuarioDto user;

    private String abr_title;

    private String dir;

    private String nameApplication;

    private String nameDB;

    private int numPuerto;


}

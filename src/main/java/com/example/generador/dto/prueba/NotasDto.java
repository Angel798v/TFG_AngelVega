package com.example.generador.dto.prueba;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotasDto {

    private long id;

    private String nombreAlumno;

    private float nota;

    private AlumnoDto alumnoDto;

    private RevisionDto revisionDto;

    private String[] profesorDto;

}

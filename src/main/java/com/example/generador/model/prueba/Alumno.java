package com.example.generador.model.prueba;


import jakarta.persistence.*;
import lombok.*;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "alumno")
public class Alumno {

    @Id
    @GeneratedValue
    private long id;

    @Column
    private String nombre;

    @Column
    private int curso;

    @Column
    private Timestamp active;


    @OneToMany(mappedBy = "alumno")
    private Set<Notas> notass;

    public boolean addNotass(Notas notas){
        if(notass == null){
            notass = new HashSet<Notas>();
        }

        if(notas != null && !notass.contains(notas)){
            if(notas.getAlumno() != null){
                notas.getAlumno().getNotass().remove(notas);
            }
            notas.setAlumno(this);
            return true;
        }

        return false;
    }

    @Override
    public String toString(){

        return id + "-" + nombre + "-" + curso;
    }


}

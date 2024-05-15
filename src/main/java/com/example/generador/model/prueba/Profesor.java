package com.example.generador.model.prueba;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "profesor")
public class Profesor implements Serializable {

    @Id
    @GeneratedValue
    private long id;

    private String nombre;

    @ManyToMany(mappedBy = "profesores")
    private Set<Notas> notas; //La clase se deberia llamar NOTA

    public void addNotass(Notas nota){
        if(notas == null){
            notas = new HashSet<Notas>();
        }

        if(nota != null && !notas.contains(nota)){
            if(!nota.getProfesores().contains(this)){
                notas.add(nota);
                nota.getProfesores().add(this);
            }
        }
    }

    @Override
    public String toString(){
        return id + "-" + nombre;
    }

}

package com.example.generador.model.prueba;


import com.example.generador.model.Usuario;
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
@Table(name = "notas")
public class Notas implements Serializable {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column
    private String nombreAlumno;

    @Column
    private float nota;


    @ManyToOne
    @JoinColumn(name = "id_alumno")
    private Alumno alumno;

    public void setAlumno(Alumno alumno){       //Bidireccional
        if(this.alumno != null){
            this.alumno.getNotass().remove(this);
        }
        this.alumno = alumno;
        if(alumno != null && !alumno.getNotass().contains(this)){
            alumno.getNotass().add(this);
        }
    }


    @OneToOne
    @JoinColumn(name = "id_revision")
    private Revision revision;

    public void setRevision(Revision revision){
        if(this.revision != null){
            this.revision.setNotas(null);
        }
        this.revision = revision;
        if(revision != null){
            revision.setNotas(this);
        }
    }


    @ManyToMany
    private Set<Profesor> profesores;

    public void addProfesor(Profesor profesor){
        if(profesores == null){
            profesores = new HashSet<Profesor>();
        }

        if(profesor != null && !profesores.contains(profesor)){
            if(!profesor.getNotas().contains(this)){   //Bidireccional
                profesor.getNotas().add(this);     //Bidireccional
            }
            profesores.add(profesor);       //Unidireccional
        }
    }


    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Override
    public String toString(){
        return id + "-" + nombreAlumno + "-" + nota;
    }




}

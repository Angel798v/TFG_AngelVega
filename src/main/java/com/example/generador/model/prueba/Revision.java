package com.example.generador.model.prueba;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "revision")
public class Revision implements Serializable {

    @Id
    @GeneratedValue
    private long id;

    @Column
    private String profesor;


    @OneToOne(mappedBy = "revision")
    private Notas notas;

    @Override
    public String toString(){
        return id + "-" + profesor;
    }

}

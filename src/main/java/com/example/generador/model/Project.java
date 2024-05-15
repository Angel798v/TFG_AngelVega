package com.example.generador.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "project")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "title", length = 30, nullable = false)
    private String title;


    @Column(name = "abr_title", length = 20, nullable = false)
    private String abbreviated_title;


    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario user;

}

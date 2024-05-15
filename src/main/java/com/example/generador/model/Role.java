package com.example.generador.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "role")
public class Role implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "role_name",nullable = false)
    private String roleName;


    @OneToMany(mappedBy = "role")
    private Set<Usuario> usuarios;

    /**
     * Constructor con nombre de rol como único parámetro
     * @param roleName Nombre del rol
     */
    public Role(String roleName) {
        this.roleName = roleName;
    }


    @Override
    public String toString(){

        return roleName;
    }


}

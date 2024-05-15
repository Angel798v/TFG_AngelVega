package com.example.generador.model;


import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "usuario")
public class Usuario implements Serializable {

    public Usuario(String nombreUsuario, String password, String email, Role role) {
        this.nombreUsuario = nombreUsuario;
        this.password = password;
        this.email = email;
        this.role = role;
    }


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "nombre_usuario", length = 30, nullable = false, unique = true)
    private String nombreUsuario;

    @Column(name = "password", length = 250)
    private String password;

    @Column(name = "email", length = 50, unique = true)
    private String email;


    //Relacion con la entidad Role
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idrole")
    private Role role;


    //Relacion con la entidad Project
    @OneToMany(mappedBy = "user")
    private List<Project> proyectos;


    @Override
    public String toString(){

        return id + " - " + nombreUsuario + " - " + role;
    }

}

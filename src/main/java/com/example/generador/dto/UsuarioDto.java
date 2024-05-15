package com.example.generador.dto;


import com.example.generador.model.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioDto {

    private long id;

    private String nombreUsuario;

    private String email;

    private Role role;

}

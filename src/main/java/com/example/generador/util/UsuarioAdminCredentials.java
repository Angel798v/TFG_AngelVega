package com.example.generador.util;

import com.example.generador.dto.AtributoDto;
import com.example.generador.service.UsuarioService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioAdminCredentials {

    private String email;

    private String username;

    private String password;

    private Set<AtributoDto> atributos = new HashSet<AtributoDto>();

    private String[] valores = new String[]{};


    public UsuarioAdminCredentials(Set<AtributoDto> atributos){
        this.atributos = atributos;
    }

    public void clearAdmin(){
        this.email = null;
        this.username = null;
        this.password = null;
        this.valores = null;
    }

}

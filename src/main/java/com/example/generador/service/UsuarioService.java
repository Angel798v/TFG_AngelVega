package com.example.generador.service;

import com.example.generador.dto.AtributoDto;
import com.example.generador.dto.RoleDto;
import com.example.generador.dto.UsuarioDtoPsw;
import com.example.generador.model.Usuario;
import com.example.generador.util.UsuarioAdminCredentials;
import org.springframework.security.core.userdetails.UserDetailsService;
import java.util.List;
import java.util.Set;

public interface UsuarioService extends UserDetailsService {

    public Usuario guardar(UsuarioDtoPsw usuarioDtoPsw);

    public List<Usuario> listarUsuarios();

    boolean registryAdmin();

    void registrarUsuariosPrueba();

    boolean addAtributosUsuario(AtributoDto atributo);

    void createRoles(List<RoleDto> roles);

    void setAdmin(UsuarioAdminCredentials admin);
    UsuarioAdminCredentials getAdmin();

    Set<AtributoDto> getAtributosUsuario();

    void clearAtributosUsuario();
}

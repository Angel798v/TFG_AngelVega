package com.example.generador.repository;

import com.example.generador.model.Role;
import com.example.generador.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario,Long> {

    void deleteById(long id);

    Usuario findByNombreUsuario(String username);

    Usuario findByEmail(String email);

    List<Usuario> findByRole(Role role);

}

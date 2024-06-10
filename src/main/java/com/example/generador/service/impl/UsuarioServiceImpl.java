package com.example.generador.service.impl;

import com.example.generador.dto.AtributoDto;
import com.example.generador.dto.RoleDto;
import com.example.generador.dto.UsuarioDtoPsw;
import com.example.generador.model.Role;
import com.example.generador.model.Usuario;
import com.example.generador.repository.RoleRepository;
import com.example.generador.repository.UsuarioRepository;
import com.example.generador.service.UsuarioService;
import com.example.generador.util.UsuarioAdminCredentials;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    public UsuarioAdminCredentials admin;

    @Setter
    public boolean flagAdminUser = false;

    private Set<AtributoDto> atributosUsuario = new HashSet<>();

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;


    /**
     * Guardar un usuario en la base de datos.
     * Se guarda con el rol de User
     * @param usuarioDtoPsw Objeto dto con los datos del usuario
     * @return Usuario guardado
     */
    @Override
    public Usuario guardar(UsuarioDtoPsw usuarioDtoPsw) {

        Role rol;

        if(roleRepository.findByRoleName("ROLE_USER") == null){
            rol = new Role("ROLE_USER");
        }else{
            rol = roleRepository.findByRoleName("ROLE_USER");
        }

        Usuario usuario = new Usuario(
                usuarioDtoPsw.getNombreUsuario(),
                encoder.encode(usuarioDtoPsw.getPassword()),
                usuarioDtoPsw.getEmail(),
                rol);


        return usuarioRepository.save(usuario);
    }


    /**
     * Lista todos los usuarios registrados
     * @return Lista de usuarios
     */
    @Override
    public List<Usuario> listarUsuarios() {

        return usuarioRepository.findAll();
    }

    /**
     * Carga el usuario cuyo nombre de usuario se pasa como parámetro
     * @param username Nombre de usuario
     * @return Usuario
     * @throws UsernameNotFoundException Excepcion si no encuentra ningún usuario
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByNombreUsuario(username);

        if(usuario == null){
            throw new UsernameNotFoundException("No existe el usuario");
        }

        Set<GrantedAuthority> rol = new HashSet<>();
        rol.add(new SimpleGrantedAuthority(usuario.getRole().getRoleName()));

        return new User(usuario.getNombreUsuario(), usuario.getPassword(), rol);
    }


    /**
     * Registra el usuario de administrador
     * @return True si lo ha registrado y false en caso contrario
     */
    @Override
    public boolean registryAdmin(){

        Role roleAdmin = roleRepository.findByRoleName("ROLE_ADMIN");

        List<Usuario> admins = usuarioRepository.findByRole(roleAdmin);

        for(Usuario usr : admins){
            if(roleAdmin.getRoleName().equals(usr.getRole().getRoleName())){
                return false;
            }
        }

        Usuario admin = new Usuario(
                "angelAdmin",
                encoder.encode("admin"),
                "angelvegabedate@gmail.com",
                roleAdmin);

        usuarioRepository.save(admin);

        return true;
    }

    public void registrarUsuariosPrueba(){

        Role roleStudent = roleRepository.findByRoleName("ROLE_STUDENT");
        Role roleTeacher = roleRepository.findByRoleName("ROLE_TEACHER");

        Usuario student = new Usuario("student", encoder.encode("student"), "student@student.com", roleStudent );
        Usuario teacher = new Usuario("teacher", encoder.encode("teacher"), "teacher@teacher.com", roleTeacher );

        usuarioRepository.save(student);
        usuarioRepository.save(teacher);

    }

    /**
     * Añade un atributo a la colección de atributos de la entidad Usuario si no lo contenía ya.
     * @param atributo Atributo
     * @return True si se ha añadido, false en caso contrario
     */
    public boolean addAtributosUsuario(AtributoDto atributo){

        if(admin == null){
            admin = new UsuarioAdminCredentials();
        }

        for(AtributoDto atr : atributosUsuario){
            if(atr.getNombre().equals(atributo.getNombre())){
                return false;
            }
        }

        return atributosUsuario.add(atributo);
    }

    //Para el generador
    @Override
    public void createRoles(List<RoleDto> roles){

        for(RoleDto role : roles) {
            if (roleRepository.findByRoleName(role.getRoleName()) == null) {
                roleRepository.save(new Role("ROLE_" + role.getRoleName().toUpperCase()));
            }
        }

    }

    @Override
    public void setAdmin(UsuarioAdminCredentials admin){
        this.admin = admin;
    }

    @Override
    public UsuarioAdminCredentials getAdmin(){
        return this.admin;
    }

    @Override
    public Set<AtributoDto> getAtributosUsuario(){

        return this.atributosUsuario;
    }

    public void clearAtributosUsuario(){
        atributosUsuario.clear();
    }

    public boolean isFlagAdminUser(){

        return flagAdminUser;
    }

    public void setFlagAdminUser(boolean adminUser){
        this.flagAdminUser = adminUser;
    }


}

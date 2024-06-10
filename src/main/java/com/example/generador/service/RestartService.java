package com.example.generador.service;


import com.example.generador.dto.RoleDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class RestartService {

    @Autowired
    private EntidadesService entidadesService;

    @Autowired
    private RelacionesService relacionesService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private IdiomaService idiomaService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private RolesService rolesService;

    @Autowired
    private DesignService designService;


    /**
     * Método que inicializa de nuevo todas las estructuras de las otras clases Service.
     */
    public void restartAllServices(){

        //EntidadesService
        entidadesService.clearEntidades();
        entidadesService.setVistaCompacta(false);

        //RelacionesService
        relacionesService.clearRelaciones();

        //UsuarioService
        usuarioService.clearAtributosUsuario();
        usuarioService.setAdmin(null);
        usuarioService.setFlagAdminUser(false);

        //IdiomaService
        idiomaService.clearIdiomas();
        idiomaService.setIdiomaDefault(null);

        //ProjectService
        projectService.setTitleProject(null);
        projectService.setAbbreviatedTitleProject(null);
        projectService.setModule(null);
        projectService.setNameApplication(null);
        projectService.setNameDB(null);
        projectService.setNumPuerto(8080);
        projectService.setNumPuertoDB(3306);
        projectService.setDatosIntroducidos(false);

        //RolesService
        rolesService.clearRoles();
        rolesService.setDefaultRole(null);
        rolesService.setRegistrarUsuarios(true);
        rolesService.addRole(new RoleDto(1,"ADMIN"));

        //DesignService
        designService.setColores(null);
        designService.setNav(true);

    }


    /**
     * Método que elimina el fichero que se le pasa como parámetro.
     * Si el fichero es un directorio, elimina todo lo que tiene.
     * @param f File
     */
    public void deleteOnCascade(File f) {

        if(!f.isDirectory()) {
            f.delete();
            return;
        }

        File[] files = f.listFiles();
        for(File file : files) {
            deleteOnCascade(file);
        }

        f.delete();
    }

}

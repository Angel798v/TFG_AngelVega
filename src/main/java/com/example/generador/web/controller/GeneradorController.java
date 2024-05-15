package com.example.generador.web.controller;

import com.example.generador.dto.*;
import com.example.generador.model.Project;
import com.example.generador.service.*;
import com.example.generador.util.ColorPicker;
import com.example.generador.util.UsuarioAdminCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class GeneradorController {

    private boolean primeraVez = true;

    @Autowired
    private EntidadesService entidadesService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private RelacionesService relacionesService;

    @Autowired
    private UrlService urlService;

    @Autowired
    private RolesService rolesService;

    @Autowired
    private GeneradorService generadorService;

    @Autowired
    private AtributosService atributosService;

    @Autowired
    private IdiomaService idiomaService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private DesignService designService;


    /**
     * Colección que contiene las entidades que se van a generar
     */
    List<EntidadDto> entidades;

    /**
     * Vista primer formulario del generador
     * @return Vista
     */
    @GetMapping("/Generador")
    public String vistaIntroduccionGenerador(){

        urlService.setUrl("/Generador");

        return "/Generador/TitleGenerador";
    }


    /**
     * Colección que contiene las entidades
     * @return Colección
     */
    @ModelAttribute("entidades")
    public List<EntidadDto> entidades(){

        entidades = entidadesService.getEntidades();

        return entidades;
    }

    /**
     * Colección que contiene las relaciones
     * @return
     */
    @ModelAttribute("relaciones")
    public List<Relacion> relaciones(){
        return relacionesService.getRelaciones();
    }



    /**
     * Vista de la generacion de la aplicacion web
     * @param entidades Lista de entidades de la aplicación a generar
     * @param model Model
     * @return Vista
     */
    @GetMapping("/Generacion")
    public String vistaGeneracion(@ModelAttribute("entidades") List<EntidadDto> entidades, Model model){

        urlService.setUrl("/Generacion");
        model.addAttribute("vistaCompacta", entidadesService.isVistaCompacta());

        return "/Generador/Generacion";
    }


    /**
     * Vista para el primer paso de la generación de un Proyecto.
     * Contiene las propiedades del proyecto a generar.
     * @param model Model
     * @return Vista
     */
    @GetMapping("/Creacion")
    public String vistaCreacion(Model model){

        urlService.setUrl("/Creacion");

        ProjectDto project = new ProjectDto();

        if(projectService.isDatosIntroducidos()){
            project.setTitle(projectService.getTitleProject());
            project.setAbr_title(projectService.getAbbreviatedTitleProject());
            project.setDir(projectService.getModule());
            project.setNameApplication(projectService.getNameApplication());
            project.setNameDB(projectService.getNameDB());
            project.setNumPuerto(projectService.getNumPuerto());
        }


        model.addAttribute("proyecto", project);
        model.addAttribute("notNullProyect", projectService.isDatosIntroducidos());

        return "/Generador/CreacionProyecto";
    }


    /**
     * Formulario que contiene las propiedades del proyecto a generar.
     * @return Form
     */
    @PostMapping("/Creacion")
    public String creacion(@ModelAttribute("proyecto") ProjectDto projectDto){

        projectService.setTitleProject(projectDto.getTitle());
        projectService.setAbbreviatedTitleProject(projectDto.getAbr_title());
        projectService.setModule(projectDto.getDir());

        String nameApplication = projectDto.getNameApplication().replace(" ", "");
        String letraInicial,resto = null;
        letraInicial = nameApplication.substring(0,1).toUpperCase();
        resto = nameApplication.substring(1);
        nameApplication = letraInicial + resto;

        projectService.setNameApplication(nameApplication);
        projectService.setNameDB(projectDto.getNameDB());
        projectService.setNumPuerto(projectDto.getNumPuerto());

        Project project = new Project();

        //FALTA RELLENAR EL OBJETO PROYECTO

        project.setTitle(projectDto.getTitle());

        //projectService.guardarProyecto(project);

        projectService.setDatosIntroducidos(true);


        return "redirect:/IndexGenerador";
    }


    /**
     * Vista principal del generador
     * @param model Model
     * @return Vista
     */
    @GetMapping("/IndexGenerador")
    public String vistaIndexGenerador(Model model){

        urlService.setUrl("/IndexGenerador");

        if(primeraVez){
            primeraVez = false;
            rolesService.addRole(new RoleDto(1,"ADMIN"));
        }

        model.addAttribute("title",projectService.getTitleProject());
        model.addAttribute("abrTitle",projectService.getAbbreviatedTitleProject());

        return "/Generador/IndexGenerador";
    }


    /**
     * Genera la aplicación web con los datos introducidos.
     * @return Vista instrucciones post-descarga
     */
    @PostMapping("/GenerateApplication")
    public String generateApplication() throws Exception{

        try {

            generadorService.createDirectories();
            generadorService.createDocs();

        }catch (Exception ex){
            throw new Exception(ex.getMessage());
        }

        return "redirect:/IndexGenerador";
    }


    /**
     * PRUEBAS.
     * Carga registros predefinidos para pruebas.
     * @return Vista Index Generador
     */
    @PostMapping("/cargarRegistros")
    public String cargarRegistros(){

        //MODELO

        //Entidades
        EntidadDto mecanico = new EntidadDto();
        EntidadDto cliente = new EntidadDto();
        EntidadDto tipo_servicio = new EntidadDto();
        EntidadDto reparacion = new EntidadDto();

        mecanico.setNombre("Mecanico");
        cliente.setNombre("Cliente");
        tipo_servicio.setNombre("Tipo_servicio");
        reparacion.setNombre("Reparacion");

        //entidadesService.switchVistaCompacta();

        //Atributos
        AtributoDto pk_mecanico = new AtributoDto("id_mecanico","long", true, false,false,false,"",false,"",0);
        AtributoDto nombre_mecanico = new AtributoDto("nombre","String", false, false,true,false,"",false,"",20);
        AtributoDto pk_cliente = new AtributoDto("id_cliente","long", true, false,false,false,"",false,"",0);
        AtributoDto pk_tipo_servicio = new AtributoDto("id_tipo_servicio","long", true, false,false,false,"",false,"",0);
        AtributoDto pk_reparacion = new AtributoDto("id_reparacion","long", true, false,false,false,"",false,"",0);

        atributosService.addAtributo(mecanico,pk_mecanico);
        atributosService.addAtributo(mecanico,nombre_mecanico);
        atributosService.addAtributo(cliente,pk_cliente);
        atributosService.addAtributo(tipo_servicio,pk_tipo_servicio);
        atributosService.addAtributo(reparacion,pk_reparacion);

        //Relaciones
        Relacion reparacion_mecanico = new Relacion(reparacion,mecanico,"N","0..1",true);
        reparacion_mecanico.setNameA(reparacion.getNombre());
        reparacion_mecanico.setNameB(mecanico.getNombre());

        Relacion reparacion_cliente = new Relacion(reparacion,cliente,"N","0..1",false);
        reparacion_cliente.setNameA(reparacion.getNombre());
        reparacion_cliente.setNameB(cliente.getNombre());

        Relacion reparacion_tipoServicio = new Relacion(reparacion,tipo_servicio,"N","M",true);
        reparacion_tipoServicio.setNameA(reparacion.getNombre());
        reparacion_tipoServicio.setNameB(tipo_servicio.getNombre());

        relacionesService.addRelacion(reparacion_mecanico);
        relacionesService.addRelacion(reparacion_cliente);
        relacionesService.addRelacion(reparacion_tipoServicio);

        entidadesService.addEntidad(mecanico);
        entidadesService.addEntidad(cliente);
        entidadesService.addEntidad(tipo_servicio);
        entidadesService.addEntidad(reparacion);

        Set<AtributoDto> atributos = new HashSet<AtributoDto>(usuarioService.getAtributosUsuario());
        UsuarioAdminCredentials usuarioAdmin = new UsuarioAdminCredentials("angel@gmail.com","angelAdmin","admin", atributos, null);
        usuarioService.setAdmin(usuarioAdmin);

        AtributoDto atributo = new AtributoDto("apellidos","String",false,false,true,false,"",false,"",15);
        usuarioService.addAtributosUsuario(atributo);

        AtributoDto atributo2 = new AtributoDto("direccion","String",false,false,false,false,"",false,"",20);
        usuarioService.addAtributosUsuario(atributo2);


        //IDIOMAS

        Idioma spanish = new Idioma("Español","ES","Visualizar","Crear","Editar","Eliminar",
                "Inicio","Servicios","Pagina de error","Nombre de usuario", "Contraseña","Email","Rol",
                "Usuarios","Guardar","Volver","Iniciar sesión","Cerrar sesión", "Registrar usuario", "Registro",
                "Idioma");
        Idioma english = new Idioma("Inglés","EN","Show","Add","Edit","Delete","Home",
                "Services","Error page","Username", "Password","Mail","Role",
                "Users","Save","Back","Login","Logout", "Registry user", "Signup", "Language");

        idiomaService.addIdioma(spanish);
        idiomaService.addIdioma(english);
        idiomaService.setIdiomaDefault(spanish);

        //PROJECT PROPERTIES

        projectService.setTitleProject("Taller SA");
        projectService.setAbbreviatedTitleProject("WkShop");
        projectService.setModule("workshop");
        projectService.setNameApplication("WorkShop");
        projectService.setNameDB("taller");
        projectService.setNumPuerto(8092);

        //SECURITY

        RoleDto admin = new RoleDto(1,"ADMIN");
        RoleDto user = new RoleDto(2,"USER");

        rolesService.addRole(admin);
        rolesService.addRole(user);

        for(EntidadDto entidad : entidadesService.getEntidades()) {

            if (!entidad.getNombre().equals(cliente.getNombre())){
                entidad.getPermisosRoles().getOperationGET().add(admin);
                entidad.getPermisosRoles().getOperationPOST().add(admin);
                entidad.getPermisosRoles().getOperationPUT().add(admin);
                entidad.getPermisosRoles().getOperationDELETE().add(admin);
                entidad.getPermisosRoles().getOperationGET().add(user);
                entidad.getPermisosRoles().getOperationPOST().add(user);
                entidad.getPermisosRoles().getOperationPUT().add(user);
                entidad.getPermisosRoles().getOperationDELETE().add(user);
            }else{
                entidad.getPermisosRoles().getOperationGET().add(admin);
                entidad.getPermisosRoles().getOperationPOST().add(admin);
                entidad.getPermisosRoles().getOperationPUT().add(admin);
                entidad.getPermisosRoles().getOperationDELETE().add(admin);
            }

        }

        rolesService.setRegistrarUsuarios(true);
        rolesService.setDefaultRole(user);

        //DISEÑO
        designService.setColores(new ColorPicker("#8ED8F1","#FFFFFF"));
        designService.switchNav();


        return "redirect:/IndexGenerador";
    }




}

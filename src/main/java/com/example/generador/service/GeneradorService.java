package com.example.generador.service;

import com.example.generador.dto.*;
import com.example.generador.exceptions.*;
import com.example.generador.util.ColorPicker;
import com.example.generador.util.UsuarioAdminCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
public class GeneradorService {

    /**
     * Ruta del directorio que contiene los demás directorios del código fuente.
     * Formato: com/example/dir
     */
    private String src;

    /**
     * Ruta del directorio que contiene los demás directorios pero con el formato necesario para las sentencias "package"
     * Formato: com.example.dir
     */
    private String pathCode;

    /**
     * Ruta del directorio que contiene los recursos del proyecto.
     */
    private String resources;

    @Autowired
    private EntidadesService entidadesService;

    @Autowired
    private RelacionesService relacionesService;

    @Autowired
    private IdiomaService idiomaService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RolesService rolesService;

    @Autowired
    private DesignService designService;

    /**
     * Crea los directorios necesarios para el proyecto a generar
     */
    public void createDirectories() throws DirectoriesException {

        List<File> directorios = new ArrayList<File>();

        String rutaSrc = projectService.getTitleProject() + "/src/main/java/com/example/" + projectService.getModule();
        String rutaResources = projectService.getTitleProject() + "/src/main/resources";

        this.src = rutaSrc;
        this.pathCode = "com.example." + projectService.getModule();
        this.resources = rutaResources;

        File principal = new File(rutaSrc);
        File resources = new File(rutaResources);

        File config = new File(rutaSrc + "/config");
        File dto = new File(rutaSrc + "/dto");
        File model = new File(rutaSrc + "/model");
        File repository = new File(rutaSrc + "/repository");
        File service = new File(rutaSrc + "/service");
        File util = new File(rutaSrc + "/util");
        File controller = new File(rutaSrc + "/controller");

        File staticD = new File(rutaResources + "/static");
        File templates = new File(rutaResources + "/templates");

        File css = new File(rutaResources + "/static/css");
        File fonts = new File(rutaResources + "/static/fonts");
        File images = new File(rutaResources + "/static/images");
        File js = new File(rutaResources + "/static/js");
        File sass = new File(rutaResources + "/static/sass");

        File crudOperations = new File(rutaResources + "/static/images/CRUDoperations");

        directorios.add(config);
        directorios.add(dto);
        directorios.add(model);
        directorios.add(repository);
        directorios.add(service);
        directorios.add(util);
        directorios.add(controller);

        directorios.add(staticD);
        directorios.add(templates);

        directorios.add(css);
        directorios.add(fonts);
        directorios.add(images);
        directorios.add(js);
        directorios.add(sass);

        directorios.add(crudOperations);

        if(!principal.mkdirs()){
            throw new DirectoriesException("No se han creado las carpetas principales. Path: this.src");
        } else if(!resources.mkdir()) {
            throw new DirectoriesException("No se ha creado la carpeta resources. Path: this.resources");
        }else{
            for(File f : directorios){
                if(!f.mkdir()){
                    throw new DirectoriesException("No se ha creado la carpeta \"" + f.getName() + "\".");
                }
            }
        }
    }


    /**
     * Genera los ficheros del proyecto
     */
    public void createDocs() throws Exception{

        List<Relacion> relaciones;

        try{
            for (EntidadDto entidad : entidadesService.getEntidades()) {
                relaciones = relacionesEntidad(relacionesService.getRelaciones(), entidad);
                generateModel(entidad, relaciones);
                generateDto(entidad, relaciones);
                generateRepository(entidad, relaciones);
                generateController(entidad, relaciones, entidadesService.isVistaCompacta());
                generateHtml(entidad, relaciones, entidadesService.isVistaCompacta());
                generateService(entidad);
            }
            generateInternationalizationMethod(idiomaService.getIdiomas());
            generateConfigDocs(entidadesService.getEntidades(),idiomaService.getIdiomaDefault());
            generateMainClass(projectService.getNameApplication());
            generateUserDocs(rolesService.getRoles(), usuarioService.getAdmin(),rolesService.getDefaultRole(),rolesService.isRegistrarUsuarios());
            generateRoleDocs();
            generateIndexDocs(projectService.getAbbreviatedTitleProject(), entidadesService.getEntidades(), idiomaService.getIdiomas(), designService.getColores(), designService.isNav());
            generatePropertiesDocs(idiomaService.getIdiomas(),idiomaService.getIdiomaDefault(), projectService.getTitleProject(), projectService.getNameDB(),
                    projectService.getNumPuerto());
            generateOthers();
            copyImages();
        }catch (Exception exception){
            throw new Exception(exception.getMessage());
        }
    }


    /**
     * Método auxiliar que genera un archivo MODEL
     * @param entidad Entidad que añade al modelo
     * @param relaciones Relaciones que tiene la entidad
     */
    private void generateModel(EntidadDto entidad, List<Relacion> relaciones) throws ModelException{
        File f = null;
        FileWriter writer = null;


        try {
            f = new File(this.src + "/model/" + entidad.getNombre() + ".java");
            writer = new FileWriter(f);

            //Package e imports
            writer.append("package " + this.pathCode + ".model;\n\n");

            writer.append("import jakarta.persistence.*;\n");
            writer.append("import java.io.Serializable;\n");
            writer.append("import lombok.*;\n");
            writer.append("import java.util.*;\n");
            writer.append("import java.sql.*;\n\n");


            //Anotaciones
            writer.append("@Entity\n");
            writer.append("@Getter\n");
            writer.append("@Setter\n");
            writer.append("@AllArgsConstructor\n");
            writer.append("@NoArgsConstructor\n");

            writer.append("@Table(name = \"" + entidad.getNombre() + "\")\n");

            //Cabecera de la clase
            writer.append("public class " + entidad.getNombre() + " implements Serializable {\n\n");

            //Constructor
            String constructor = "\tpublic " + entidad.getNombre() + "(";
            boolean primeraIteracion = true;

            if(entidad.getAtributos().size() > 1) {

                for (AtributoDto atr : entidad.getAtributos()) {
                    if (!atr.isPrimaryKey()) {
                        if (!primeraIteracion) {
                            constructor += ",";
                        }
                        constructor += atr.getTipo() + " " + atr.getNombre();
                        primeraIteracion = false;
                    }
                }
                constructor += ") {\n";
                writer.append(constructor);

                for (AtributoDto atr : entidad.getAtributos()) {
                    if (!atr.isPrimaryKey()) {
                        writer.append("\t\tthis." + atr.getNombre() + " = " + atr.getNombre() + ";\n");
                    }
                }
                writer.append("\t}\n\n\n");

            }

            String aux = "";

            //Atributos
            for(AtributoDto atr : entidad.getAtributos()) {
                aux = "";
                if(atr.isPrimaryKey()) {
                    writer.append("\t@Id\n\t@GeneratedValue(strategy = GenerationType.AUTO)\n\tprivate " + atr.getTipo() + " " + atr.getNombre() + ";\n\n");
                }else {
                    aux = "\t@Column(name = \"" + atr.getNombre() + "\", ";
                    if(atr.getTipo().equals("String")) {
                        aux += "length = " + atr.getSizeText() + ", ";
                    }

                    if(atr.isNullable()) {
                        aux += "nullable = true, ";
                    }else {
                        aux += "nullable = false, ";
                    }

                    if(atr.isUnique()) {
                        aux += "unique = true)\n";
                    }else {
                        aux += "unique = false)\n";
                    }
                    aux += "\tprivate " + atr.getTipo() + " " + atr.getNombre().toLowerCase() + ";\n\n\n";
                    writer.append(aux);
                }
            }

            //Relaciones
            aux = "";
            for(Relacion relacion : relaciones) {

                if(relacion.getA() == entidad) {
                    if(relacion.getCardinalityA().equals("N") && relacion.getCardinalityB().equals("M")) {	//Many to many
                        aux = "\t@ManyToMany\n\t@JoinTable(name = \"" + relacion.getNameA() + "_" + relacion.getNameB() + "\",\n"
                                + "\t\tjoinColumns = @JoinColumn(name=\"id_" + relacion.getNameA().toLowerCase() + "\"),\n"
                                + "\t\tinverseJoinColumns = @JoinColumn(name=\"id_" + relacion.getNameB().toLowerCase() + "\"))\n"
                                + "\tprivate Set<" + relacion.getNameB() + "> " + relacion.getNameB().toLowerCase() + "s;\n\n";

                        aux += "\tpublic void add" + relacion.getNameB() + "(" + relacion.getNameB() + " " + relacion.getNameB().toLowerCase() + "){\n";
                        aux += "\t\tif(" + relacion.getNameB().toLowerCase() + "s == null){\n\t\t\t" + relacion.getNameB().toLowerCase() + "s = new HashSet<" +
                                relacion.getNameB() + ">();\n\t\t}\n\n";
                        aux += "\t\tif(" + relacion.getNameB().toLowerCase() + " != null && !" + relacion.getNameB().toLowerCase() +
                                "s.contains(" + relacion.getNameB().toLowerCase() + ")){\n";

                        if(relacion.isBidireccional()) {
                            aux += "\t\t\tif(!" + relacion.getNameB().toLowerCase() + ".get" + relacion.getNameA() + "s().contains(this)){\n" +
                                    "\t\t\t\t" + relacion.getNameB().toLowerCase() + ".get" + relacion.getNameA() + "s().add(this);\n\t\t\t}\n";
                        }
                        aux += "\t\t\t" + relacion.getNameB().toLowerCase() + "s.add(" + relacion.getNameB().toLowerCase() + ");\n\t\t}\n\t}\n\n";

                    }else {
                        if(relacion.getCardinalityA().equals("0..1")) {
                            if(relacion.getCardinalityB().equals("0..1")) { //One to one
                                aux = "\t@OneToOne\n\t@JoinColumn(name=\"id_" + relacion.getNameB().toLowerCase() + "\")\n\tprivate "
                                        + relacion.getNameB() + " " + relacion.getNameB().toLowerCase() + ";\n\n";

                                aux += "\tpublic void set" + relacion.getNameB() + "(" + relacion.getNameB() + " " + relacion.getNameB().toLowerCase() + "){\n" +
                                        "\t\tif(this." + relacion.getNameB().toLowerCase() + " != null){\n\t\t\tthis." + relacion.getNameB().toLowerCase() +
                                        ".set" + relacion.getNameA() + "(null);\n" +
                                        "\t\t}\n" +
                                        "this." + relacion.getNameB().toLowerCase() + " = " + relacion.getNameB().toLowerCase() + ";\n" +
                                        "\t\tif(" + relacion.getNameB().toLowerCase() + " != null){\n" +
                                        "\t\t\t" + relacion.getNameB().toLowerCase() + ".set" + relacion.getNameA() + "(this);\n\t\t}\n\t}\n\n";

                            }else {	//One to many
                                aux = "\t@OneToMany\n\t@JoinColumn(name=\"id_" + relacion.getNameB().toLowerCase() + "\")\n\tprivate Set<"
                                        + relacion.getNameB() + "> " + relacion.getNameB().toLowerCase() + "s;\n\n";
                            }
                        }else if(relacion.getCardinalityB().equals("0..1")) { //Many to one
                            aux = "\t@ManyToOne\n\t@JoinColumn(name=\"id_" + relacion.getNameB().toLowerCase() + "\")\n\tprivate "
                                    + relacion.getNameB() + " " + relacion.getNameB().toLowerCase() + ";\n\n";

                            if(relacion.isBidireccional()) {
                                aux += "\tpublic void set" + relacion.getNameB() + "(" + relacion.getNameB() + " " + relacion.getNameB().toLowerCase() + "){\n" +
                                        "\t\tif(this." + relacion.getNameB().toLowerCase() + " != null){\n" +
                                        "\t\t\tthis." + relacion.getNameB().toLowerCase() + ".get" + relacion.getNameA() + "s().remove(this);\n\t\t}\n" +
                                        "\t\tthis." + relacion.getNameB().toLowerCase() + " = " + relacion.getNameB().toLowerCase() + ";\n" +
                                        "\t\tif(" + relacion.getNameB().toLowerCase() + " != null && !" + relacion.getNameB().toLowerCase() + ".get"
                                        + relacion.getNameA() + "s().contains(this)){\n" +
                                        "\t\t\t" + relacion.getNameB().toLowerCase() + ".get" + relacion.getNameA() + "s().add(this);\n\t\t}\n\t}\n\n\n";

                            }
                        }
                    }
                }else if(relacion.getB() == entidad && relacion.isBidireccional()){
                    if(relacion.getCardinalityA().equals("N") && relacion.getCardinalityB().equals("M")) {	//Many to many
                        aux = "\t@ManyToMany(mappedBy=\"" + relacion.getNameB().toLowerCase() + "s\")\n"
                                + "\tprivate Set<" + relacion.getNameA() + "> " + relacion.getNameA().toLowerCase() + "s;\n\n";

                        aux += "\tpublic void add" + relacion.getNameA() + "s(" + relacion.getNameA() + " " + relacion.getNameA().toLowerCase() + "){\n" +
                                "\t\tif(" + relacion.getNameA().toLowerCase() + "s == null){\n" +
                                "\t\t\t" + relacion.getNameA().toLowerCase() + "s = new HashSet<" + relacion.getNameA() + ">();\n\t\t}\n\n" +
                                "\t\tif(" + relacion.getNameA().toLowerCase() + " != null && !" + relacion.getNameA().toLowerCase() + "s.contains("
                                + relacion.getNameA().toLowerCase() + ")){\n" +
                                "\t\t\tif(!" + relacion.getNameA().toLowerCase() + ".get" + relacion.getNameB() + "s().contains(this)){\n" +
                                "\t\t\t\t" + relacion.getNameA().toLowerCase() + ".get" + relacion.getNameB() + "s().add(this);\n\t\t\t}\n" +
                                "\t\t\t" + relacion.getNameA().toLowerCase() + "s.add(" + relacion.getNameA().toLowerCase() + ");\n" +
                                "\t\t}\n\t}\n\n\n";
                    }else {
                        if(relacion.getCardinalityA().equals("0..1")) {
                            if(relacion.getCardinalityB().equals("0..1")) { //One to one
                                aux = "\t@OneToOne(mappedBy=\"" + relacion.getNameB().toLowerCase() + "\")\n\tprivate "
                                        + relacion.getNameA() + " " + relacion.getNameA().toLowerCase() + ";\n\n";
                            }else {	//Many to one
                                aux = "\t@ManyToOne(mappedBy=\"" + relacion.getNameB().toLowerCase() + "s\")\n\tprivate "
                                        + relacion.getNameA() + " " + relacion.getNameA().toLowerCase() + ";\n\n";
                            }
                        }else if(relacion.getCardinalityB().equals("0..1")) { //One to many
                            aux = "\t@OneToMany(mappedBy=\"" + relacion.getNameB().toLowerCase() + "\")\n\tprivate Set<"
                                    + relacion.getNameA() + "> " + relacion.getNameA().toLowerCase() + "s;\n\n";

                            aux += "\tpublic boolean add" + relacion.getNameA() + "(" + relacion.getNameA() + " " + relacion.getNameA().toLowerCase() + "){\n" +
                                    "\t\tif(" + relacion.getNameA().toLowerCase() + "s == null){\n" +
                                    "\t\t\t" + relacion.getNameA().toLowerCase() + "s = new HashSet<" + relacion.getNameA() + ">();\n\t\t}\n\n" +
                                    "\t\tif(" + relacion.getNameA().toLowerCase() + " != null && !" + relacion.getNameA().toLowerCase() + "s.contains("
                                    + relacion.getNameA().toLowerCase() + ")){\n" +
                                    "\t\t\tif(" + relacion.getNameA().toLowerCase() + ".get" + relacion.getNameB() + "() != null){\n" +
                                    "\t\t\t\t" + relacion.getNameA().toLowerCase() + ".get" + relacion.getNameB() + "().get" + relacion.getNameA()
                                    + "s().remove(" + relacion.getNameA().toLowerCase() + ");\n\t\t\t}\n" +
                                    "\t\t\t" + relacion.getNameA().toLowerCase() + ".set" + relacion.getNameB() + "(this);\n" +
                                    "\t\t\treturn true;\n\t\t}\n\n\t\treturn false;\n\t}\n\n\n";
                        }
                    }
                }

                writer.append(aux);
            }

            //Relation with User
            writer.append("\t@ManyToOne\n" +
                    "\t@JoinColumn(name = \"id_user\")\n" +
                    "\tprivate User user;\n\n\n");

            //toString() method
            writer.append("\t@Override\n\tpublic String toString(){\n\n");
            aux = "\t\treturn ";
            if(entidad.getAtributos().size() <= 1){
                aux += "\"\" + ";
            }
            primeraIteracion = true;
            for(AtributoDto atr : entidad.getAtributos()){
                if(!primeraIteracion){
                    aux += " + \"-\" + ";
                }else{
                    primeraIteracion = false;
                }
                aux += atr.getNombre();
            }
            aux += ";\n\t}\n\n";
            writer.append(aux);


            //EOF
            writer.append("\n}");


        }catch(Exception ex) {
            throw new ModelException(ex.getMessage());
        }finally {
            try {
                writer.close();
            }catch(Exception ex) {
                System.out.println(ex.getMessage());
            }
        }

    }

    /**
     * Método auxiliar para obtener las relaciones de una entidad
     * @param relacionesTotales Relaciones totales del proyecto a generar
     * @param entidad Entidad de la que se desean obtener las relaciones
     * @return Lista de relaciones de la entidad
     */
    private List<Relacion> relacionesEntidad(List<Relacion> relacionesTotales, EntidadDto entidad){

        List<Relacion> relaciones = new ArrayList<Relacion>();

        for(Relacion relacion : relacionesTotales){
            if(relacion.getA() == entidad || relacion.getB() == entidad){
                relaciones.add(relacion);
            }
        }

        return relaciones;
    }


    /**
     * Genera el archivo DTO de una entidad
     * @param entidad Entidad
     */
    private void generateDto(EntidadDto entidad, List<Relacion> relaciones) throws DtoException {

        File f = null;
        FileWriter writer = null;

        try {

            f = new File( this.src + "/dto/" + entidad.getNombre() + "Dto.java");
            writer = new FileWriter(f);

            //Package e imports
            writer.append("package " + this.pathCode + ".dto;\n\n");

            writer.append("import lombok.*;\n\n");

            //Anotaciones
            writer.append("@Getter\n");
            writer.append("@Setter\n");
            writer.append("@AllArgsConstructor\n");
            writer.append("@NoArgsConstructor\n");
            writer.append("public class " + entidad.getNombre() + "Dto {\n\n");


            //Atributos
            for(AtributoDto atributo : entidad.getAtributos()) {
                writer.append("\tprivate " + atributo.getTipo() + " " + atributo.getNombre() + ";\n\n");
            }

            //Relaciones
            for(Relacion relacion : relaciones){
                if(relacion.getA() == entidad){
                    if(relacion.getCardinalityA().equals("N") && relacion.getCardinalityB().equals("M")) {
                        writer.append("\tprivate String[] " + relacion.getNameB().toLowerCase() + "Dto;\n\n");
                    }else{
                        writer.append("\tprivate " + relacion.getNameB() + "Dto " + relacion.getNameB().toLowerCase() + "Dto;\n\n");
                    }
                }
            }

            //EOF
            writer.append("}");

        } catch (Exception ex) {
            throw new DtoException(ex.getMessage());
        } finally {

            try {
                if(writer != null) {
                    writer.close();
                }
            }catch(Exception ex) {
                System.out.println(ex.getMessage());
            }
        }


    }


    /**
     * Genera el archivo REPOSITORY de la entidad que se le pasa como parámetro
     * @param entidad Entidad
     * @param relaciones Lista relaciones
     * @throws RepositoryException Excepción al crear repositorio
     */
    private void generateRepository(EntidadDto entidad, List<Relacion> relaciones) throws RepositoryException {

        File f = null;
        FileWriter writer = null;

        try {

            f = new File(this.src + "/repository/" + entidad.getNombre() + "Repository.java");
            writer = new FileWriter(f);


            //Package e imports
            writer.append("package " + this.pathCode + ".repository;\n\n");

            writer.append("import " + this.pathCode + ".model." + entidad.getNombre() + ";\n");
            writer.append("import org.springframework.data.jpa.repository.JpaRepository;\n");
            writer.append("import org.springframework.stereotype.Repository;\n\n");


            //Cabecera de la interfaz
            writer.append("@Repository\n");
            writer.append("public interface " + entidad.getNombre() + "Repository extends JpaRepository<" + entidad.getNombre() + ",Long> {\n\n");

            //Funciones
            writer.append("\t" + entidad.getNombre() + " findById(long id);\n\n");


            writer.append("\tvoid deleteById(long id);\n\n");


            for(Relacion relacion : relaciones){
                if(relacion.getA() == entidad) {
                    if (relacion.getCardinalityA().equals("0..1") && relacion.getCardinalityB().equals("0..1")) {
                        writer.append("\t" + relacion.getNameA() + " findBy" + relacion.getNameB() + "Id(long id)\n\n");
                    }
                }
            }

            //EOF
            writer.append("}");


        }catch(Exception ex) {
            throw new RepositoryException(ex.getMessage());
        }finally {

            try {
                if(writer != null) {
                    writer.close();
                }
            }catch(Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }


    /**
     * Genera el archivo CONTROLLER de la entidad que se le pasa como parámetro
     * @param entidad Entidad
     * @param relaciones  Lista relaciones
     * @throws ControllerException Excepción al crear controller
     */
    private void generateController(EntidadDto entidad, List<Relacion> relaciones, boolean vistaCompata) throws ControllerException{

        File f = null;
        FileWriter writer = null;

        try {

            if(!vistaCompata) {
                f = new File(this.src + "/controller/" + entidad.getNombre() + "Controller.java");
                writer = new FileWriter(f);

                //Package e Imports
                writer.append("package " + this.pathCode + ".controller;\n\n");

                writer.append("import org.springframework.beans.factory.annotation.Autowired;\n");
                writer.append("import org.springframework.stereotype.Controller;\n");
                writer.append("import org.springframework.ui.Model;\n");
                writer.append("import org.springframework.web.bind.annotation.*;\n");
                writer.append("import org.springframework.security.core.Authentication;\n");
                writer.append("import org.springframework.security.core.context.SecurityContextHolder;\n");
                writer.append("import java.util.*;\n");
                writer.append("import " + this.pathCode + ".service.UrlService;\n");
                writer.append("import " + this.pathCode + ".model." + entidad.getNombre() + ";\n");
                writer.append("import " + this.pathCode + ".dto." + entidad.getNombre() + "Dto;\n");
                writer.append("import " + this.pathCode + ".repository." + entidad.getNombre() + "Repository;\n");
                for (Relacion relacion : relaciones) {
                    if (relacion.getA() != entidad) {
                        writer.append("import " + this.pathCode + ".model." + relacion.getNameA() + ";\n");
                        writer.append("import " + this.pathCode + ".dto." + relacion.getNameA() + "Dto;\n");
                        writer.append("import " + this.pathCode + ".repository." + relacion.getNameA() + "Repository;\n");
                    } else if (relacion.getB() != entidad) {
                        writer.append("import " + this.pathCode + ".model." + relacion.getNameB() + ";\n");
                        writer.append("import " + this.pathCode + ".dto." + relacion.getNameB() + "Dto;\n");
                        writer.append("import " + this.pathCode + ".repository." + relacion.getNameB() + "Repository;\n");
                    }
                }
                writer.append("import " + this.pathCode + ".model.User;\n" +
                        "import " + this.pathCode + ".repository.UserRepository;\n\n\n");

                //Class
                writer.append("@Controller\n");
                writer.append("@RequestMapping(\"/" + entidad.getNombre() + "\")\n");
                writer.append("public class " + entidad.getNombre() + "Controller {\n\n");

                writer.append("\t@Autowired\n");
                writer.append("\tprivate " + entidad.getNombre() + "Repository " + entidad.getNombre().toLowerCase() + "Repository;\n\n");
                for (Relacion relacion : relaciones) {
                    if (relacion.getA() == entidad) {
                        writer.append("\t@Autowired\n");
                        writer.append("\tprivate " + relacion.getNameB() + "Repository " + relacion.getNameB().toLowerCase() + "Repository;\n\n");
                    }
                }

                writer.append("\t@Autowired\n" +
                        "\tprivate UserRepository userRepository;\n\n");

                writer.append("\t@Autowired\n");
                writer.append("\tprivate UrlService urlService;\n\n");

                //principal entity view method
                writer.append("\t//Principal entity operations view\n\n");

                writer.append("\t@GetMapping\n");
                writer.append("\tpublic String principal" + entidad.getNombre() + "(){\n\n");
                writer.append("\t\turlService.setUrl(\"/" + entidad.getNombre() + "\");\n\n");
                writer.append("\t\treturn \"/" + entidad.getNombre() + "/principal" + entidad.getNombre() + "\";\n\t}\n\n");


                //Operation GET
                writer.append("\t//Operation GET\n\n");

                writer.append("\t@GetMapping(\"/Get\")\n");
                writer.append("\tpublic String operationGet" + entidad.getNombre() + "(Model model){\n\n");
                writer.append("\t\turlService.setUrl(\"/" + entidad.getNombre() + "/Get\");\n");
                writer.append("\t\tmodel.addAttribute(\"listAll\", " + entidad.getNombre().toLowerCase() + "Repository.findAll());\n\n");
                writer.append("\t\treturn \"/" + entidad.getNombre() + "/operationGet/operationGet" + entidad.getNombre() + "\";\n\t}\n\n");

                //Operation POST
                writer.append("\t//Operation POST\n\n");

                writer.append("\t@GetMapping(\"/Post\")\n");
                writer.append("\tpublic String operationPost" + entidad.getNombre() + "(Model model){\n\n");
                writer.append("\t\turlService.setUrl(\"/" + entidad.getNombre() + "/Post\");\n");
                writer.append("\t\tmodel.addAttribute(\"object\", new " + entidad.getNombre() + "Dto());\n");
                for (Relacion relacion : relaciones) {
                    if (relacion.getA() == entidad) {
                        writer.append("\t\tmodel.addAttribute(\"list" + relacion.getNameB() + "\", " + relacion.getNameB().toLowerCase() + "Repository.findAll());\n");
                    }
                }
                writer.append("\n\t\treturn \"/" + entidad.getNombre() + "/operationPost/operationPost" + entidad.getNombre() + "\";\n\t}\n\n");

                writer.append("\t@PostMapping(\"/Post\")\n");
                writer.append("\tpublic String operationPost(" + entidad.getNombre() + "Dto " + entidad.getNombre().toLowerCase() + "Dto){\n\n");

                for (Relacion relacion : relaciones) {

                    if (relacion.getA() == entidad) {
                        if (relacion.getCardinalityA().equals("N")) {
                            if (relacion.getCardinalityB().equals("M")) { //ManyToMany
                                writer.append("\t\t//Relation - ManyToMany with " + relacion.getB().getNombre() + "\n");
                                writer.append("\t\tSet<" + relacion.getB().getNombre() + "> " + relacion.getB().getNombre().toLowerCase() + "s = new HashSet<" + relacion.getB().getNombre() + ">();\n");
                                writer.append("\t\tfor(String " + relacion.getB().getNombre().toLowerCase() + "DtoId : " + entidad.getNombre().toLowerCase() + "Dto.get" + relacion.getB().getNombre() + "Dto()){\n");
                                writer.append("\t\t\t" + relacion.getB().getNombre().toLowerCase() + "s.add(" + relacion.getB().getNombre().toLowerCase() + "Repository.findById(Long.parseLong("
                                        + relacion.getB().getNombre().toLowerCase() + "DtoId)));\n\t\t}\n\n");

                            } else if (relacion.getCardinalityB().equals("0..1")) {    //ManyToOne
                                writer.append("\t\t//Relation - ManyToOne with " + relacion.getB().getNombre() + "\n");
                                writer.append("\t\t" + relacion.getB().getNombre() + " " + relacion.getB().getNombre().toLowerCase() + " = " +
                                        relacion.getB().getNombre().toLowerCase() + "Repository.findById(" +
                                        entidad.getNombre().toLowerCase() + "Dto.get" + relacion.getB().getNombre() + "Dto().getId_" + relacion.getB().getNombre().toLowerCase() + "());\n\n");
                            }
                        } else if (relacion.getCardinalityA().equals("0..1") && relacion.getCardinalityB().equals("0..1")) {   //OneToOne
                            writer.append("\t\t//Relation - OneToOne with " + relacion.getB().getNombre() + "\n");
                            writer.append("\t\tif(" + entidad.getNombre().toLowerCase() + "Repository.findBy" + relacion.getB().getNombre() + "Id_" + relacion.getB().getNombre().toLowerCase() + "(" +
                                    entidad.getNombre().toLowerCase() + "Dto.get" + relacion.getB().getNombre() + "Dto().getId()) != null){\n");
                            writer.append("\t\t\treturn \"redirect:/" + entidad.getNombre() + "?failPost\";\n\t\t}\n");
                            writer.append("\t\t" + relacion.getB().getNombre() + " " + relacion.getB().getNombre().toLowerCase() + " = " + relacion.getB().getNombre().toLowerCase() + "Repository.findById(" +
                                    entidad.getNombre().toLowerCase() + "Dto.get" + relacion.getB().getNombre() + "Dto().getId());\n\n");
                        }
                    }
                }

                writer.append("\t\tAuthentication auth = SecurityContextHolder.getContext().getAuthentication();\n" +
                        "\t\torg.springframework.security.core.userdetails.User userConnected = (org.springframework.security.core.userdetails.User) auth.getPrincipal();\n" +
                        "\t\tUser user = userRepository.findByUsername(userConnected.getUsername());\n\n");

                writer.append("\t\t" + entidad.getNombre() + " " + entidad.getNombre().toLowerCase() + " = new " + entidad.getNombre() + "();\n");
                String letraInicial, resto, nombreCompleto = null;
                for (AtributoDto atr : entidad.getAtributos()) {
                    if (!atr.isPrimaryKey()) {
                        letraInicial = atr.getNombre().substring(0, 1).toUpperCase();
                        resto = atr.getNombre().substring(1);
                        nombreCompleto = letraInicial + resto;
                        writer.append("\t\t" + entidad.getNombre().toLowerCase() + ".set" + nombreCompleto + "(" + entidad.getNombre().toLowerCase() + "Dto.get" + nombreCompleto + "());\n");
                    }
                }
                for (Relacion relacion : relaciones) {
                    if (relacion.getA() == entidad) {
                        if (relacion.getCardinalityA().equals("N")) {
                            if (relacion.getCardinalityB().equals("M")) { //ManyToMany
                                writer.append("\t\tfor(" + relacion.getB().getNombre() + " " + relacion.getB().getNombre().toLowerCase() + " : " + relacion.getB().getNombre().toLowerCase() + "s){\n");
                                writer.append("\t\t\t" + entidad.getNombre().toLowerCase() + ".add" + relacion.getB().getNombre() + "(" + relacion.getB().getNombre().toLowerCase() + ");\n\t\t}\n");
                            } else if (relacion.getCardinalityB().equals("0..1")) {    //ManyToOne
                                writer.append("\t\t" + entidad.getNombre().toLowerCase() + ".set" + relacion.getB().getNombre() + "(" + relacion.getB().getNombre().toLowerCase() + ");\n");
                            }
                        } else if (relacion.getCardinalityA().equals("0..1") && relacion.getCardinalityB().equals("0..1")) {   //OneToOne
                            writer.append("\t\t" + entidad.getNombre().toLowerCase() + ".set" + relacion.getB().getNombre() + "(" + relacion.getB().getNombre().toLowerCase() + ");\n");
                        }
                    }
                }

                writer.append("\t\t" + entidad.getNombre().toLowerCase() + ".setUser(user);\n\n");

                writer.append("\t\t" + entidad.getNombre().toLowerCase() + "Repository.save(" + entidad.getNombre().toLowerCase() + ");\n\n");
                writer.append("\t\treturn \"redirect:/" + entidad.getNombre() + "?successPost\";\n\t}\n\n");


                //Operation PUT
                writer.append("\t//Operation PUT\n\n");
                writer.append("\t@GetMapping(\"/Put\")\n");
                writer.append("\tpublic String operationPut" + entidad.getNombre() + "(Model model){\n\n");
                writer.append("\t\turlService.setUrl(\"/" + entidad.getNombre() + "/Put\");\n");
                writer.append("\t\tmodel.addAttribute(\"listAll\", " + entidad.getNombre().toLowerCase() + "Repository.findAll());\n\n");
                writer.append("\t\treturn \"/" + entidad.getNombre() + "/operationPut/operationPut" + entidad.getNombre() + "View\";\n\t}\n\n");

                writer.append("\t@GetMapping(\"/Put/{id}\")\n");
                writer.append("\tpublic String operationPut" + entidad.getNombre() + "(@PathVariable(\"id\") long id, Model model){\n\n");
                writer.append("\t\t" + entidad.getNombre() + " " + entidad.getNombre().toLowerCase() + " = " + entidad.getNombre().toLowerCase() + "Repository.findById(id);\n");
                writer.append("\t\t" + entidad.getNombre() + "Dto " + entidad.getNombre().toLowerCase() + "Dto = new " + entidad.getNombre() + "Dto();\n\n");
                for (AtributoDto atr : entidad.getAtributos()) {
                    letraInicial = atr.getNombre().substring(0, 1).toUpperCase();
                    resto = atr.getNombre().substring(1);
                    nombreCompleto = letraInicial + resto;
                    writer.append("\t\t" + entidad.getNombre().toLowerCase() + "Dto.set" + nombreCompleto + "(" + entidad.getNombre().toLowerCase() + ".get" + nombreCompleto + "());\n");
                }

                writer.append("\n");
                for (Relacion relacion : relaciones) {
                    if (relacion.getA() == entidad) {
                        if (relacion.getCardinalityA().equals("N")) {
                            if (relacion.getCardinalityB().equals("M")) { //ManyToMany
                                writer.append("\t\tString[] " + relacion.getB().getNombre().toLowerCase() + "Dto = new String[]{};\n");
                                writer.append("\t\t" + entidad.getNombre().toLowerCase() + "Dto.set" + relacion.getB().getNombre() + "Dto(" + relacion.getB().getNombre().toLowerCase() + "Dto);\n");
                                writer.append("\t\tmodel.addAttribute(\"list" + relacion.getB().getNombre() + "\", " + relacion.getB().getNombre().toLowerCase() + "Repository.findAll());\n\n");
                            } else if (relacion.getCardinalityB().equals("0..1")) {    //ManyToOne
                                writer.append("\t\t" + relacion.getB().getNombre() + "Dto " + relacion.getB().getNombre().toLowerCase() + "Dto = new " + relacion.getB().getNombre() + "Dto();\n");
                                writer.append("\t\t" + relacion.getB().getNombre().toLowerCase() + "Dto.setId_" + relacion.getB().getNombre().toLowerCase() + "(" + entidad.getNombre().toLowerCase() + ".get" + relacion.getB().getNombre() + "().getId_" + relacion.getB().getNombre().toLowerCase() + "());\n");
                                writer.append("\t\t" + entidad.getNombre().toLowerCase() + "Dto.set" + relacion.getB().getNombre() + "Dto(" + relacion.getB().getNombre().toLowerCase() + "Dto);\n");
                                writer.append("\t\tmodel.addAttribute(\"list" + relacion.getB().getNombre() + "\", " + relacion.getB().getNombre().toLowerCase() + "Repository.findAll());\n\n");
                            }
                        } else if (relacion.getCardinalityA().equals("0..1") && relacion.getCardinalityB().equals("0..1")) {   //OneToOne
                            writer.append("\t\t" + entidad.getNombre() + "Dto " + relacion.getB().getNombre().toLowerCase() + "Dto = new " + relacion.getB().getNombre() + "Dto();\n");
                            writer.append("\t\t" + relacion.getB().getNombre().toLowerCase() + "Dto.setId(" + entidad.getNombre().toLowerCase() + ".get" + relacion.getB().getNombre() + "().getId_" + relacion.getB().getNombre().toLowerCase() + "());\n");
                            writer.append("\t\t" + entidad.getNombre().toLowerCase() + "Dto.set" + relacion.getB().getNombre() + "Dto(" + relacion.getB().getNombre().toLowerCase() + "Dto);\n");
                            writer.append("\t\tmodel.addAttribute(\"list" + relacion.getB().getNombre() + "\", " + relacion.getB().getNombre().toLowerCase() + "Repository.findAll());\n\n");
                        }
                    }
                }

                writer.append("\t\tmodel.addAttribute(\"object\", " + entidad.getNombre().toLowerCase() + "Dto);\n\n");

                writer.append("\t\treturn \"/" + entidad.getNombre() + "/operationPut/operationPut" + entidad.getNombre() + "\";\n\t}\n\n");

                writer.append("\t@PostMapping(\"/Put/{id}\")\n");
                writer.append("\tpublic String operationPut(@PathVariable(\"id\") long id, " + entidad.getNombre() + "Dto " + entidad.getNombre().toLowerCase() + "Dto){\n\n");
                for (Relacion relacion : relaciones) {
                    if (relacion.getA() == entidad) {
                        if (relacion.getCardinalityA().equals("N")) {
                            if (relacion.getCardinalityB().equals("M")) { //ManyToMany
                                writer.append("\t\t//Relation - ManyToMany with " + relacion.getB().getNombre() + "\n");
                                writer.append("\t\tSet<" + relacion.getB().getNombre() + "> " + relacion.getB().getNombre().toLowerCase() + "s = new HashSet<" + relacion.getB().getNombre() + ">();\n");
                                writer.append("\t\tfor(String " + relacion.getB().getNombre().toLowerCase() + "DtoId : " + entidad.getNombre().toLowerCase() + "Dto.get" + relacion.getB().getNombre() + "Dto()){\n");
                                writer.append("\t\t\t" + relacion.getB().getNombre().toLowerCase() + "s.add(" + relacion.getB().getNombre().toLowerCase() + "Repository.findById(Long.parseLong("
                                        + relacion.getB().getNombre().toLowerCase() + "DtoId)));\n\t\t}\n\n");

                            } else if (relacion.getCardinalityB().equals("0..1")) {    //ManyToOne
                                writer.append("\t\t//Relation - ManyToOne with " + relacion.getB().getNombre() + "\n");
                                writer.append("\t\t" + relacion.getB().getNombre() + " " + relacion.getB().getNombre().toLowerCase() + " = " +
                                        relacion.getB().getNombre().toLowerCase() + "Repository.findById(" +
                                        entidad.getNombre().toLowerCase() + "Dto.get" + relacion.getB().getNombre() + "Dto().getId_" + relacion.getB().getNombre().toLowerCase() + "());\n\n");
                            }
                        } else if (relacion.getCardinalityA().equals("0..1") && relacion.getCardinalityB().equals("0..1")) {   //OneToOne
                            writer.append("\t\t//Relation - OneToOne with " + relacion.getB().getNombre() + "\n");
                            writer.append("\t\tif(" + entidad.getNombre().toLowerCase() + "Repository.findBy" + relacion.getB().getNombre() + "Id(" +
                                    entidad.getNombre().toLowerCase() + "Dto.get" + relacion.getB().getNombre() + "Dto().getId()) != null){\n");
                            writer.append("\t\t\treturn \"redirect:/" + entidad.getNombre() + "?failPut\";\n\t\t}\n");
                            writer.append("\t\t" + relacion.getB().getNombre() + " " + relacion.getB().getNombre().toLowerCase() + " = " + relacion.getB().getNombre().toLowerCase() + "Repository.findById(" +
                                    entidad.getNombre().toLowerCase() + "Dto.get" + relacion.getB().getNombre() + "Dto().getId_" + relacion.getB().getNombre().toLowerCase() + "());\n\n");
                        }
                    }
                }

                writer.append("\t\tAuthentication auth = SecurityContextHolder.getContext().getAuthentication();\n" +
                        "\t\torg.springframework.security.core.userdetails.User userConnected = (org.springframework.security.core.userdetails.User) auth.getPrincipal();\n" +
                        "\t\tUser user = userRepository.findByUsername(userConnected.getUsername());\n\n");

                writer.append("\t\t" + entidad.getNombre() + " " + entidad.getNombre().toLowerCase() + " = " + entidad.getNombre().toLowerCase() + "Repository.findById(id);\n");
                for (AtributoDto atr : entidad.getAtributos()) {
                    if (!atr.isPrimaryKey()) {
                        letraInicial = atr.getNombre().substring(0, 1).toUpperCase();
                        resto = atr.getNombre().substring(1);
                        nombreCompleto = letraInicial + resto;
                        writer.append("\t\t" + entidad.getNombre().toLowerCase() + ".set" + nombreCompleto + "(" + entidad.getNombre().toLowerCase() + "Dto.get" + nombreCompleto + "());\n");
                    }
                }
                for (Relacion relacion : relaciones) {
                    if (relacion.getA() == entidad) {
                        if (relacion.getCardinalityA().equals("N")) {
                            if (relacion.getCardinalityB().equals("M")) { //ManyToMany
                                writer.append("\t\t" + entidad.getNombre().toLowerCase() + ".set" + relacion.getB().getNombre() + "s(" + relacion.getB().getNombre().toLowerCase() + "s);\n");
                            } else if (relacion.getCardinalityB().equals("0..1")) {    //ManyToOne
                                writer.append("\t\t" + entidad.getNombre().toLowerCase() + ".set" + relacion.getB().getNombre() + "(" + relacion.getB().getNombre().toLowerCase() + ");\n");
                            }
                        } else if (relacion.getCardinalityA().equals("0..1") && relacion.getCardinalityB().equals("0..1")) {   //OneToOne
                            writer.append("\t\t" + entidad.getNombre().toLowerCase() + ".set" + relacion.getB().getNombre() + "(" + relacion.getB().getNombre().toLowerCase() + ");\n");
                        }
                    }
                }

                writer.append("\t\t" + entidad.getNombre().toLowerCase() + ".setUser(user);\n\n");

                writer.append("\t\t" + entidad.getNombre().toLowerCase() + "Repository.save(" + entidad.getNombre().toLowerCase() + ");\n\n");
                writer.append("\t\treturn \"redirect:/" + entidad.getNombre() + "?successPut\";\n\t}\n\n");


                //Operation DELETE

                writer.append("\t//Operation DELETE\n\n");

                writer.append("\t@GetMapping(\"/Delete\")\n");
                writer.append("\tpublic String operationDelete" + entidad.getNombre() + "(Model model){\n\n");
                writer.append("\t\turlService.setUrl(\"/" + entidad.getNombre() + "/Delete\");\n");
                writer.append("\t\tmodel.addAttribute(\"listAll\", " + entidad.getNombre().toLowerCase() + "Repository.findAll());\n\n");
                writer.append("\t\treturn \"/" + entidad.getNombre() + "/operationDelete/operationDelete" + entidad.getNombre() + "\";\n\t}\n\n");

                writer.append("\t@PostMapping(\"/Delete/{id}\")\n");
                writer.append("\tpublic String operationDelete(@PathVariable(\"id\") long id){\n\n");
                writer.append("\t\ttry {\n");
                writer.append("\t\t\t" + entidad.getNombre().toLowerCase() + "Repository.deleteById(id);\n" +
                        "\t\t}catch (Exception ex){\n" +
                        "\t\t\treturn \"redirect:/" + entidad.getNombre() + "?failDelete\";\n\t\t}\n\n");
                writer.append("\t\treturn \"redirect:/" + entidad.getNombre() + "?successDelete\";\n\t}\n\n\n");


                //EOF
                writer.append("}");
            }else{
                f = new File(this.src + "/controller/" + entidad.getNombre() + "Controller.java");
                writer = new FileWriter(f);

                //Package e Imports
                writer.append("package " + this.pathCode + ".controller;\n\n");

                writer.append("import org.springframework.beans.factory.annotation.Autowired;\n");
                writer.append("import org.springframework.stereotype.Controller;\n");
                writer.append("import org.springframework.ui.Model;\n");
                writer.append("import org.springframework.web.bind.annotation.*;\n");
                writer.append("import org.springframework.security.core.Authentication;\n");
                writer.append("import org.springframework.security.core.context.SecurityContextHolder;\n");
                writer.append("import java.util.*;\n");
                writer.append("import " + this.pathCode + ".service.UrlService;\n");
                writer.append("import " + this.pathCode + ".model." + entidad.getNombre() + ";\n");
                writer.append("import " + this.pathCode + ".dto." + entidad.getNombre() + "Dto;\n");
                writer.append("import " + this.pathCode + ".repository." + entidad.getNombre() + "Repository;\n");
                for (Relacion relacion : relaciones) {
                    if (relacion.getA() != entidad) {
                        writer.append("import " + this.pathCode + ".model." + relacion.getNameA() + ";\n");
                        writer.append("import " + this.pathCode + ".dto." + relacion.getNameA() + "Dto;\n");
                        writer.append("import " + this.pathCode + ".repository." + relacion.getNameA() + "Repository;\n");
                    } else if (relacion.getB() != entidad) {
                        writer.append("import " + this.pathCode + ".model." + relacion.getNameB() + ";\n");
                        writer.append("import " + this.pathCode + ".dto." + relacion.getNameB() + "Dto;\n");
                        writer.append("import " + this.pathCode + ".repository." + relacion.getNameB() + "Repository;\n");
                    }
                }
                writer.append("import " + this.pathCode + ".model.User;\n" +
                        "import " + this.pathCode + ".repository.UserRepository;\n\n\n");

                //Class
                writer.append("@Controller\n");
                writer.append("@RequestMapping(\"/" + entidad.getNombre() + "\")\n");
                writer.append("public class " + entidad.getNombre() + "Controller {\n\n");

                writer.append("\t@Autowired\n");
                writer.append("\tprivate " + entidad.getNombre() + "Repository " + entidad.getNombre().toLowerCase() + "Repository;\n\n");
                for (Relacion relacion : relaciones) {
                    if (relacion.getA() == entidad) {
                        writer.append("\t@Autowired\n");
                        writer.append("\tprivate " + relacion.getNameB() + "Repository " + relacion.getNameB().toLowerCase() + "Repository;\n\n");
                    }
                }

                writer.append("\t@Autowired\n" +
                        "\tprivate UserRepository userRepository;\n\n");

                writer.append("\t@Autowired\n");
                writer.append("\tprivate UrlService urlService;\n\n");

                //Operation GET
                writer.append("\t//Operation GET\n\n");
                writer.append("\t@GetMapping\n" +
                        "\tpublic String viewNotas(Model model){\n\n" +
                        "\t\turlService.setUrl(\"/" + entidad.getNombre() + "\");\n" +
                        "\t\tmodel.addAttribute(\"listAll\", " + entidad.getNombre().toLowerCase() + "Repository.findAll());\n\n" +
                        "\t\treturn \"" + entidad.getNombre() + "/view" + entidad.getNombre() + "\";\n" +
                        "\t}\n\n");

                //Operation POST
                writer.append("\t//Operation POST\n\n");

                writer.append("\t@GetMapping(\"/Post\")\n");
                writer.append("\tpublic String operationPost" + entidad.getNombre() + "(Model model){\n\n");
                writer.append("\t\turlService.setUrl(\"/" + entidad.getNombre() + "/Post\");\n");
                writer.append("\t\tmodel.addAttribute(\"object\", new " + entidad.getNombre() + "Dto());\n");
                for (Relacion relacion : relaciones) {
                    if (relacion.getA() == entidad) {
                        writer.append("\t\tmodel.addAttribute(\"list" + relacion.getNameB() + "\", " + relacion.getNameB().toLowerCase() + "Repository.findAll());\n");
                    }
                }
                writer.append("\n\t\treturn \"/" + entidad.getNombre() + "/post" + entidad.getNombre() + "\";\n\t}\n\n");

                writer.append("\t@PostMapping(\"/Post\")\n");
                writer.append("\tpublic String operationPost(" + entidad.getNombre() + "Dto " + entidad.getNombre().toLowerCase() + "Dto){\n\n");

                for (Relacion relacion : relaciones) {

                    if (relacion.getA() == entidad) {
                        if (relacion.getCardinalityA().equals("N")) {
                            if (relacion.getCardinalityB().equals("M")) { //ManyToMany
                                writer.append("\t\t//Relation - ManyToMany with " + relacion.getB().getNombre() + "\n");
                                writer.append("\t\tSet<" + relacion.getB().getNombre() + "> " + relacion.getB().getNombre().toLowerCase() + "s = new HashSet<" + relacion.getB().getNombre() + ">();\n");
                                writer.append("\t\tfor(String " + relacion.getB().getNombre().toLowerCase() + "DtoId : " + entidad.getNombre().toLowerCase() + "Dto.get" + relacion.getB().getNombre() + "Dto()){\n");
                                writer.append("\t\t\t" + relacion.getB().getNombre().toLowerCase() + "s.add(" + relacion.getB().getNombre().toLowerCase() + "Repository.findById(Long.parseLong("
                                        + relacion.getB().getNombre().toLowerCase() + "DtoId)));\n\t\t}\n\n");

                            } else if (relacion.getCardinalityB().equals("0..1")) {    //ManyToOne
                                writer.append("\t\t//Relation - ManyToOne with " + relacion.getB().getNombre() + "\n");
                                writer.append("\t\t" + relacion.getB().getNombre() + " " + relacion.getB().getNombre().toLowerCase() + " = " +
                                        relacion.getB().getNombre().toLowerCase() + "Repository.findById(" +
                                        entidad.getNombre().toLowerCase() + "Dto.get" + relacion.getB().getNombre() + "Dto().getId_" + relacion.getB().getNombre().toLowerCase() + "());\n\n");
                            }
                        } else if (relacion.getCardinalityA().equals("0..1") && relacion.getCardinalityB().equals("0..1")) {   //OneToOne
                            writer.append("\t\t//Relation - OneToOne with " + relacion.getB().getNombre() + "\n");
                            writer.append("\t\tif(" + entidad.getNombre().toLowerCase() + "Repository.findBy" + relacion.getB().getNombre() + "Id_" + relacion.getB().getNombre().toLowerCase() + "(" +
                                    entidad.getNombre().toLowerCase() + "Dto.get" + relacion.getB().getNombre() + "Dto().getId()) != null){\n");
                            writer.append("\t\t\treturn \"redirect:/" + entidad.getNombre() + "?failPost\";\n\t\t}\n");
                            writer.append("\t\t" + relacion.getB().getNombre() + " " + relacion.getB().getNombre().toLowerCase() + " = " + relacion.getB().getNombre().toLowerCase() + "Repository.findById(" +
                                    entidad.getNombre().toLowerCase() + "Dto.get" + relacion.getB().getNombre() + "Dto().getId());\n\n");
                        }
                    }
                }

                writer.append("\t\tAuthentication auth = SecurityContextHolder.getContext().getAuthentication();\n" +
                        "\t\torg.springframework.security.core.userdetails.User userConnected = (org.springframework.security.core.userdetails.User) auth.getPrincipal();\n" +
                        "\t\tUser user = userRepository.findByUsername(userConnected.getUsername());\n\n");

                writer.append("\t\t" + entidad.getNombre() + " " + entidad.getNombre().toLowerCase() + " = new " + entidad.getNombre() + "();\n");
                String letraInicial, resto, nombreCompleto = null;
                for (AtributoDto atr : entidad.getAtributos()) {
                    if (!atr.isPrimaryKey()) {
                        letraInicial = atr.getNombre().substring(0, 1).toUpperCase();
                        resto = atr.getNombre().substring(1);
                        nombreCompleto = letraInicial + resto;
                        writer.append("\t\t" + entidad.getNombre().toLowerCase() + ".set" + nombreCompleto + "(" + entidad.getNombre().toLowerCase() + "Dto.get" + nombreCompleto + "());\n");
                    }
                }
                for (Relacion relacion : relaciones) {
                    if (relacion.getA() == entidad) {
                        if (relacion.getCardinalityA().equals("N")) {
                            if (relacion.getCardinalityB().equals("M")) { //ManyToMany
                                writer.append("\t\tfor(" + relacion.getB().getNombre() + " " + relacion.getB().getNombre().toLowerCase() + " : " + relacion.getB().getNombre().toLowerCase() + "s){\n");
                                writer.append("\t\t\t" + entidad.getNombre().toLowerCase() + ".add" + relacion.getB().getNombre() + "(" + relacion.getB().getNombre().toLowerCase() + ");\n\t\t}\n");
                            } else if (relacion.getCardinalityB().equals("0..1")) {    //ManyToOne
                                writer.append("\t\t" + entidad.getNombre().toLowerCase() + ".set" + relacion.getB().getNombre() + "(" + relacion.getB().getNombre().toLowerCase() + ");\n");
                            }
                        } else if (relacion.getCardinalityA().equals("0..1") && relacion.getCardinalityB().equals("0..1")) {   //OneToOne
                            writer.append("\t\t" + entidad.getNombre().toLowerCase() + ".set" + relacion.getB().getNombre() + "(" + relacion.getB().getNombre().toLowerCase() + ");\n");
                        }
                    }
                }
                writer.append("\t\t" + entidad.getNombre().toLowerCase() + ".setUser(user);\n\n");
                writer.append("\t\t" + entidad.getNombre().toLowerCase() + "Repository.save(" + entidad.getNombre().toLowerCase() + ");\n\n");
                writer.append("\t\treturn \"redirect:/" + entidad.getNombre() + "?successPost\";\n\t}\n\n");


                //Operation PUT
                writer.append("\t//Operation PUT\n\n");

                writer.append("\t@GetMapping(\"/Put/{id}\")\n");
                writer.append("\tpublic String operationPut" + entidad.getNombre() + "(@PathVariable(\"id\") long id, Model model){\n\n");
                writer.append("\t\t" + entidad.getNombre() + " " + entidad.getNombre().toLowerCase() + " = " + entidad.getNombre().toLowerCase() + "Repository.findById(id);\n");
                writer.append("\t\t" + entidad.getNombre() + "Dto " + entidad.getNombre().toLowerCase() + "Dto = new " + entidad.getNombre() + "Dto();\n\n");
                for (AtributoDto atr : entidad.getAtributos()) {
                    letraInicial = atr.getNombre().substring(0, 1).toUpperCase();
                    resto = atr.getNombre().substring(1);
                    nombreCompleto = letraInicial + resto;
                    writer.append("\t\t" + entidad.getNombre().toLowerCase() + "Dto.set" + nombreCompleto + "(" + entidad.getNombre().toLowerCase() + ".get" + nombreCompleto + "());\n");
                }

                writer.append("\n");
                for (Relacion relacion : relaciones) {
                    if (relacion.getA() == entidad) {
                        if (relacion.getCardinalityA().equals("N")) {
                            if (relacion.getCardinalityB().equals("M")) { //ManyToMany
                                writer.append("\t\tString[] " + relacion.getB().getNombre().toLowerCase() + "Dto = new String[]{};\n");
                                writer.append("\t\t" + entidad.getNombre().toLowerCase() + "Dto.set" + relacion.getB().getNombre() + "Dto(" + relacion.getB().getNombre().toLowerCase() + "Dto);\n");
                                writer.append("\t\tmodel.addAttribute(\"list" + relacion.getB().getNombre() + "\", " + relacion.getB().getNombre().toLowerCase() + "Repository.findAll());\n\n");
                            } else if (relacion.getCardinalityB().equals("0..1")) {    //ManyToOne
                                writer.append("\t\t" + relacion.getB().getNombre() + "Dto " + relacion.getB().getNombre().toLowerCase() + "Dto = new " + relacion.getB().getNombre() + "Dto();\n");
                                writer.append("\t\t" + relacion.getB().getNombre().toLowerCase() + "Dto.setId_" + relacion.getB().getNombre().toLowerCase() + "(" + entidad.getNombre().toLowerCase() + ".get" + relacion.getB().getNombre() + "().getId_" + relacion.getB().getNombre().toLowerCase() + "());\n");
                                writer.append("\t\t" + entidad.getNombre().toLowerCase() + "Dto.set" + relacion.getB().getNombre() + "Dto(" + relacion.getB().getNombre().toLowerCase() + "Dto);\n");
                                writer.append("\t\tmodel.addAttribute(\"list" + relacion.getB().getNombre() + "\", " + relacion.getB().getNombre().toLowerCase() + "Repository.findAll());\n\n");
                            }
                        } else if (relacion.getCardinalityA().equals("0..1") && relacion.getCardinalityB().equals("0..1")) {   //OneToOne
                            writer.append("\t\t" + entidad.getNombre() + "Dto " + relacion.getB().getNombre().toLowerCase() + "Dto = new " + relacion.getB().getNombre() + "Dto();\n");
                            writer.append("\t\t" + relacion.getB().getNombre().toLowerCase() + "Dto.setId(" + entidad.getNombre().toLowerCase() + ".get" + relacion.getB().getNombre() + "().getId_" + relacion.getB().getNombre().toLowerCase() + "());\n");
                            writer.append("\t\t" + entidad.getNombre().toLowerCase() + "Dto.set" + relacion.getB().getNombre() + "Dto(" + relacion.getB().getNombre().toLowerCase() + "Dto);\n");
                            writer.append("\t\tmodel.addAttribute(\"list" + relacion.getB().getNombre() + "\", " + relacion.getB().getNombre().toLowerCase() + "Repository.findAll());\n\n");
                        }
                    }
                }

                writer.append("\t\tmodel.addAttribute(\"object\", " + entidad.getNombre().toLowerCase() + "Dto);\n\n");

                writer.append("\t\treturn \"/" + entidad.getNombre() + "/put" + entidad.getNombre() + "\";\n\t}\n\n");

                writer.append("\t@PostMapping(\"/Put/{id}\")\n");
                writer.append("\tpublic String operationPut(@PathVariable(\"id\") long id, " + entidad.getNombre() + "Dto " + entidad.getNombre().toLowerCase() + "Dto){\n\n");
                for (Relacion relacion : relaciones) {
                    if (relacion.getA() == entidad) {
                        if (relacion.getCardinalityA().equals("N")) {
                            if (relacion.getCardinalityB().equals("M")) { //ManyToMany
                                writer.append("\t\t//Relation - ManyToMany with " + relacion.getB().getNombre() + "\n");
                                writer.append("\t\tSet<" + relacion.getB().getNombre() + "> " + relacion.getB().getNombre().toLowerCase() + "s = new HashSet<" + relacion.getB().getNombre() + ">();\n");
                                writer.append("\t\tfor(String " + relacion.getB().getNombre().toLowerCase() + "DtoId : " + entidad.getNombre().toLowerCase() + "Dto.get" + relacion.getB().getNombre() + "Dto()){\n");
                                writer.append("\t\t\t" + relacion.getB().getNombre().toLowerCase() + "s.add(" + relacion.getB().getNombre().toLowerCase() + "Repository.findById(Long.parseLong("
                                        + relacion.getB().getNombre().toLowerCase() + "DtoId)));\n\t\t}\n\n");

                            } else if (relacion.getCardinalityB().equals("0..1")) {    //ManyToOne
                                writer.append("\t\t//Relation - ManyToOne with " + relacion.getB().getNombre() + "\n");
                                writer.append("\t\t" + relacion.getB().getNombre() + " " + relacion.getB().getNombre().toLowerCase() + " = " +
                                        relacion.getB().getNombre().toLowerCase() + "Repository.findById(" +
                                        entidad.getNombre().toLowerCase() + "Dto.get" + relacion.getB().getNombre() + "Dto().getId_" + relacion.getB().getNombre().toLowerCase() + "());\n\n");
                            }
                        } else if (relacion.getCardinalityA().equals("0..1") && relacion.getCardinalityB().equals("0..1")) {   //OneToOne
                            writer.append("\t\t//Relation - OneToOne with " + relacion.getB().getNombre() + "\n");
                            writer.append("\t\tif(" + entidad.getNombre().toLowerCase() + "Repository.findBy" + relacion.getB().getNombre() + "Id(" +
                                    entidad.getNombre().toLowerCase() + "Dto.get" + relacion.getB().getNombre() + "Dto().getId()) != null){\n");
                            writer.append("\t\t\treturn \"redirect:/" + entidad.getNombre() + "?failPut\";\n\t\t}\n");
                            writer.append("\t\t" + relacion.getB().getNombre() + " " + relacion.getB().getNombre().toLowerCase() + " = " + relacion.getB().getNombre().toLowerCase() + "Repository.findById(" +
                                    entidad.getNombre().toLowerCase() + "Dto.get" + relacion.getB().getNombre() + "Dto().getId_" + relacion.getB().getNombre().toLowerCase() + "());\n\n");
                        }
                    }
                }

                writer.append("\t\tAuthentication auth = SecurityContextHolder.getContext().getAuthentication();\n" +
                        "\t\torg.springframework.security.core.userdetails.User userConnected = (org.springframework.security.core.userdetails.User) auth.getPrincipal();\n" +
                        "\t\tUser user = userRepository.findByUsername(userConnected.getUsername());\n\n");

                writer.append("\t\t" + entidad.getNombre().toLowerCase() + "Repository.deleteById(id);\n\n");
                writer.append("\t\t" + entidad.getNombre() + " " + entidad.getNombre().toLowerCase() + " = new " + entidad.getNombre() + "();\n");
                for (AtributoDto atr : entidad.getAtributos()) {
                    if (!atr.isPrimaryKey()) {
                        letraInicial = atr.getNombre().substring(0, 1).toUpperCase();
                        resto = atr.getNombre().substring(1);
                        nombreCompleto = letraInicial + resto;
                        writer.append("\t\t" + entidad.getNombre().toLowerCase() + ".set" + nombreCompleto + "(" + entidad.getNombre().toLowerCase() + "Dto.get" + nombreCompleto + "());\n");
                    }
                }
                for (Relacion relacion : relaciones) {
                    if (relacion.getA() == entidad) {
                        if (relacion.getCardinalityA().equals("N")) {
                            if (relacion.getCardinalityB().equals("M")) { //ManyToMany
                                writer.append("\t\t" + entidad.getNombre().toLowerCase() + ".set" + relacion.getB().getNombre() + "s(" + relacion.getB().getNombre().toLowerCase() + "s);\n");
                            } else if (relacion.getCardinalityB().equals("0..1")) {    //ManyToOne
                                writer.append("\t\t" + entidad.getNombre().toLowerCase() + ".set" + relacion.getB().getNombre() + "(" + relacion.getB().getNombre().toLowerCase() + ");\n");
                            }
                        } else if (relacion.getCardinalityA().equals("0..1") && relacion.getCardinalityB().equals("0..1")) {   //OneToOne
                            writer.append("\t\t" + entidad.getNombre().toLowerCase() + ".set" + relacion.getB().getNombre() + "(" + relacion.getB().getNombre().toLowerCase() + ");\n");
                        }
                    }
                }

                writer.append("\t\t" + entidad.getNombre().toLowerCase() + ".setUser(user);\n\n");

                writer.append("\t\t" + entidad.getNombre().toLowerCase() + "Repository.save(" + entidad.getNombre().toLowerCase() + ");\n\n");
                writer.append("\t\treturn \"redirect:/" + entidad.getNombre() + "?successPut\";\n\t}\n\n");


                //Operation DELETE

                writer.append("\t//Operation DELETE\n\n");

                writer.append("\t@PostMapping(\"/Delete/{id}\")\n");
                writer.append("\tpublic String operationDelete(@PathVariable(\"id\") long id){\n\n");
                writer.append("\t\ttry {\n");
                writer.append("\t\t\t" + entidad.getNombre().toLowerCase() + "Repository.deleteById(id);\n" +
                        "\t\t}catch (Exception ex){\n" +
                        "\t\t\treturn \"redirect:/" + entidad.getNombre() + "?failDelete\";\n\t\t}\n\n");
                writer.append("\t\treturn \"redirect:/" + entidad.getNombre() + "?successDelete\";\n\t}\n\n\n");

                //EOF
                writer.append("}");
            }

        }catch (Exception ex){
            throw new ControllerException(ex.getMessage());
        }finally {
            try {
                if(writer != null) {
                    writer.close();
                }
            }catch (Exception ex){
                System.out.println(ex.getMessage());
            }
        }

    }


    /**
     * Genera los documentos HTML correspondientes a la entidad que se pasa como parámetro.
     * @param entidad Entidad
     * @param relaciones Relaciones de la entidad
     * @throws HtmlException Excepción
     */
    private void generateHtml(EntidadDto entidad, List<Relacion> relaciones, boolean vistaCompacta) throws HtmlException{

        File f = null;
        FileWriter writer = null;

        try {
            //Carpeta principal de templates de la entidad
            f = new File(this.resources + "/templates/" + entidad.getNombre());
            if(!f.mkdir()){
                throw new HtmlException("Error al crear la carpeta principal de templates de la entidad: " + entidad.getNombre());
            }

            if(!vistaCompacta) {
                //Subcarpetas de la entidad
                f = new File(this.resources + "/templates/" + entidad.getNombre() + "/operationGet");
                if (!f.mkdir()) {
                    throw new HtmlException("Error al crear la subcarpeta GET de templates de la entidad: " + entidad.getNombre());
                }

                f = new File(this.resources + "/templates/" + entidad.getNombre() + "/operationPost");
                if (!f.mkdir()) {
                    throw new HtmlException("Error al crear la subcarpeta POST de templates de la entidad: " + entidad.getNombre());
                }

                f = new File(this.resources + "/templates/" + entidad.getNombre() + "/operationPut");
                if (!f.mkdir()) {
                    throw new HtmlException("Error al crear la subcarpeta PUT de templates de la entidad: " + entidad.getNombre());
                }

                f = new File(this.resources + "/templates/" + entidad.getNombre() + "/operationDelete");
                if (!f.mkdir()) {
                    throw new HtmlException("Error al crear la subcarpeta DELETE de templates de la entidad: " + entidad.getNombre());
                }
            }

            if(!vistaCompacta) {
                //principal{entidad}.html - Path: /templates/{entidad}/principal{entidad}.html
                f = new File(this.resources + "/templates/" + entidad.getNombre() + "/principal" + entidad.getNombre() + ".html");
                writer = new FileWriter(f);

                writer.append("<!DOCTYPE html>\n");
                writer.append("<html th:lang=\"#{principal.lang}\"\n" +
                        "      xmlns:th=\"http://www.thymeleaf.org\"\n" +
                        "      xmlns:layout=\"http://www.ultraq.net.nz/thymeleaf/layout\"\n" +
                        "      xmlns:sec=\"http://www.w3.org/1999/xhtml\"\n" +
                        "      layout:decorate=\"~{layout}\">\n");

                writer.append("<head>\n" +
                        "    <meta th:charset=\"#{principal.charset}\">\n" +
                        "    <title th:text=\"#{principal.title}\"></title>\n" +
                        "</head>");

                writer.append("<body>\n");
                writer.append("\t<div class=\"container py-4\" layout:fragment=\"header\">\n");
                writer.append("\t\t<h1 class=\"display-1\">" + entidad.getNombre() + "</h1>\n");
                writer.append("\t</div>\n\n");

                writer.append("\t<div class=\"container py-4\" layout:fragment=\"content\">\n\n");
                writer.append("\t\t<div th:if=\"${param.successPost}\"><span class=\"alert alert-info\" th:text=\"'Operation succeded (Creation)'\"></span></div>\n");
                writer.append("\t\t<div th:if=\"${param.failPost}\"><span class=\"alert alert-danger\" th:text=\"'Operation failed (Creation)'\"></span></div>\n");
                writer.append("\t\t<div th:if=\"${param.successPut}\"><span class=\"alert alert-info\" th:text=\"'Operation succeded (Update)'\"></span></div>\n");
                writer.append("\t\t<div th:if=\"${param.failPut}\"><span class=\"alert alert-danger\" th:text=\"'Operation failed (Update)'\"></span></div>\n");
                writer.append("\t\t<div th:if=\"${param.successDelete}\"><span class=\"alert alert-info\" th:text=\"'Operation succeded (Delete)'\"></span></div>\n");
                writer.append("\t\t<div th:if=\"${param.failDelete}\"><span class=\"alert alert-danger\" th:text=\"${'Operation fail (Delete)'}\"></span></div>\n\n");

                writer.append("\t\t<div class=\"row mt-5\">\n\n");
                for (RoleDto role : entidad.getPermisosRoles().getOperationGET()) {
                    writer.append("\t\t\t<!-- Operation GET card - Role " + role.getRoleName().toUpperCase() + " -->\n");
                    writer.append("\t\t\t<div class=\"col-3\" sec:authorize=\"hasRole('" + role.getRoleName() + "')\">\n");
                    writer.append("\t\t\t\t<div class=\"container\">\n");
                    writer.append("\t\t\t\t\t<div class=\"card py-4\" style=\"width: 18rem;\">\n");
                    writer.append("\t\t\t\t\t\t<img class=\"card-img-top\" th:src=\"@{/images/CRUDoperations/get.png}\" alt=\"\">\n");
                    writer.append("\t\t\t\t\t\t<div class=\"card-body\">\n");
                    writer.append("\t\t\t\t\t\t\t<h5 class=\"card-title mt-2\" th:text=\"#{get.button}\"></h5>\n");
                    writer.append("\t\t\t\t\t\t\t<a class=\"btn btn-primary mt-2\" th:text=\"#{get.button} + ' " + entidad.getNombre() + "'\" th:href=\"@{/" + entidad.getNombre() + "/Get}\"></a>\n");
                    writer.append("\t\t\t\t\t\t</div>\n\t\t\t\t\t</div>\n\t\t\t\t</div>\n\t\t\t</div>\n\n");
                }


                for (RoleDto role : entidad.getPermisosRoles().getOperationPOST()) {
                    writer.append("\t\t\t<!-- Operation POST card - Role " + role.getRoleName().toUpperCase() + " -->\n");
                    writer.append("\t\t\t<div class=\"col-3\" sec:authorize=\"hasRole('" + role.getRoleName() + "')\">\n");
                    writer.append("\t\t\t\t<div class=\"container\">\n");
                    writer.append("\t\t\t\t\t<div class=\"card py-4\" style=\"width: 18rem;\">\n");
                    writer.append("\t\t\t\t\t\t<img class=\"card-img-top\" th:src=\"@{/images/CRUDoperations/post.png}\" alt=\"\">\n");
                    writer.append("\t\t\t\t\t\t<div class=\"card-body\">\n");
                    writer.append("\t\t\t\t\t\t\t<h5 class=\"card-title mt-2\" th:text=\"#{post.button}\"></h5>\n");
                    writer.append("\t\t\t\t\t\t\t<a class=\"btn btn-primary mt-2\" th:text=\"#{post.button} + ' " + entidad.getNombre() + "'\" th:href=\"@{/" + entidad.getNombre() + "/Post}\"></a>\n");
                    writer.append("\t\t\t\t\t\t</div>\n\t\t\t\t\t</div>\n\t\t\t\t</div>\n\t\t\t</div>\n\n");
                }

                for (RoleDto role : entidad.getPermisosRoles().getOperationPUT()) {
                    writer.append("\t\t\t<!-- Operation PUT card - Role " + role.getRoleName().toUpperCase() + " -->\n");
                    writer.append("\t\t\t<div class=\"col-3\" sec:authorize=\"hasRole('" + role.getRoleName() + "')\">\n");
                    writer.append("\t\t\t\t<div class=\"container\">\n");
                    writer.append("\t\t\t\t\t<div class=\"card py-4\" style=\"width: 18rem;\">\n");
                    writer.append("\t\t\t\t\t\t<img class=\"card-img-top\" th:src=\"@{/images/CRUDoperations/put.png}\" alt=\"\">\n");
                    writer.append("\t\t\t\t\t\t<div class=\"card-body\">\n");
                    writer.append("\t\t\t\t\t\t\t<h5 class=\"card-title mt-2\" th:text=\"#{put.button}\"></h5>\n");
                    writer.append("\t\t\t\t\t\t\t<a class=\"btn btn-primary mt-2\" th:text=\"#{put.button} + ' " + entidad.getNombre() + "'\" th:href=\"@{/" + entidad.getNombre() + "/Put}\"></a>\n");
                    writer.append("\t\t\t\t\t\t</div>\n\t\t\t\t\t</div>\n\t\t\t\t</div>\n\t\t\t</div>\n\n");
                }

                for (RoleDto role : entidad.getPermisosRoles().getOperationDELETE()) {
                    writer.append("\t\t\t<!-- Operation DELETE card - Role " + role.getRoleName().toUpperCase() + " -->\n");
                    writer.append("\t\t\t<div class=\"col-3\" sec:authorize=\"hasRole('" + role.getRoleName() + "')\">\n");
                    writer.append("\t\t\t\t<div class=\"container\">\n");
                    writer.append("\t\t\t\t\t<div class=\"card py-4\" style=\"width: 18rem;\">\n");
                    writer.append("\t\t\t\t\t\t<img class=\"card-img-top\" th:src=\"@{/images/CRUDoperations/delete.png}\" alt=\"\">\n");
                    writer.append("\t\t\t\t\t\t<div class=\"card-body\">\n");
                    writer.append("\t\t\t\t\t\t\t<h5 class=\"card-title mt-2\" th:text=\"#{delete.button}\"></h5>\n");
                    writer.append("\t\t\t\t\t\t\t<a class=\"btn btn-primary mt-2\" th:text=\"#{delete.button} + ' " + entidad.getNombre() + "'\" th:href=\"@{/" + entidad.getNombre() + "/Delete}\"></a>\n");
                    writer.append("\t\t\t\t\t\t</div>\n\t\t\t\t\t</div>\n\t\t\t\t</div>\n\t\t\t</div>\n\n");
                }

                writer.append("\t\t</div>\n\n" +
                        "\t\t<div class=\"container row mt-5\">\n" +
                        "\t\t\t<div class=\"col-11\"></div>\n" +
                        "\t\t\t<div class=\"col-1\"><a role=\"button\" class=\"btn btn-danger\" th:href=\"@{/index}\" th:text=\"#{principal.back}\"></a></div>\n" +
                        "\t\t</div>\n" +
                        "\t</div>\n\n");
                writer.append("</body>\n</html>");

                writer.close();

                //operationGet{entidad}.html - Path: /templates/{entidad}/operationGet/operationGet{entidad}.html
                f = new File(this.resources + "/templates/" + entidad.getNombre() + "/operationGet/operationGet" + entidad.getNombre() + ".html");
                writer = new FileWriter(f);

                writer.append("<!DOCTYPE html>\n");
                writer.append("<html th:lang=\"#{principal.lang}\"\n" +
                        "      xmlns:th=\"http://www.thymeleaf.org\"\n" +
                        "      xmlns:layout=\"http://www.ultraq.net.nz/thymeleaf/layout\"\n" +
                        "      layout:decorate=\"~{layout}\">\n");

                writer.append("<head>\n" +
                        "    <meta th:charset=\"#{principal.charset}\">\n" +
                        "    <title th:text=\"#{principal.title}\"></title>\n" +
                        "</head>\n");

                writer.append("<body>\n\n");
                writer.append("\t<div class=\"container justify-content-center\" layout:fragment=\"header\">\n");
                writer.append("\t\t<h1 th:text=\"'" + entidad.getNombre() + "'\"></h1>\n\t</div>\n\n");

                writer.append("\t<div class=\"container\" layout:fragment=\"content\">\n");
                writer.append("\t\t<table class=\"table table-responsive\">\n");
                writer.append("\t\t\t<thead>\n\t\t\t\t<tr>\n");
                for (AtributoDto atr : entidad.getAtributos()) {
                    writer.append("\t\t\t\t\t<td><strong th:text=\"'" + atr.getNombre() + "'\"></strong></td>\n");
                }
                for (Relacion relacion : relaciones) {
                    if (relacion.getA() == entidad) {
                        writer.append("\t\t\t\t\t<td><strong th:text=\"'Id_" + relacion.getNameB().toLowerCase() + "'\"></strong></td>\n");
                    }
                }
                writer.append("\t\t\t\t\t<td><strong th:text=\"'User_id'\"></strong></td>\n");
                writer.append("\t\t\t\t</tr>\n\t\t\t</thead>\n");
                writer.append("\t\t\t<tbody>\n");
                writer.append("\t\t\t\t<tr th:each=\"object : ${listAll}\">\n");
                for (AtributoDto atr : entidad.getAtributos()) {
                    writer.append("\t\t\t\t\t<td th:text=\"${object." + atr.getNombre() + "}\"></td>\n");
                }
                for (Relacion relacion : relaciones) {
                    if (relacion.getA() == entidad) {
                        if (relacion.getCardinalityA().equals("N") && relacion.getCardinalityB().equals("M")) {   //ManyToMany
                            writer.append("\t\t\t\t\t<td th:if=\"${object." + relacion.getNameB().toLowerCase() + "s}\"><p th:each=\"suboject : ${object." + relacion.getNameB().toLowerCase() + "s}\" th:text=\"${suboject.toString()}\"></p></td>\n");
                        } else {  //ManyToOne && OneToOne
                            writer.append("\t\t\t\t\t<td th:if=\"${object." + relacion.getNameB().toLowerCase() + ".id_" + relacion.getNameB().toLowerCase() + "}\" th:text=\"${object." + relacion.getNameB().toLowerCase() + ".id_" + relacion.getNameB().toLowerCase() + "}\"></td>\n");
                        }
                    }
                }
                writer.append("\t\t\t\t\t<td th:if=\"${object.user}\" th:text=\"${object.user}\"></td>\n");
                writer.append("\t\t\t\t</tr>\n\t\t\t</tbody>\n\t\t</table>\n\n");
                writer.append("\t\t<div class=\"row mt-4\">\n");
                writer.append("\t\t\t<div class=\"col-10\"></div>\n");
                writer.append("\t\t\t<div class=\"col-2\"><a role=\"button\" class=\"btn btn-danger\" th:href=\"@{/" + entidad.getNombre() + "}\" th:text=\"#{principal.back}\"></a></div>\n");
                writer.append("\t\t</div>\n\t</div>\n\n");
                writer.append("</body>\n</html>");

                writer.close();

                //operationPost{entidad}.html - Path: /templates/{entidad}/operationPost/operationPost{entidad}.html
                f = new File(this.resources + "/templates/" + entidad.getNombre() + "/operationPost/operationPost" + entidad.getNombre() + ".html");
                writer = new FileWriter(f);

                writer.append("<!DOCTYPE html>\n");
                writer.append("<html th:lang=\"#{principal.lang}\"\n" +
                        "      xmlns:th=\"http://www.thymeleaf.org\"\n" +
                        "      xmlns:layout=\"http://www.ultraq.net.nz/thymeleaf/layout\"\n" +
                        "      layout:decorate=\"~{layout}\">\n");

                writer.append("<head>\n" +
                        "    <meta th:charset=\"#{principal.charset}\">\n" +
                        "    <title th:text=\"#{principal.title}\"></title>\n" +
                        "</head>\n");

                writer.append("<body>\n\n");
                writer.append("\t<div class=\"container justify-content-center\" layout:fragment=\"header\">\n");
                writer.append("\t\t<h1 th:text=\"'" + entidad.getNombre() + "'\"></h1>\n\t</div>\n\n");

                writer.append("\t<div class=\"container\" layout:fragment=\"content\">\n\n");
                writer.append("\t\t<div class=\"container\">\n");
                writer.append("\t\t\t<form th:action=\"@{/" + entidad.getNombre() + "/Post}\" method=\"POST\" th:object=\"${object}\">\n\n");
                for (AtributoDto atr : entidad.getAtributos()) {
                    if (!atr.isPrimaryKey()) {
                        writer.append("\t\t\t\t<div class=\"input-group mt-5\">\n");
                        writer.append("\t\t\t\t\t<label for=\"" + atr.getNombre() + "\" class=\"control-label input-group-text\" th:text=\"'" + atr.getNombre() + "'\"></label>\n");
                        writer.append("\t\t\t\t\t<input id=\"" + atr.getNombre() + "\" class=\"form-control\" required th:field=\"*{" + atr.getNombre() + "}\">\n\t\t\t\t</div>\n\n");
                    }
                }
                int i = 1;
                for (Relacion relacion : relaciones) {
                    if (relacion.getA() == entidad) {
                        if (relacion.getCardinalityA().equals("N") && relacion.getCardinalityB().equals("M")) {   //ManyToMany
                            writer.append("\t\t\t\t<div class=\"form-floating mt-5\">\n");
                            writer.append("\t\t\t\t\t<select id=\"" + relacion.getNameB().toLowerCase() + "\" class=\"form-select\" multiple required th:field=\"*{" + relacion.getNameB().toLowerCase() + "Dto}\">\n");
                            writer.append("\t\t\t\t\t\t<option th:each=\"object" + i + " : ${list" + relacion.getNameB() + "}\" th:text=\"${object" + i + "}\" th:value=\"${object" + i + ".id_" + relacion.getNameB().toLowerCase() + "}\"></option>\n\t\t\t\t\t</select>\n");
                            writer.append("\t\t\t\t\t<label for=\"" + relacion.getNameB().toLowerCase() + "\" th:text=\"'" + relacion.getNameB().toLowerCase() + "'\"></label>\n\t\t\t\t</div>\n\n");
                        } else {  //ManyToOne && OneToOne
                            writer.append("\t\t\t\t<div class=\"form-floating mt-5\">\n");
                            writer.append("\t\t\t\t\t<select id=\"" + relacion.getNameB().toLowerCase() + "\" class=\"form-select\" required th:field=\"*{" + relacion.getNameB().toLowerCase() + "Dto.id_" + relacion.getNameB().toLowerCase() + "}\">\n");
                            writer.append("\t\t\t\t\t\t<option th:each=\"object" + i + " : ${list" + relacion.getNameB() + "}\" th:text=\"${object" + i + "}\" th:value=\"${object" + i + ".id_" + relacion.getNameB().toLowerCase() + "}\"></option>\n\t\t\t\t\t</select>\n");
                            writer.append("\t\t\t\t\t<label for=\"" + relacion.getNameB().toLowerCase() + "\" th:text=\"'" + relacion.getNameB().toLowerCase() + "'\"></label>\n\t\t\t\t</div>\n\n");
                        }
                    }
                    i++;
                }

                writer.append("\t\t\t\t<button type=\"submit\" class=\"btn btn-success mt-5\" th:text=\"#{principal.save}\"></button>\n\t\t\t</form>\n\t\t</div>\n\n");
                writer.append("\t\t<div class=\"row mt-4\">\n");
                writer.append("\t\t\t<div class=\"col-10\"></div>\n");
                writer.append("\t\t\t<div class=\"col-2\"><a role=\"button\" class=\"btn btn-danger\" th:href=\"@{/" + entidad.getNombre() + "}\" th:text=\"#{principal.back}\"></a></div>\n\t\t</div>\n\t</div>\n\n");
                writer.append("</body>\n</html>");

                writer.close();


                //operationPut{entidad}View.html - Path: /templates/{entidad}/operationPut/operationPut{entidad}View.html
                f = new File(this.resources + "/templates/" + entidad.getNombre() + "/operationPut/operationPut" + entidad.getNombre() + "View.html");
                writer = new FileWriter(f);

                writer.append("<!DOCTYPE html>\n");
                writer.append("<html th:lang=\"#{principal.lang}\"\n" +
                        "      xmlns:th=\"http://www.thymeleaf.org\"\n" +
                        "      xmlns:layout=\"http://www.ultraq.net.nz/thymeleaf/layout\"\n" +
                        "      layout:decorate=\"~{layout}\">\n");

                writer.append("<head>\n" +
                        "    <meta th:charset=\"#{principal.charset}\">\n" +
                        "    <title th:text=\"#{principal.title}\"></title>\n" +
                        "</head>\n");

                writer.append("<body>\n\n");
                writer.append("\t<div class=\"container justify-content-center\" layout:fragment=\"header\">\n");
                writer.append("\t\t<h1 th:text=\"'" + entidad.getNombre() + "'\"></h1>\n\t</div>\n\n");

                writer.append("\t<div class=\"container\" layout:fragment=\"content\">\n");
                writer.append("\t\t<table class=\"table table-responsive\">\n");
                writer.append("\t\t\t<thead>\n\t\t\t\t<tr>\n");
                for (AtributoDto atr : entidad.getAtributos()) {
                    writer.append("\t\t\t\t\t<td><strong th:text=\"'" + atr.getNombre() + "'\"></strong></td>\n");
                }
                for (Relacion relacion : relaciones) {
                    if (relacion.getA() == entidad) {
                        writer.append("\t\t\t\t\t<td><strong th:text=\"'id_" + relacion.getNameB().toLowerCase() + "'\"></strong></td>\n");
                    }

                }
                writer.append("\t\t\t\t\t<td><strong th:text=\"'User_id'\"></strong></td>\n");
                writer.append("\t\t\t\t\t<td></td>\n");
                writer.append("\t\t\t\t</tr>\n\t\t\t</thead>\n");
                writer.append("\t\t\t<tbody>\n");
                writer.append("\t\t\t\t<tr th:each=\"object : ${listAll}\">\n");
                for (AtributoDto atr : entidad.getAtributos()) {
                    writer.append("\t\t\t\t\t<td th:text=\"${object." + atr.getNombre() + "}\"></td>\n");
                }
                for (Relacion relacion : relaciones) {
                    if (relacion.getA() == entidad) {
                        if (relacion.getCardinalityA().equals("N") && relacion.getCardinalityB().equals("M")) {   //ManyToMany
                            writer.append("\t\t\t\t\t<td th:if=\"${object." + relacion.getNameB().toLowerCase() + "s}\"><p th:each=\"suboject : ${object." + relacion.getNameB().toLowerCase() + "s}\" th:text=\"${suboject.toString()}\"></p></td>\n");
                        } else {  //ManyToOne && OneToOne
                            writer.append("\t\t\t\t\t<td th:if=\"${object." + relacion.getNameB().toLowerCase() + ".id_" + relacion.getNameB().toLowerCase() + "}\" th:text=\"${object." + relacion.getNameB().toLowerCase() + ".id_" + relacion.getNameB().toLowerCase() + "}\"></td>\n");
                        }
                    }
                }
                writer.append("\t\t\t\t\t<td th:if=\"${object.user}\" th:text=\"${object.user}\"></td>\n");
                writer.append("\t\t\t\t\t<td><a role=\"button\" class=\"btn btn-info\" th:href=\"@{/" + entidad.getNombre() + "/Put/} + ${object.id_" + entidad.getNombre().toLowerCase() + "}\" th:text=\"'Edit'\"></a></td>\n");
                writer.append("\t\t\t\t</tr>\n\t\t\t</tbody>\n\t\t</table>\n\n");
                writer.append("\t\t<div class=\"row mt-4\">\n");
                writer.append("\t\t\t<div class=\"col-10\"></div>\n");
                writer.append("\t\t\t<div class=\"col-2\"><a role=\"button\" class=\"btn btn-danger\" th:href=\"@{/" + entidad.getNombre() + "}\" th:text=\"#{principal.back}\"></a></div>\n");
                writer.append("\t\t</div>\n\t</div>\n\n");
                writer.append("</body>\n</html>");

                writer.close();

                //operationPut{entidad}.html - Path: /templates/{entidad}/operationPut/operationPut{entidad}.html
                f = new File(this.resources + "/templates/" + entidad.getNombre() + "/operationPut/operationPut" + entidad.getNombre() + ".html");
                writer = new FileWriter(f);

                writer.append("<!DOCTYPE html>\n");
                writer.append("<html th:lang=\"#{principal.lang}\"\n" +
                        "      xmlns:th=\"http://www.thymeleaf.org\"\n" +
                        "      xmlns:layout=\"http://www.ultraq.net.nz/thymeleaf/layout\"\n" +
                        "      layout:decorate=\"~{layout}\">\n");

                writer.append("<head>\n" +
                        "    <meta th:charset=\"#{principal.charset}\">\n" +
                        "    <title th:text=\"#{principal.title}\"></title>\n" +
                        "</head>\n");

                writer.append("<body>\n\n");
                writer.append("\t<div class=\"container justify-content-center\" layout:fragment=\"header\">\n");
                writer.append("\t\t<h1 th:text=\"'" + entidad.getNombre() + "'\"></h1>\n\t</div>\n\n");

                writer.append("\t<div class=\"container\" layout:fragment=\"content\">\n\n");
                writer.append("\t\t<div class=\"container\">\n");
                writer.append("\t\t\t<form th:action=\"@{/" + entidad.getNombre() + "/Put/} + ${object.id_" + entidad.getNombre().toLowerCase() + "}\" method=\"POST\" th:object=\"${object}\">\n\n");
                for (AtributoDto atr : entidad.getAtributos()) {
                    if (!atr.isPrimaryKey()) {
                        writer.append("\t\t\t\t<div class=\"input-group mt-5\">\n");
                        writer.append("\t\t\t\t\t<label for=\"" + atr.getNombre() + "\" class=\"control-label input-group-text\" th:text=\"'" + atr.getNombre() + "'\"></label>\n");
                        writer.append("\t\t\t\t\t<input id=\"" + atr.getNombre() + "\" class=\"form-control\" required th:field=\"*{" + atr.getNombre() + "}\" th:value=\"*{" + atr.getNombre() + "}\">\n\t\t\t\t</div>\n\n");
                    } else {
                        writer.append("\t\t\t\t<div class=\"input-group mt-5\">\n");
                        writer.append("\t\t\t\t\t<label for=\"" + atr.getNombre() + "\" class=\"control-label input-group-text\" th:text=\"'" + atr.getNombre() + "'\"></label>\n");
                        writer.append("\t\t\t\t\t<input id=\"" + atr.getNombre() + "\" class=\"form-control\" required th:field=\"*{" + atr.getNombre() + "}\" disabled th:value=\"*{" + atr.getNombre() + "}\">\n\t\t\t\t</div>\n\n");
                    }
                }
                i = 1;
                for (Relacion relacion : relaciones) {
                    if (relacion.getA() == entidad) {
                        if (relacion.getCardinalityA().equals("N") && relacion.getCardinalityB().equals("M")) {   //ManyToMany
                            writer.append("\t\t\t\t<div class=\"form-floating mt-5\">\n");
                            writer.append("\t\t\t\t\t<select id=\"" + relacion.getNameB().toLowerCase() + "\" class=\"form-select\" multiple required th:field=\"*{" + relacion.getNameB().toLowerCase() + "Dto}\">\n");
                            writer.append("\t\t\t\t\t\t<option th:each=\"object" + i + " : ${list" + relacion.getNameB() + "}\" th:text=\"${object" + i + "}\" th:value=\"${object" + i + ".id_" + relacion.getNameB().toLowerCase() + "}\"></option>\n\t\t\t\t\t</select>\n");
                            writer.append("\t\t\t\t\t<label for=\"" + relacion.getNameB().toLowerCase() + "\" th:text=\"'" + relacion.getNameB().toLowerCase() + "'\"></label>\n\t\t\t\t</div>\n\n");
                        } else {  //ManyToOne && OneToOne
                            writer.append("\t\t\t\t<div class=\"form-floating mt-5\">\n");
                            writer.append("\t\t\t\t\t<select id=\"" + relacion.getNameB().toLowerCase() + "\" class=\"form-select\" required th:field=\"*{" + relacion.getNameB().toLowerCase() + "Dto.id_" + relacion.getNameB().toLowerCase() + "}\">\n");
                            writer.append("\t\t\t\t\t\t<option th:each=\"object" + i + " : ${list" + relacion.getNameB() + "}\" th:text=\"${object" + i + "}\" th:value=\"${object" + i + ".id_" + relacion.getNameB().toLowerCase() + "}\"></option>\n\t\t\t\t\t</select>\n");
                            writer.append("\t\t\t\t\t<label for=\"" + relacion.getNameB().toLowerCase() + "\" th:text=\"'" + relacion.getNameB().toLowerCase() + "'\"></label>\n\t\t\t\t</div>\n\n");
                        }
                    }
                    i++;
                }

                writer.append("\t\t\t\t<button type=\"submit\" class=\"btn btn-success mt-5\" th:text=\"#{principal.save}\"></button>\n\t\t\t</form>\n\t\t</div>\n\n");
                writer.append("\t\t<div class=\"row mt-4\">\n");
                writer.append("\t\t\t<div class=\"col-10\"></div>\n");
                writer.append("\t\t\t<div class=\"col-2\"><a role=\"button\" class=\"btn btn-danger\" th:href=\"@{/" + entidad.getNombre() + "}\" th:text=\"#{principal.back}\"></a></div>\n\t\t</div>\n\t</div>\n\n");
                writer.append("</body>\n</html>");

                writer.close();

                //operationDelete{entidad}.html - Path: /templates/{entidad}/operationDelete/operationDelete{entidad}.html
                f = new File(this.resources + "/templates/" + entidad.getNombre() + "/operationDelete/operationDelete" + entidad.getNombre() + ".html");
                writer = new FileWriter(f);

                writer.append("<!DOCTYPE html>\n");
                writer.append("<html th:lang=\"#{principal.lang}\"\n" +
                        "      xmlns:th=\"http://www.thymeleaf.org\"\n" +
                        "      xmlns:layout=\"http://www.ultraq.net.nz/thymeleaf/layout\"\n" +
                        "      layout:decorate=\"~{layout}\">\n");

                writer.append("<head>\n" +
                        "    <meta th:charset=\"#{principal.charset}\">\n" +
                        "    <title th:text=\"#{principal.title}\"></title>\n" +
                        "</head>\n");

                writer.append("<body>\n\n");
                writer.append("\t<div class=\"container justify-content-center\" layout:fragment=\"header\">\n");
                writer.append("\t\t<h1 th:text=\"'" + entidad.getNombre() + "'\"></h1>\n\t</div>\n\n");

                writer.append("\t<div class=\"container\" layout:fragment=\"content\">\n");
                writer.append("\t\t<table class=\"table table-responsive\">\n");
                writer.append("\t\t\t<thead>\n\t\t\t\t<tr>\n");
                for (AtributoDto atr : entidad.getAtributos()) {
                    writer.append("\t\t\t\t\t<td><strong th:text=\"'" + atr.getNombre() + "'\"></strong></td>\n");
                }
                for (Relacion relacion : relaciones) {
                    if (relacion.getA() == entidad) {
                        writer.append("\t\t\t\t\t<td><strong th:text=\"'id_" + relacion.getNameB().toLowerCase() + "'\"></strong></td>\n");
                    }

                }
                writer.append("\t\t\t\t\t<td><strong th:text=\"'User_id'\"></strong></td>\n");
                writer.append("\t\t\t\t\t<td></td>\n");
                writer.append("\t\t\t\t</tr>\n\t\t\t</thead>\n");
                writer.append("\t\t\t<tbody>\n");
                writer.append("\t\t\t\t<tr th:each=\"object : ${listAll}\">\n");
                for (AtributoDto atr : entidad.getAtributos()) {
                    writer.append("\t\t\t\t\t<td th:text=\"${object." + atr.getNombre() + "}\"></td>\n");
                }
                for (Relacion relacion : relaciones) {
                    if (relacion.getA() == entidad) {
                        if (relacion.getCardinalityA().equals("N") && relacion.getCardinalityB().equals("M")) {   //ManyToMany
                            writer.append("\t\t\t\t\t<td th:if=\"${object." + relacion.getNameB().toLowerCase() + "s}\"><p th:each=\"suboject : ${object." + relacion.getNameB().toLowerCase() + "s}\" th:text=\"${suboject.toString()}\"></p></td>\n");
                        } else {  //ManyToOne && OneToOne
                            writer.append("\t\t\t\t\t<td th:if=\"${object." + relacion.getNameB().toLowerCase() + ".id_" + relacion.getNameB().toLowerCase() + "}\" th:text=\"${object." + relacion.getNameB().toLowerCase() + ".id_" + relacion.getNameB().toLowerCase() + "}\"></td>\n");
                        }
                    }
                }
                writer.append("\t\t\t\t\t<td th:if=\"${object.user}\" th:text=\"${object.user}\"></td>\n");
                writer.append("\t\t\t\t\t<td><form th:action=\"@{/" + entidad.getNombre() + "/Delete/} + ${object.id_" + entidad.getNombre().toLowerCase() + "}\" method=\"post\"><button type=\"submit\" class=\"btn btn-danger\" th:text=\"'Delete'\"></button></form></td>\n");
                writer.append("\t\t\t\t</tr>\n\t\t\t</tbody>\n\t\t</table>\n\n");
                writer.append("\t\t<div class=\"row mt-4\">\n");
                writer.append("\t\t\t<div class=\"col-10\"></div>\n");
                writer.append("\t\t\t<div class=\"col-2\"><a role=\"button\" class=\"btn btn-danger\" th:href=\"@{/" + entidad.getNombre() + "}\" th:text=\"#{principal.back}\"></a></div>\n");
                writer.append("\t\t</div>\n\t</div>\n\n");
                writer.append("</body>\n</html>");

                writer.close();
            }else{
                //viewNotas.html
                f = new File(this.resources + "/templates/" + entidad.getNombre() + "/view" + entidad.getNombre() + ".html");
                writer = new FileWriter(f);

                writer.append("<!DOCTYPE html>\n");
                writer.append("<html th:lang=\"#{principal.lang}\"\n" +
                        "      xmlns:th=\"http://www.thymeleaf.org\"\n" +
                        "      xmlns:layout=\"http://www.ultraq.net.nz/thymeleaf/layout\"\n" +
                        "      layout:decorate=\"~{layout}\"\n" +
                        "      xmlns:sec=\"http://www.w3.org/1999/xhtml\">\n");

                writer.append("<head>\n" +
                        "    <meta th:charset=\"#{principal.charset}\">\n" +
                        "    <title th:text=\"#{principal.title}\"></title>\n" +
                        "</head>\n");

                writer.append("<body>\n\n");
                writer.append("\t<div class=\"container justify-content-center\" layout:fragment=\"header\">\n");
                writer.append("\t\t<h1 th:text=\"'" + entidad.getNombre() + "'\"></h1>\n\t</div>\n\n");

                writer.append("\t<div class=\"container\" layout:fragment=\"content\">\n\n");

                writer.append("\t\t<div th:if=\"${param.successPost}\" class=\"mb-5\"><span class=\"alert alert-info\" th:text=\"${'Operation succeded (Creation)'}\"></span></div>\n" +
                        "\t\t<div th:if=\"${param.failPost}\" class=\"mb-5\"><span class=\"alert alert-danger\" th:text=\"${'Operation failed (Creation)'}\"></span></div>\n" +
                        "\t\t<div th:if=\"${param.successPut}\" class=\"mb-5\"><span class=\"alert alert-info\" th:text=\"${'Operation succeded (Update)'}\"></span></div>\n" +
                        "\t\t<div th:if=\"${param.failPut}\" class=\"mb-5\"><span class=\"alert alert-danger\" th:text=\"${'Operation failed (Update)'}\"></span></div>\n" +
                        "\t\t<div th:if=\"${param.successDelete}\" class=\"mb-5\"><span class=\"alert alert-info\" th:text=\"${'Operation succeded (Delete)'}\"></span></div>\n" +
                        "\t\t<div th:if=\"${param.failDelete}\"><span class=\"alert alert-danger\" th:text=\"${'Operation fail (Delete)'}\"></span></div>\n\n");

                for(RoleDto role : entidad.getPermisosRoles().getOperationPOST()){
                    writer.append("\t\t<!-- Post - " + role.getRoleName().toUpperCase() + " -->\n" +
                            "\t\t<div sec:authorize=\"hasRole('" + role.getRoleName().toUpperCase() + "')\" class=\"container mb-3\">\n" +
                            "\t\t\t<a role=\"button\" class=\"btn btn-success\" th:href=\"@{/" + entidad.getNombre() + "/Post}\" th:text=\"#{post.button} + ' " + entidad.getNombre() + "'\"></a>\n" +
                            "\t\t</div>\n\n");
                }

                writer.append("\t\t<table class=\"table table-responsive\">\n");
                writer.append("\t\t\t<thead>\n\t\t\t\t<tr>\n");
                for (AtributoDto atr : entidad.getAtributos()) {
                    writer.append("\t\t\t\t\t<td><strong th:text=\"'" + atr.getNombre() + "'\"></strong></td>\n");
                }
                for (Relacion relacion : relaciones) {
                    if (relacion.getA() == entidad) {
                        writer.append("\t\t\t\t\t<td><strong th:text=\"'Id_" + relacion.getNameB().toLowerCase() + "'\"></strong></td>\n");
                    }
                }
                writer.append("\t\t\t\t\t<td><strong th:text=\"'User_id'\"></strong></td>\n");

                for(RoleDto role : entidad.getPermisosRoles().getOperationPUT()){
                    writer.append("\t\t\t\t\t<td sec:authorize=\"hasRole('" + role.getRoleName().toUpperCase() + "')\"></td><!-- Put - " + role.getRoleName() + " -->\n");
                }
                for(RoleDto role : entidad.getPermisosRoles().getOperationDELETE()){
                    writer.append("\t\t\t\t\t<td sec:authorize=\"hasRole('" + role.getRoleName().toUpperCase() + "')\"></td><!-- Delete - " + role.getRoleName() + " -->\n");
                }

                writer.append("\t\t\t\t</tr>\n\t\t\t</thead>\n");
                writer.append("\t\t\t<tbody>\n");
                writer.append("\t\t\t\t<tr th:each=\"object : ${listAll}\">\n");
                for (AtributoDto atr : entidad.getAtributos()) {
                    writer.append("\t\t\t\t\t<td th:text=\"${object." + atr.getNombre() + "}\"></td>\n");
                }
                for (Relacion relacion : relaciones) {
                    if (relacion.getA() == entidad) {
                        if (relacion.getCardinalityA().equals("N") && relacion.getCardinalityB().equals("M")) {   //ManyToMany
                            writer.append("\t\t\t\t\t<td th:if=\"${object." + relacion.getNameB().toLowerCase() + "s}\"><p th:each=\"suboject : ${object." + relacion.getNameB().toLowerCase() + "s}\" th:text=\"${suboject.toString()}\"></p></td>\n");
                        } else {  //ManyToOne && OneToOne
                            writer.append("\t\t\t\t\t<td th:if=\"${object." + relacion.getNameB().toLowerCase() + ".id_" + relacion.getNameB().toLowerCase() + "}\" th:text=\"${object." + relacion.getNameB().toLowerCase() + ".id_" + relacion.getNameB().toLowerCase() + "}\"></td>\n");
                        }
                    }
                }
                writer.append("\t\t\t\t\t<td th:if=\"${object.user}\" th:text=\"${object.user}\"></td>\n");

                for(RoleDto role : entidad.getPermisosRoles().getOperationPUT()){
                    writer.append("\t\t\t\t\t<td sec:authorize=\"hasRole('" + role.getRoleName().toUpperCase() + "')\"><a role=\"button\" class=\"btn btn-info\" th:href=\"@{/" + entidad.getNombre() + "/Put/} + ${object.id_"  + entidad.getNombre().toLowerCase() + "}\" th:text=\"'Edit'\"></a></td>");
                }
                for(RoleDto role : entidad.getPermisosRoles().getOperationDELETE()){
                    writer.append("\t\t\t\t\t<td sec:authorize=\"hasRole('" + role.getRoleName().toUpperCase() + "')\"><form th:action=\"@{/" + entidad.getNombre() + "/Delete/} + ${object.id_"  + entidad.getNombre().toLowerCase() + "}\" method=\"post\"><button type=\"submit\" class=\"btn btn-danger\" th:text=\"'Delete'\"></button></form></td>\n");
                }

                writer.append("\t\t\t\t</tr>\n\t\t\t</tbody>\n\t\t</table>\n\n");
                writer.append("\t\t<div class=\"row mt-4\">\n" +
                        "\t\t\t<div class=\"col-10\"></div>\n" +
                        "\t\t\t<div class=\"col-2\"><a role=\"button\" class=\"btn btn-danger\" th:href=\"@{/index}\" th:text=\"#{principal.back}\"></a></div>\n" +
                        "\t\t</div>\n" +
                        "\t</div>\n\n");
                writer.append("</body>\n</html>");

                writer.close();

                //post{entidad}.html
                f = new File(this.resources + "/templates/" + entidad.getNombre() + "/post" + entidad.getNombre() + ".html");
                writer = new FileWriter(f);

                writer.append("<!DOCTYPE html>\n");
                writer.append("<html th:lang=\"#{principal.lang}\"\n" +
                        "      xmlns:th=\"http://www.thymeleaf.org\"\n" +
                        "      xmlns:layout=\"http://www.ultraq.net.nz/thymeleaf/layout\"\n" +
                        "      layout:decorate=\"~{layout}\">\n");

                writer.append("<head>\n" +
                        "    <meta th:charset=\"#{principal.charset}\">\n" +
                        "    <title th:text=\"#{principal.title}\"></title>\n" +
                        "</head>\n");

                writer.append("<body>\n\n");
                writer.append("\t<div class=\"container justify-content-center\" layout:fragment=\"header\">\n");
                writer.append("\t\t<h1 th:text=\"'" + entidad.getNombre() + "'\"></h1>\n\t</div>\n\n");

                writer.append("\t<div class=\"container\" layout:fragment=\"content\">\n\n");
                writer.append("\t\t<div class=\"container\">\n");
                writer.append("\t\t\t<form th:action=\"@{/" + entidad.getNombre() + "/Post}\" method=\"POST\" th:object=\"${object}\">\n\n");
                for (AtributoDto atr : entidad.getAtributos()) {
                    if (!atr.isPrimaryKey()) {
                        writer.append("\t\t\t\t<div class=\"input-group mt-5\">\n");
                        writer.append("\t\t\t\t\t<label for=\"" + atr.getNombre() + "\" class=\"control-label input-group-text\" th:text=\"'" + atr.getNombre() + "'\"></label>\n");
                        writer.append("\t\t\t\t\t<input id=\"" + atr.getNombre() + "\" class=\"form-control\" required th:field=\"*{" + atr.getNombre() + "}\">\n\t\t\t\t</div>\n\n");
                    }
                }
                int i = 1;
                for (Relacion relacion : relaciones) {
                    if (relacion.getA() == entidad) {
                        if (relacion.getCardinalityA().equals("N") && relacion.getCardinalityB().equals("M")) {   //ManyToMany
                            writer.append("\t\t\t\t<div class=\"form-floating mt-5\">\n");
                            writer.append("\t\t\t\t\t<select id=\"" + relacion.getNameB().toLowerCase() + "\" class=\"form-select\" multiple required th:field=\"*{" + relacion.getNameB().toLowerCase() + "Dto}\">\n");
                            writer.append("\t\t\t\t\t\t<option th:each=\"object" + i + " : ${list" + relacion.getNameB() + "}\" th:text=\"${object" + i + "}\" th:value=\"${object" + i + ".id_" + relacion.getNameB().toLowerCase() + "}\"></option>\n\t\t\t\t\t</select>\n");
                            writer.append("\t\t\t\t\t<label for=\"" + relacion.getNameB().toLowerCase() + "\" th:text=\"'" + relacion.getNameB().toLowerCase() + "'\"></label>\n\t\t\t\t</div>\n\n");
                        } else {  //ManyToOne && OneToOne
                            writer.append("\t\t\t\t<div class=\"form-floating mt-5\">\n");
                            writer.append("\t\t\t\t\t<select id=\"" + relacion.getNameB().toLowerCase() + "\" class=\"form-select\" required th:field=\"*{" + relacion.getNameB().toLowerCase() + "Dto.id_" + relacion.getNameB().toLowerCase() + "}\">\n");
                            writer.append("\t\t\t\t\t\t<option th:each=\"object" + i + " : ${list" + relacion.getNameB() + "}\" th:text=\"${object" + i + "}\" th:value=\"${object" + i + ".id_" + relacion.getNameB().toLowerCase() + "}\"></option>\n\t\t\t\t\t</select>\n");
                            writer.append("\t\t\t\t\t<label for=\"" + relacion.getNameB().toLowerCase() + "\" th:text=\"'" + relacion.getNameB().toLowerCase() + "'\"></label>\n\t\t\t\t</div>\n\n");
                        }
                    }
                    i++;
                }

                writer.append("\t\t\t\t<button type=\"submit\" class=\"btn btn-success mt-5\" th:text=\"#{principal.save}\"></button>\n\t\t\t</form>\n\t\t</div>\n\n");
                writer.append("\t\t<div class=\"row mt-4\">\n");
                writer.append("\t\t\t<div class=\"col-11\"></div>\n");
                writer.append("\t\t\t<div class=\"col-1\"><a role=\"button\" class=\"btn btn-warning\" th:href=\"@{/" + entidad.getNombre() + "}\" th:text=\"#{principal.back}\"></a></div>\n\t\t</div>\n\t</div>\n\n");
                writer.append("</body>\n</html>");

                writer.close();

                //put{entidad}.html
                f = new File(this.resources + "/templates/" + entidad.getNombre() + "/put" + entidad.getNombre() + ".html");
                writer = new FileWriter(f);

                writer.append("<!DOCTYPE html>\n");
                writer.append("<html th:lang=\"#{principal.lang}\"\n" +
                        "      xmlns:th=\"http://www.thymeleaf.org\"\n" +
                        "      xmlns:layout=\"http://www.ultraq.net.nz/thymeleaf/layout\"\n" +
                        "      layout:decorate=\"~{layout}\">\n");

                writer.append("<head>\n" +
                        "    <meta th:charset=\"#{principal.charset}\">\n" +
                        "    <title th:text=\"#{principal.title}\"></title>\n" +
                        "</head>\n");

                writer.append("<body>\n\n");
                writer.append("\t<div class=\"container justify-content-center\" layout:fragment=\"header\">\n");
                writer.append("\t\t<h1 th:text=\"'" + entidad.getNombre() + "'\"></h1>\n\t</div>\n\n");

                writer.append("\t<div class=\"container\" layout:fragment=\"content\">\n\n");
                writer.append("\t\t<div class=\"container\">\n");
                writer.append("\t\t\t<form th:action=\"@{/" + entidad.getNombre() + "/Put/} + ${object.id_" + entidad.getNombre().toLowerCase() + "}\" method=\"POST\" th:object=\"${object}\">\n\n");
                for (AtributoDto atr : entidad.getAtributos()) {
                    if (!atr.isPrimaryKey()) {
                        writer.append("\t\t\t\t<div class=\"input-group mt-5\">\n");
                        writer.append("\t\t\t\t\t<label for=\"" + atr.getNombre() + "\" class=\"control-label input-group-text\" th:text=\"'" + atr.getNombre() + "'\"></label>\n");
                        writer.append("\t\t\t\t\t<input id=\"" + atr.getNombre() + "\" class=\"form-control\" required th:field=\"*{" + atr.getNombre() + "}\" th:value=\"*{" + atr.getNombre() + "}\">\n\t\t\t\t</div>\n\n");
                    } else {
                        writer.append("\t\t\t\t<div class=\"input-group mt-5\">\n");
                        writer.append("\t\t\t\t\t<label for=\"" + atr.getNombre() + "\" class=\"control-label input-group-text\" th:text=\"'" + atr.getNombre() + "'\"></label>\n");
                        writer.append("\t\t\t\t\t<input id=\"" + atr.getNombre() + "\" class=\"form-control\" required th:field=\"*{" + atr.getNombre() + "}\" disabled th:value=\"*{" + atr.getNombre() + "}\">\n\t\t\t\t</div>\n\n");
                    }
                }
                i = 1;
                for (Relacion relacion : relaciones) {
                    if (relacion.getA() == entidad) {
                        if (relacion.getCardinalityA().equals("N") && relacion.getCardinalityB().equals("M")) {   //ManyToMany
                            writer.append("\t\t\t\t<div class=\"form-floating mt-5\">\n");
                            writer.append("\t\t\t\t\t<select id=\"" + relacion.getNameB().toLowerCase() + "\" class=\"form-select\" multiple required th:field=\"*{" + relacion.getNameB().toLowerCase() + "Dto}\">\n");
                            writer.append("\t\t\t\t\t\t<option th:each=\"object" + i + " : ${list" + relacion.getNameB() + "}\" th:text=\"${object" + i + "}\" th:value=\"${object" + i + ".id_" + relacion.getNameB().toLowerCase() + "}\"></option>\n\t\t\t\t\t</select>\n");
                            writer.append("\t\t\t\t\t<label for=\"" + relacion.getNameB().toLowerCase() + "\" th:text=\"'" + relacion.getNameB().toLowerCase() + "'\"></label>\n\t\t\t\t</div>\n\n");
                        } else {  //ManyToOne && OneToOne
                            writer.append("\t\t\t\t<div class=\"form-floating mt-5\">\n");
                            writer.append("\t\t\t\t\t<select id=\"" + relacion.getNameB().toLowerCase() + "\" class=\"form-select\" required th:field=\"*{" + relacion.getNameB().toLowerCase() + "Dto.id_" + relacion.getNameB().toLowerCase() + "}\">\n");
                            writer.append("\t\t\t\t\t\t<option th:each=\"object" + i + " : ${list" + relacion.getNameB() + "}\" th:text=\"${object" + i + "}\" th:value=\"${object" + i + ".id_" + relacion.getNameB().toLowerCase() + "}\"></option>\n\t\t\t\t\t</select>\n");
                            writer.append("\t\t\t\t\t<label for=\"" + relacion.getNameB().toLowerCase() + "\" th:text=\"'" + relacion.getNameB().toLowerCase() + "'\"></label>\n\t\t\t\t</div>\n\n");
                        }
                    }
                    i++;
                }

                writer.append("\t\t\t\t<button type=\"submit\" class=\"btn btn-success mt-5\" th:text=\"#{principal.save}\"></button>\n\t\t\t</form>\n\t\t</div>\n\n");
                writer.append("\t\t<div class=\"row mt-4\">\n");
                writer.append("\t\t\t<div class=\"col-10\"></div>\n");
                writer.append("\t\t\t<div class=\"col-2\"><a role=\"button\" class=\"btn btn-warning\" th:href=\"@{/" + entidad.getNombre() + "}\" th:text=\"#{principal.back}\"></a></div>\n\t\t</div>\n\t</div>\n\n");
                writer.append("</body>\n</html>");

                writer.close();
            }

        }catch (Exception ex){
            throw new HtmlException(ex.getMessage());
        }finally {
            try {
                if(writer != null) {
                    writer.close();
                }
            }catch (Exception ex){
                System.out.println(ex.getMessage());
            }
        }

    }


    /**
     * Método que genera los ficheros de configuración
     * @param entidades Lista de entidades
     * @param defaultLanguage Idioma por defecto
     * @throws ConfigException Exception
     */
    private void generateConfigDocs(List<EntidadDto> entidades, Idioma defaultLanguage) throws ConfigException{

        File f = null;
        FileWriter writer = null;

        try{
            f = new File(this.src + "/config/SecurityConfig.java");
            writer = new FileWriter(f);


            //SecurityConfig.java
            writer.append("package " + this.pathCode + ".config;\n\n");

            writer.append("import " + this.pathCode + ".service.UserService;\n");
            writer.append("import org.springframework.beans.factory.annotation.Autowired;\n");
            writer.append("import org.springframework.context.annotation.Bean;\n");
            writer.append("import org.springframework.context.annotation.Configuration;\n");
            writer.append("import org.springframework.security.authentication.AuthenticationProvider;\n");
            writer.append("import org.springframework.security.authentication.dao.DaoAuthenticationProvider;\n");
            writer.append("import org.springframework.security.config.annotation.web.builders.HttpSecurity;\n");
            writer.append("import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;\n");
            writer.append("import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;\n");
            writer.append("import org.springframework.security.web.SecurityFilterChain;\n");
            writer.append("import org.springframework.security.web.authentication.AuthenticationSuccessHandler;\n");
            writer.append("import org.springframework.security.web.util.matcher.AntPathRequestMatcher;\n\n");

            writer.append("@Configuration\n" +
                    "@EnableWebSecurity\n" +
                    "public class SecurityConfig {\n\n");

            writer.append("\t@Autowired\n" +
                    "\tprivate UserService userService;\n\n");

            writer.append("\t@Bean\n" +
                    "\tpublic BCryptPasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}\n\n");

            writer.append("\t@Bean\n" +
                    "\tpublic SecurityFilterChain filterChain(HttpSecurity http) throws Exception{\n\n");
            writer.append("\t\thttp.authorizeHttpRequests()\n");
            writer.append("\t\t\t.requestMatchers(\"/css/**\").permitAll()\n");
            writer.append("\t\t\t.requestMatchers(\"/fonts/**\").permitAll()\n");
            writer.append("\t\t\t.requestMatchers(\"/images/**\").permitAll()\n");
            writer.append("\t\t\t.requestMatchers(\"/js/**\").permitAll()\n");
            writer.append("\t\t\t.requestMatchers(\"/sass/**\").permitAll()\n");
            writer.append("\t\t\t.requestMatchers(\"/\").permitAll()\n");
            writer.append("\t\t\t.requestMatchers(\"/index\").permitAll()\n");
            writer.append("\t\t\t.requestMatchers(\"/registro\").permitAll()\n");
            writer.append("\t\t\t.requestMatchers(\"/usuarios/**\").hasRole(\"ADMIN\")\n");

            Set<RoleDto> rolesUnicos = null;
            String aux = null;
            boolean primeraVez = true;
            for(EntidadDto entidad : entidades){
                aux = "";
                primeraVez = true;
                rolesUnicos = new HashSet<>();
                rolesUnicos.addAll(entidad.getPermisosRoles().getOperationGET());
                rolesUnicos.addAll(entidad.getPermisosRoles().getOperationPOST());
                rolesUnicos.addAll(entidad.getPermisosRoles().getOperationPUT());
                rolesUnicos.addAll(entidad.getPermisosRoles().getOperationDELETE());

                aux = "\t\t\t.requestMatchers(\"/" + entidad.getNombre() + "\").hasAnyRole(";
                for(RoleDto role : rolesUnicos){
                    if(primeraVez){
                        aux += "\"" + role.getRoleName() + "\"";
                        primeraVez = false;
                    }else {
                        aux += ",\"" + role.getRoleName() + "\"";
                    }
                }
                aux += ")\n";
                writer.append(aux);

                primeraVez = true;
                aux = "\t\t\t.requestMatchers(\"/" + entidad.getNombre() + "/Get\").hasAnyRole(";
                for(RoleDto role : entidad.getPermisosRoles().getOperationGET()){
                    if(primeraVez){
                        aux += "\"" + role.getRoleName() + "\"";
                        primeraVez = false;
                    }else {
                        aux += ",\"" + role.getRoleName() + "\"";
                    }
                }
                aux += ")\n";
                writer.append(aux);

                primeraVez = true;
                aux = "\t\t\t.requestMatchers(\"/" + entidad.getNombre() + "/Post\").hasAnyRole(";
                for(RoleDto role : entidad.getPermisosRoles().getOperationPOST()){
                    if(primeraVez){
                        aux += "\"" + role.getRoleName() + "\"";
                        primeraVez = false;
                    }else {
                        aux += ",\"" + role.getRoleName() + "\"";
                    }
                }
                aux += ")\n";
                writer.append(aux);

                primeraVez = true;
                aux = "\t\t\t.requestMatchers(\"/" + entidad.getNombre() + "/Put/**\").hasAnyRole(";
                for(RoleDto role : entidad.getPermisosRoles().getOperationPUT()){
                    if(primeraVez){
                        aux += "\"" + role.getRoleName() + "\"";
                        primeraVez = false;
                    }else {
                        aux += ",\"" + role.getRoleName() + "\"";
                    }
                }
                aux += ")\n";
                writer.append(aux);

                primeraVez = true;
                aux = "\t\t\t.requestMatchers(\"/" + entidad.getNombre() + "/Delete/**\").hasAnyRole(";
                for(RoleDto role : entidad.getPermisosRoles().getOperationDELETE()){
                    if(primeraVez){
                        aux += "\"" + role.getRoleName() + "\"";
                        primeraVez = false;
                    }else {
                        aux += ",\"" + role.getRoleName() + "\"";
                    }
                }
                aux += ")\n";
                writer.append(aux);
            }

            writer.append("\t\t\t.anyRequest().authenticated();\n\n");

            writer.append("\t\thttp.formLogin()\n" +
                    "\t\t\t.loginPage(\"/login\")\n" +
                    "\t\t\t.successHandler(successHandler())\n" +
                    "\t\t\t.permitAll()\n" +
                    "\t\t\t.and()\n" +
                    "\t\t\t.logout().invalidateHttpSession(true)\n" +
                    "\t\t\t.clearAuthentication(true)\n" +
                    "\t\t\t.logoutRequestMatcher(new AntPathRequestMatcher(\"/logout\"))\n" +
                    "\t\t\t.logoutSuccessUrl(\"/login?logout\")\n" +
                    "\t\t\t.permitAll();\n" +
                    "\t\treturn http.build();\n\t}\n\n");

            writer.append("\t@Bean\n" +
                    "\tpublic AuthenticationProvider authenticationProvider(){\n\n" +
                    "\t\tDaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();\n" +
                    "\t\tauthenticationProvider.setUserDetailsService(userService);\n" +
                    "\t\tauthenticationProvider.setPasswordEncoder(passwordEncoder());\n\n" +
                    "\t\treturn authenticationProvider;\n\t}\n\n");

            writer.append("\tpublic AuthenticationSuccessHandler successHandler(){\n\n" +
                    "\t\treturn (request, response, authentication) -> response.sendRedirect(\"index\");\n\t}\n\n}");

            writer.close();

            //LocaleConfig.java
            f = new File(this.src + "/config/LocaleConfig.java");
            writer = new FileWriter(f);
            writer.append("package " + this.pathCode + ".config;\n\n");

            writer.append("import org.springframework.context.annotation.Bean;\n" +
                    "import org.springframework.context.annotation.Configuration;\n" +
                    "import org.springframework.web.servlet.LocaleContextResolver;\n" +
                    "import org.springframework.web.servlet.config.annotation.InterceptorRegistry;\n" +
                    "import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;\n" +
                    "import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;\n" +
                    "import org.springframework.web.servlet.i18n.SessionLocaleResolver;\n" +
                    "import java.util.Locale;\n\n");

            writer.append("@Configuration\n" +
                    "public class LocaleConfig implements WebMvcConfigurer {\n\n\n");

            writer.append("\t@Bean\n" +
                    "\tpublic LocaleContextResolver localeResolver(){\n\n" +
                    "\t\tSessionLocaleResolver resolver = new SessionLocaleResolver();\n" +
                    "\t\tresolver.setDefaultLocale(Locale.forLanguageTag(\"" + defaultLanguage.getAbreviatura().toLowerCase() + "\"));\n\n" +
                    "\t\treturn resolver;\n\t}\n\n\n");

            writer.append("\t@Bean\n" +
                    "\tpublic LocaleChangeInterceptor localeChangeInterceptor(){\n\n" +
                    "\t\tLocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();\n" +
                    "\t\tinterceptor.setParamName(\"lang\");\n\n" +
                    "\t\treturn interceptor;\n\t}\n\n\n");

            writer.append("\t@Override\n" +
                    "\tpublic void addInterceptors(InterceptorRegistry registry){\n" +
                    "\t\tregistry.addInterceptor(localeChangeInterceptor());\n" +
                    "\t}\n\n}");

            writer.close();

        }catch (Exception ex){
            throw new ConfigException(ex.getMessage());
        }finally {
            try {
                if(writer != null) {
                    writer.close();
                }
            }catch (Exception ex){
                System.out.println(ex.getMessage());
            }
        }

    }

    /**
     * Genera los documentos necesarios para que se pueda modificar el idioma en la aplicación a generar
     * @param idiomas Lista de idiomas
     */
    private void generateInternationalizationMethod(List<Idioma> idiomas) throws UrlException{

        File f = null;
        FileWriter writer = null;

        try{

            //UrlService
            f = new File(this.src + "/service/UrlService.java");
            writer = new FileWriter(f);

            writer.append("package " + this.pathCode + ".service;\n\n");

            writer.append("import lombok.Getter;\n" +
                    "import org.springframework.stereotype.Service;\n\n");

            writer.append("@Service\n" +
                    "@Getter\n" +
                    "public class UrlService {\n\n");

            String letraInicial, resto, abreviatura = null;
            for(Idioma idioma : idiomas){
                letraInicial = idioma.getAbreviatura().substring(0,1).toUpperCase();
                resto = idioma.getAbreviatura().substring(1).toLowerCase();
                abreviatura = letraInicial + resto;
                writer.append("\tprivate String url" + abreviatura + ";\n\n");
            }

            writer.append("\n\tpublic void setUrl(String url){\n\n");
            for(Idioma idioma : idiomas){
                letraInicial = idioma.getAbreviatura().substring(0,1).toUpperCase();
                resto = idioma.getAbreviatura().substring(1).toLowerCase();
                abreviatura = letraInicial + resto;
                writer.append("\t\tthis.url" + abreviatura + " = url + \"?lang=" + abreviatura.toLowerCase() + "\";\n");
            }
            writer.append("\t}\n\n}");

            writer.close();

            //UrlController
            f = new File(this.src + "/controller/UrlController.java");
            writer = new FileWriter(f);

            writer.append("package " + this.pathCode + ".controller;\n\n");
            writer.append("import " + this.pathCode + ".service.UrlService;\n" +
                    "import org.springframework.beans.factory.annotation.Autowired;\n" +
                    "import org.springframework.stereotype.Controller;\n" +
                    "import org.springframework.web.bind.annotation.GetMapping;\n\n");

            writer.append("@Controller\n" +
                    "public class UrlController {\n\n");

            writer.append("\t@Autowired\n" +
                    "\tprivate UrlService urlService;\n\n");

            for(Idioma idioma : idiomas){
                letraInicial = idioma.getAbreviatura().substring(0,1).toUpperCase();
                resto = idioma.getAbreviatura().substring(1).toLowerCase();
                abreviatura = letraInicial + resto;
                writer.append("\t@GetMapping(\"/redirect" + abreviatura + "\")\n" +
                        "\tpublic String redirect" + abreviatura + "(){\n" +
                        "\t\treturn \"redirect:\" + urlService.getUrl" + abreviatura + "();\n\t}\n\n\n");
            }
            writer.append("}");

            writer.close();

        }catch (Exception ex){
            throw new UrlException(ex.getMessage());
        }finally {
            try{
                if(writer != null) {
                    writer.close();
                }
            }catch (Exception ex){
                System.out.println(ex.getMessage());
            }
        }

    }


    /**
     * Genera un archivo de tipo SERVICE para la entidad que se pasa como parámetro
     * @param entidad Entidad
     * @throws ServiceException Excepción
     */
    private void generateService(EntidadDto entidad) throws ServiceException{

        File f = null;
        FileWriter writer = null;

        try {
            f = new File(this.src + "/service/" + entidad.getNombre() + "Service.java");
            writer = new FileWriter(f);

            writer.append("package " + this.pathCode + ".service;\n\n" +
                    "import org.springframework.stereotype.Service;\n\n" +
                    "@Service\n" +
                    "public class " + entidad.getNombre() + "Service {\n\n}");

            writer.close();

        }catch (Exception ex){
            throw new ServiceException(ex.getMessage());
        }finally {
            try{
                if(writer != null) {
                    writer.close();
                }
            }catch (Exception ex){
                System.out.println(ex.getMessage());
            }
        }

    }


    /**
     * Genera la clase principal que se ejecuta para levantar la aplicación.
     * @param name Nombre de la clase
     * @throws MainClassException Excepcion
     */
    private void generateMainClass(String name) throws MainClassException{

        File f = null;
        FileWriter writer = null;

        name = name.replace(" ", "");

        String primeraLetra, resto = "";
        primeraLetra = name.substring(0,1).toUpperCase();
        resto = name.substring(1);
        name = primeraLetra + resto;

        try {

            f = new File(this.src + "/" + name + "Application.java");
            writer = new FileWriter(f);

            writer.append("package " + this.pathCode + ";\n\n" +
                    "import org.springframework.boot.SpringApplication;\n" +
                    "import org.springframework.boot.autoconfigure.SpringBootApplication;\n\n");

            writer.append("@SpringBootApplication\n" +
                    "public class " + name + "Application{\n\n" +
                    "\tpublic static void main(String[] args) {\n\n" +
                    "\t\tSpringApplication.run(" + name + "Application.class, args);\n\n\t}\n\n}");

            writer.close();

        }catch (Exception ex){
            throw new MainClassException(ex.getMessage());
        }finally {
            try{
                if(writer != null) {
                    writer.close();
                }
            }catch (Exception ex){
                System.out.println(ex.getMessage());
            }
        }

    }


    /**
     * Genera los archivos correspondientes a la entidad USER
     * @param roles Roles de la aplicación
     * @param adminUser Usuario admin a registrar
     * @param defaultRole Rol por defecto
     * @param registroUsusarios Boolean por si se quiere poder registrar o no usuarios
     * @throws UserException Excepcion
     */
    private void generateUserDocs(List<RoleDto> roles, UsuarioAdminCredentials adminUser, RoleDto defaultRole, boolean registroUsusarios) throws UserException {

        File f = null;
        FileWriter writer = null;

        try{

            //User - Model
            f = new File(this.src + "/model/User.java");
            writer = new FileWriter(f);

            writer.append("package " + this.pathCode + ".model;\n\n");

            writer.append("import jakarta.persistence.*;\n" +
                    "import lombok.*;\n" +
                    "import java.io.Serializable;\n" +
                    "import java.util.List;\n\n");

            writer.append("@Entity\n" +
                    "@Getter\n" +
                    "@Setter\n" +
                    "@AllArgsConstructor\n" +
                    "@NoArgsConstructor\n" +
                    "@Table(name = \"user\")\n" +
                    "public class User implements Serializable {\n\n");

            writer.append("\tpublic User(String username, String password, String email,");

            for(AtributoDto atr : usuarioService.getAtributosUsuario()){
                    writer.append(" " + atr.getTipo() + " " + atr.getNombre() + ",");
            }

            writer.append(" Role role) {\n" +
                    "\t\tthis.username = username;\n" +
                    "\t\tthis.password = password;\n" +
                    "\t\tthis.email = email;\n");

            for(AtributoDto atr : usuarioService.getAtributosUsuario()){
                writer.append("\t\tthis." + atr.getNombre() + " = " + atr.getNombre() + ";\n");
            }

            writer.append("\t\tthis.role = role;\n\t}\n\n");

            writer.append("\t@Id\n" +
                    "\t@GeneratedValue(strategy = GenerationType.AUTO)\n" +
                    "\tprivate long id;\n\n");

            writer.append("\t@Column(name = \"username\", length = 30, nullable = false, unique = true)\n" +
                    "\tprivate String username;\n\n");

            writer.append("\t@Column(name = \"password\", length = 250)\n" +
                    "\tprivate String password;\n\n");

            writer.append("\t@Column(name = \"email\", length = 50, nullable = false, unique = true)\n" +
                    "\tprivate String email;\n\n");

            String aux = "";
            for(AtributoDto atr : usuarioService.getAtributosUsuario()){
                aux = "\t@Column(name = \"" + atr.getNombre() + "\", ";
                if(atr.getTipo().equals("String")) {
                    aux += "length = " + atr.getSizeText() + ", ";
                }

                if(atr.isNullable()) {
                    aux += "nullable = true, ";
                }else {
                    aux += "nullable = false, ";
                }

                if(atr.isUnique()) {
                    aux += "unique = true)\n";
                }else {
                    aux += "unique = false)\n";
                }
                aux += "\tprivate " + atr.getTipo() + " " + atr.getNombre().toLowerCase() + ";\n\n\n";
                writer.append(aux);
            }

            writer.append("\t@ManyToOne(fetch = FetchType.EAGER)\n" +
                    "\t@JoinColumn(name = \"idrole\")\n" +
                    "\tprivate Role role;\n\n\n");

            writer.append("\t@Override\n" +
                    "\tpublic String toString(){\n\n" +
                    "\t\treturn id + \" - \" + username + \" - \" + role;\n" +
                    "\t}\n\n}");

            writer.close();

            //UserDto - Dto
            f = new File(this.src + "/dto/UserDto.java");
            writer = new FileWriter(f);

            writer.append("package " + this.pathCode + ".dto;\n\n");
            writer.append("import " + this.pathCode + ".model.Role;\n" +
                    "import lombok.AllArgsConstructor;\n" +
                    "import lombok.Getter;\n" +
                    "import lombok.NoArgsConstructor;\n" +
                    "import lombok.Setter;\n\n");

            writer.append("@Getter\n" +
                    "@Setter\n" +
                    "@AllArgsConstructor\n" +
                    "@NoArgsConstructor\n" +
                    "public class UserDto {\n" +
                    "\tprivate long id;\n\n" +
                    "\tprivate String username;\n\n" +
                    "\tprivate String email;\n\n");

            for(AtributoDto atr : usuarioService.getAtributosUsuario()){
                writer.append("\tprivate " + atr.getTipo() + " " + atr.getNombre() + ";\n\n");
            }

            writer.append("\tprivate Role role;\n\n}");

            writer.close();

            //UserDtoPsw - Dto
            f = new File(this.src + "/dto/UserDtoPsw.java");
            writer = new FileWriter(f);

            writer.append("package " + this.pathCode + ".dto;\n\n");
            writer.append("import " + this.pathCode + ".model.Role;\n" +
                    "import lombok.AllArgsConstructor;\n" +
                    "import lombok.Getter;\n" +
                    "import lombok.NoArgsConstructor;\n" +
                    "import lombok.Setter;\n\n");

            writer.append("@Getter\n" +
                    "@Setter\n" +
                    "@AllArgsConstructor\n" +
                    "@NoArgsConstructor\n" +
                    "public class UserDtoPsw {\n" +
                    "\tprivate long id;\n\n" +
                    "\tprivate String username;\n\n" +
                    "\tprivate String email;\n\n");

            for(AtributoDto atr : usuarioService.getAtributosUsuario()){
                writer.append("\tprivate " + atr.getTipo() + " " + atr.getNombre() + ";\n\n");
            }

            writer.append("\tprivate String role;\n\n" +
                    "\tprivate String password;\n\n}");

            writer.close();

            //UserRepository - Repository
            f = new File(this.src + "/repository/UserRepository.java");
            writer = new FileWriter(f);

            writer.append("package " + this.pathCode + ".repository;\n\n");
            writer.append("import " + this.pathCode + ".model.Role;\n" +
                    "import " + this.pathCode + ".model.User;\n" +
                    "import org.springframework.data.jpa.repository.JpaRepository;\n" +
                    "import org.springframework.stereotype.Repository;\n" +
                    "import java.util.List;\n\n");

            writer.append("@Repository\n" +
                    "public interface UserRepository extends JpaRepository<User,Long> {\n\n" +
                    "\tUser findByUsername(String username);\n\n" +
                    "\tUser findByEmail(String email);\n\n" +
                    "\tList<User> findByRole(Role role);\n\n" +
                    "\tvoid deleteById(long id);\n\n}");

            writer.close();

            //UserService (interface) - Service
            f = new File(this.src + "/service/UserService.java");
            writer = new FileWriter(f);

            writer.append("package " + this.pathCode + ".service;\n\n");
            writer.append("import " + this.pathCode + ".dto.UserDtoPsw;\n" +
                    "import " + this.pathCode + ".model.User;\n" +
                    "import org.springframework.security.core.userdetails.UserDetailsService;\n" +
                    "import " + this.pathCode + ".dto.RoleDto;\n" +
                    "import java.util.List;\n\n");

            writer.append("public interface UserService extends UserDetailsService {\n\n" +
                    "\tpublic User save(UserDtoPsw userDtoPsw);\n\n" +
                    "\tpublic List<User> listUsers();\n\n" +
                    "\tboolean registryAdmin();\n\n" +
                    "\tvoid createRoles(List<RoleDto> roles);\n\n" +
                    "\tvoid adminComprobation();\n\n" +
                    "}");

            writer.close();

            //UserServiceImpl (implementation) - Service
            f = new File(this.src + "/service/impl");
            if(!f.mkdir()){
                throw new UserException("No se ha podido crear la carpeta \"impl\". Ruta: /service/impl");
            }

            f = new File(this.src + "/service/impl/UserServiceImpl.java");
            writer = new FileWriter(f);

            writer.append("package " + this.pathCode + ".service.impl;\n\n");

            writer.append("import " + this.pathCode + ".dto.RoleDto;\n" +
                    "import " + this.pathCode + ".dto.UserDtoPsw;\n" +
                    "import " + this.pathCode + ".model.Role;\n" +
                    "import " + this.pathCode + ".model.User;\n" +
                    "import " + this.pathCode + ".repository.RoleRepository;\n" +
                    "import " + this.pathCode + ".repository.UserRepository;\n" +
                    "import " + this.pathCode + ".service.UserService;\n" +
                    "import org.springframework.beans.factory.annotation.Autowired;\n" +
                    "import org.springframework.security.core.GrantedAuthority;\n" +
                    "import org.springframework.security.core.authority.SimpleGrantedAuthority;\n" +
                    "import org.springframework.security.core.userdetails.UserDetails;\n" +
                    "import org.springframework.security.core.userdetails.UsernameNotFoundException;\n" +
                    "import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;\n" +
                    "import org.springframework.stereotype.Service;\n" +
                    "import java.util.*;\n\n");

            writer.append("@Service\n" +
                    "public class UserServiceImpl implements UserService {\n\n");

            writer.append("\tprivate boolean adminComprobacion = false;\n\n\n");

            writer.append("\t@Autowired\n" +
                    "\tprivate UserRepository userRepository;\n\n");

            writer.append("\t@Autowired\n" +
                    "\tprivate RoleRepository roleRepository;\n\n");

            writer.append("\t@Autowired\n" +
                    "\tprivate BCryptPasswordEncoder encoder;\n\n\n");

            writer.append("\t@Override\n" +
                    "\tpublic User save(UserDtoPsw userDtoPsw) {\n\n" +
                    "\t\tRole rol = roleRepository.findByRoleName(\"ROLE_" + defaultRole.getRoleName().toUpperCase() + "\");\n\n");

            writer.append("\t\tUser user = new User(\n" +
                    "\t\t\tuserDtoPsw.getUsername(),\n" +
                    "\t\t\tencoder.encode(userDtoPsw.getPassword()),\n" +
                    "\t\t\tuserDtoPsw.getEmail(),\n");

            String letraInicial, resto, nombreCompleto = null;

            for(AtributoDto atr : usuarioService.getAtributosUsuario()){

                letraInicial = atr.getNombre().substring(0,1).toUpperCase();
                resto = atr.getNombre().substring(1);
                nombreCompleto = letraInicial + resto;

                writer.append("\t\t\tuserDtoPsw.get" + nombreCompleto + "(),\n");
            }

            writer.append("\t\t\trol);\n\n" +
                    "\t\treturn userRepository.save(user);\n\t}\n\n\n");

            writer.append("\t@Override\n" +
                    "\tpublic List<User> listUsers() {\n\n" +
                    "\t\treturn userRepository.findAll();\n\t}\n\n\n");

            writer.append("\t@Override\n" +
                    "\tpublic UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {\n\n" +
                    "\t\tUser user = userRepository.findByUsername(username);\n\n" +
                    "\t\tif(user == null){\n" +
                    "\t\t\tthrow new UsernameNotFoundException(\"The user doesnt exists\");\n\t\t}\n\n" +
                    "\t\tSet<GrantedAuthority> rol = new HashSet<>();\n" +
                    "\t\trol.add(new SimpleGrantedAuthority(user.getRole().getRoleName()));\n\n" +
                    "\t\treturn new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), rol);\n\t}\n\n\n");

            writer.append("\t@Override\n" +
                    "\tpublic boolean registryAdmin(){\n\n" +
                    "\t\tRole roleAdmin = roleRepository.findByRoleName(\"ROLE_ADMIN\");\n" +
                    "\t\tList<User> admins = userRepository.findByRole(roleAdmin);\n" +
                    "\t\tfor(User usr : admins){\n" +
                    "\t\t\treturn false;\n\t\t}\n\n" +
                    "\t\tUser admin = new User(\n" +
                    "\t\t\t\"" + adminUser.getUsername() + "\",\n" +
                    "\t\t\tencoder.encode(\"" + adminUser.getPassword() + "\"),\n" +
                    "\t\t\t\"" + adminUser.getEmail() + "\",\n");

            int i = 0;
            for(AtributoDto value : adminUser.getAtributos()){
                if(value.getTipo().equals("int")){
                    writer.append("\t\t\t" + adminUser.getValores()[i] + ",\n");
                }else {
                    writer.append("\t\t\t\"" + adminUser.getValores()[i] + "\",\n");
                }
                i++;
            }

            writer.append("\t\t\troleAdmin);\n\n");

            writer.append("\t\tuserRepository.save(admin);\n\n" +
                    "\t\treturn true;\n\t}\n\n\n");

            writer.append("\t@Override\n" +
                    "\tpublic void createRoles(List<RoleDto> roles){\n\n" +
                    "\t\tfor(RoleDto role : roles) {\n" +
                    "\t\t\tif (roleRepository.findByRoleName(role.getRoleName()) == null) {\n" +
                    "\t\t\t\troleRepository.save(new Role(role.getId(), \"ROLE_\" + role.getRoleName().toUpperCase()));\n" +
                    "\t\t\t}\n\t\t}\n\n\t}\n\n\n");

            writer.append("\t@Override\n" +
                    "\tpublic void adminComprobation(){\n\n" +
                    "\t\tif(!adminComprobacion) {\n" +
                    "\t\t\tList<RoleDto> roles = new ArrayList<RoleDto>();\n");

            for(RoleDto role : roles){
                writer.append("\t\t\tRoleDto " + role.getRoleName().toLowerCase() + " = new RoleDto( " + role.getId() + ",\"" + role.getRoleName().toUpperCase() + "\");\n" +
                        "\t\t\troles.add(" + role.getRoleName().toLowerCase() + ");\n");
            }

            writer.append("\t\t\tcreateRoles(roles);\n" +
                    "\t\t\tregistryAdmin();\n" +
                    "\t\t\tadminComprobacion = true;\n\t\t}\n\t}\n\n}");

            writer.close();

            //Carpeta users - templates
            f = new File(this.resources + "/templates/Users");
            if(!f.mkdir()){
                throw new UserException("Error al crear la carpeta \"Users\". Path: /templates/Users");
            }

            //Carpeta admin - templates
            f = new File(this.resources + "/templates/Users/admin");
            if(!f.mkdir()){
                throw new UserException("Error al crear la carpeta \"admin\". Path: /templates/Users/admin");
            }

            //UsersController
            f = new File(this.src + "/controller/UsersController.java");
            writer = new FileWriter(f);

            writer.append("package " + this.pathCode + ".controller;\n\n");

            writer.append("import " + this.pathCode + ".service.UrlService;\n" +
                    "import " + this.pathCode + ".service.UserService;\n" +
                    "import org.springframework.beans.factory.annotation.Autowired;\n" +
                    "import org.springframework.security.access.prepost.PreAuthorize;\n" +
                    "import org.springframework.security.core.Authentication;\n" +
                    "import org.springframework.security.core.context.SecurityContextHolder;\n" +
                    "import org.springframework.security.core.userdetails.User;\n" +
                    "import org.springframework.stereotype.Controller;\n" +
                    "import org.springframework.ui.Model;\n" +
                    "import org.springframework.web.bind.annotation.GetMapping;\n\n");

            writer.append("@Controller\n" +
                    "public class UsersController {\n\n");

            writer.append("\t@Autowired\n\tprivate UserService userService;\n\n");

            writer.append("\t@Autowired\n\tprivate UrlService urlService;\n\n");

            writer.append("\t@GetMapping(\"/login\")\n" +
                    "\tpublic String vistaLogin(){\n\n" +
                    "\t\turlService.setUrl(\"/login\");\n\n" +
                    "\t\treturn \"/Users/login\";\n\t}\n\n\n");

            writer.append("\t@PreAuthorize(\"hasRole('ROLE_ADMIN')\")\n" +
                    "\t@GetMapping(\"/usuarios\")\n" +
                    "\tpublic String vistaUsuariosAdmin(Model model){\n\n" +
                    "\t\turlService.setUrl(\"/usuarios\");\n\n" +
                    "\t\tmodel.addAttribute(\"usuarios\",userService.listUsers());\n\n" +
                    "\t\treturn \"/Users/admin/users\";\n\t}\n\n}");

            writer.close();

            //UsersRegistryController
            f = new File(this.src + "/controller/UsersRegistryController.java");
            writer = new FileWriter(f);

            writer.append("package " + this.pathCode + ".controller;\n\n");

            writer.append("import " + this.pathCode + ".dto.UserDtoPsw;\n" +
                    "import " + this.pathCode + ".model.User;\n" +
                    "import " + this.pathCode + ".repository.RoleRepository;\n" +
                    "import " + this.pathCode + ".repository.UserRepository;\n" +
                    "import " + this.pathCode + ".service.UrlService;\n" +
                    "import " + this.pathCode + ".service.UserService;\n" +
                    "import org.springframework.beans.factory.annotation.Autowired;\n" +
                    "import org.springframework.security.access.prepost.PreAuthorize;\n" +
                    "import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;\n" +
                    "import org.springframework.stereotype.Controller;\n" +
                    "import org.springframework.ui.Model;\n" +
                    "import org.springframework.web.bind.annotation.GetMapping;\n" +
                    "import org.springframework.web.bind.annotation.ModelAttribute;\n" +
                    "import org.springframework.web.bind.annotation.PathVariable;\n" +
                    "import org.springframework.web.bind.annotation.PostMapping;\n\n");

            writer.append("@Controller\n" +
                    "public class UsersRegistryController {\n\n");

            writer.append("\t@Autowired\n\tprivate UserService userService;\n\n" +
                    "\t@Autowired\n\tprivate UrlService urlService;\n\n" +
                    "\t@Autowired\n\tprivate RoleRepository roleRepository;\n\n" +
                    "\t@Autowired\n\tprivate UserRepository userRepository;\n\n" +
                    "\t@Autowired\n\tprivate BCryptPasswordEncoder encoder;\n\n\n");


            writer.append("\t@ModelAttribute(\"usuario\")\n" +
                    "\tpublic UserDtoPsw newUserDtoPsw(){\n" +
                    "\t\treturn new UserDtoPsw();\n\t}\n\n\n");

            if(registroUsusarios){
                writer.append("\t@GetMapping(\"/registro\")\n" +
                        "\tpublic String mostrarRegistro(){\n\n" +
                        "\t\turlService.setUrl(\"/registro\");\n" +
                        "\t\treturn \"/Users/registry\";\n\t}\n\n");

                writer.append("\t@PostMapping(\"/registro\")\n" +
                        "\tpublic String registryUser(@ModelAttribute(\"usuario\") UserDtoPsw userDtoPsw){\n\n" +
                        "\t\tif(userRepository.findByUsername(userDtoPsw.getUsername()) != null ||\n" +
                        "\t\t\tuserRepository.findByEmail(userDtoPsw.getEmail()) != null){\n" +
                        "\t\t\treturn \"redirect:/registro?error\";\n\t\t}\n\n" +
                        "\t\tuserService.save(userDtoPsw);\n\n" +
                        "\t\treturn \"redirect:/login?exito\";\n\t}\n\n");
            }

            writer.append("\t@PreAuthorize(\"hasRole('ROLE_ADMIN')\")\n" +
                    "\t@GetMapping(\"/usuarios/registry\")\n" +
                    "\tpublic String userRegistryAdmin(Model model){\n\n" +
                    "\t\turlService.setUrl(\"/usuarios/registry\");\n" +
                    "\t\tmodel.addAttribute(\"roles\", roleRepository.findAll());\n\n" +
                    "\t\treturn \"/Users/admin/registryAdmin\";\n\t}\n\n");

            writer.append("\t@PreAuthorize(\"hasRole('ROLE_ADMIN')\")\n" +
                    "\t@PostMapping(\"/usuarios/registry\")\n" +
                    "\tpublic String userRegistryAdmin(UserDtoPsw userDtoPsw){\n\n" +
                    "\t\tif(userRepository.findByUsername(userDtoPsw.getUsername()) != null ||\n" +
                    "\t\t\tuserRepository.findByEmail(userDtoPsw.getEmail()) != null){\n" +
                    "\t\t\treturn \"redirect:/usuarios?fallo\";\n\t\t}\n\n" +
                    "\t\tUser user = new User(userDtoPsw.getUsername(),\n" +
                    "\t\t\tencoder.encode(userDtoPsw.getPassword()),\n" +
                    "\t\t\tuserDtoPsw.getEmail(),\n");

            for(AtributoDto atr : usuarioService.getAtributosUsuario()){

                letraInicial = atr.getNombre().substring(0,1).toUpperCase();
                resto = atr.getNombre().substring(1);
                nombreCompleto = letraInicial + resto;

                writer.append("\t\t\tuserDtoPsw.get" + nombreCompleto + "(),\n");
            }

            writer.append("\t\t\troleRepository.findByRoleName(userDtoPsw.getRole()));\n\n" +
                    "\t\tuserRepository.save(user);\n\n" +
                    "\t\treturn \"redirect:/usuarios?exito\";\n\t}\n\n");

            writer.append("\t@PreAuthorize(\"hasRole('ROLE_ADMIN')\")\n" +
                    "\t@PostMapping(\"/usuarios/delete/{id}\")\n" +
                    "\tpublic String userDeleteAdmin(@PathVariable(name = \"id\") long id){\n\n" +
                    "\t\tuserRepository.deleteById(id);\n\n" +
                    "\t\treturn \"redirect:/usuarios?exitoDelete\";\n" +
                    "\t}\n\n}");

            writer.close();

            //login html
            f = new File(this.resources + "/templates/Users/login.html");
            writer = new FileWriter(f);

            writer.append("<!DOCTYPE html>\n" +
                    "<html th:lang=\"#{principal.lang}\" xmlns:th=\"http://www.thymeleaf.org\"\n" +
                    "      xmlns:layout=\"http://www.ultraq.net.nz/thymeleaf/layout\"\n" +
                    "      layout:decorate=\"~{layout}\" >\n" +
                    "<head>\n" +
                    "    <meta th:charset=\"#{principal.charset}\">\n" +
                    "    <title th:text=\"#{principal.title}\"></title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "\n" +
                    "    <div layout:fragment=\"header\">\n" +
                    "        <h1 th:text=\"#{principal.login}\"></h1>\n" +
                    "    </div>\n" +
                    "\n" +
                    "    <div layout:fragment=\"content\">\n" +
                    "\n" +
                    "        <div class=\"py-4 container\">\n" +
                    "            <form th:action=\"@{/login}\" method=\"post\">\n" +
                    "\n" +
                    "                <div th:if=\"${param.error}\" class=\"alert alert-danger\" th:text=\"'Fail'\"></div>\n" +
                    "\n" +
                    "                <div th:if=\"${param.logout}\" class=\"alert alert-info\" th:text=\"'Logout'\"></div>\n" +
                    "\n" +
                    "                <div class=\"form-group input-group\">\n" +
                    "                    <label class=\"control-label input-group-text\" for=\"username\" th:text=\"#{principal.username}\"></label>\n" +
                    "                    <input id=\"username\" type=\"text\" class=\"form-control\" name=\"username\" required autofocus=\"autofocus\" th:placeholder=\"#{principal.username}\">\n" +
                    "                </div>\n" +
                    "                <br>\n" +
                    "\n" +
                    "                <div class=\"form-group input-group\">\n" +
                    "                    <label class=\"control-label input-group-text\" for=\"password\" th:text=\"#{principal.password}\"></label>\n" +
                    "                    <input id=\"password\" type=\"password\" class=\"form-control\" name=\"password\" required autofocus=\"autofocus\" th:placeholder=\"#{principal.password}\">\n" +
                    "                </div>\n" +
                    "                <br>\n" +
                    "\n" +
                    "                <button type=\"submit\" class=\"btn btn-primary form-control \" th:text=\"#{principal.login}\"></button>\n" +
                    "\n" +
                    "\n" +
                    "            </form>\n" +
                    "            <br>\n" +
                    "            <div class=\"container\">\n" +
                    addLineaRegistro(registroUsusarios) +
                    "            </div>\n" +
                    "\n" +
                    "        </div>\n" +
                    "\n" +
                    "    </div>\n" +
                    "</body>\n" +
                    "</html>");

            writer.close();

            //registry html
            f = new File(this.resources + "/templates/Users/registry.html");
            writer = new FileWriter(f);

            if(registroUsusarios) {

                writer.append("<!DOCTYPE html>\n" +
                        "<html lang=\"en\" xmlns:th=\"http://www.thymeleaf.org\"\n" +
                        "      xmlns:layout=\"http://www.ultraq.net.nz/thymeleaf/layout\"\n" +
                        "      layout:decorate=\"~{layout}\">\n" +
                        "<head>\n" +
                        "    <meta th:charset=\"#{principal.charset}\">\n" +
                        "    <title th:text=\"#{principal.title}\"></title>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "\n" +
                        "    <div layout:fragment=\"header\">\n" +
                        "        <h1 th:text=\"#{principal.registrarUsuario}\"></h1>\n" +
                        "    </div>\n" +
                        "\n" +
                        "\n" +
                        "    <div layout:fragment=\"content\">\n" +
                        "\n" +
                        "        <div th:if=\"${param.error}\" class=\"alert alert-danger\" th:text=\"'Fail'\"></div>\n" +
                        "\n" +
                        "        <div class=\"py-4\">\n" +
                        "            <form th:action=\"@{/registro}\" method=\"post\" th:object=\"${usuario}\">\n" +
                        "                <div class=\"form-group input-group\">\n" +
                        "                    <label class=\"control-label input-group-text\" for=\"nombre\" th:text=\"#{principal.username}\"></label>\n" +
                        "                    <input id=\"nombre\" type=\"text\" class=\"form-control\" name=\"nombre\" th:field=\"*{username}\" required autofocus=\"autofocus\">\n" +
                        "                </div>\n" +
                        "                <br>\n" +
                        "\n" +
                        "                <div class=\"form-group input-group\">\n" +
                        "                    <label class=\"control-label input-group-text\" for=\"email\" th:text=\"#{principal.email}\"></label>\n" +
                        "                    <input id=\"email\" class=\"form-control\" type=\"email\" name=\"email\" th:field=\"*{email}\" required autofocus=\"autofocus\">\n" +
                        "                </div>\n" +
                        "                <br>\n" +
                        "\n" +
                        "                <div class=\"form-group input-group\">\n" +
                        "                    <label class=\"control-label input-group-text\" for=\"password\" th:text=\"#{principal.password}\"></label>\n" +
                        "                    <input id=\"password\" class=\"form-control\" type=\"password\" name=\"password\" th:field=\"*{password}\" required autofocus=\"autofocus\">\n" +
                        "                </div>\n" +
                        "                <br>\n" +
                        "\n");

                for(AtributoDto atr : usuarioService.getAtributosUsuario()){
                    writer.append("                <div class=\"form-group input-group\">\n" +
                            "                    <label class=\"control-label input-group-text\" for=\"" + atr.getNombre() + "\" th:text=\"'" + atr.getNombre() + "'\"></label>\n" +
                            "                    <input id=\"" + atr.getNombre() + "\" class=\"form-control\" type=\"text\" name=\"" + atr.getNombre() + "\" th:field=\"*{" + atr.getNombre() + "}\" required autofocus=\"autofocus\">\n" +
                            "                </div>\n" +
                            "                <br>\n" +
                            "\n");
                }

                writer.append("                <div class=\"form-group\">\n" +
                        "                    <button type=\"submit\" class=\"btn btn-primary form-control\" th:text=\"#{principal.registrarUsuario}\"></button>\n" +
                        "                </div>\n" +
                        "\n" +
                        "                <a th:href=\"@{/login}\"><span  th:text=\"#{principal.login}\"></span></a>\n" +
                        "            </form>\n" +
                        "        </div>\n" +
                        "    </div>\n" +
                        "\n" +
                        "\n" +
                        "</body>\n" +
                        "</html>");
            }

            writer.close();

            //usuarios html
            f = new File(this.resources + "/templates/Users/admin/users.html");
            writer = new FileWriter(f);

            writer.append("<!DOCTYPE html>\n" +
                    "<html th:lang=\"#{principal.lang}\" xmlns:th=\"http://www.thymeleaf.org\"\n" +
                    "      xmlns:layout=\"http://www.ultraq.net.nz/thymeleaf/layout\"\n" +
                    "      layout:decorate=\"~{layout}\">\n" +
                    "<head>\n" +
                    "    <meta th:charset=\"#{principal.charset}\">\n" +
                    "    <title th:text=\"#{principal.title}\"></title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "\n" +
                    "    <div layout:fragment=\"header\">\n" +
                    "        <div class=\"container mb-4\">\n" +
                    "            <h1><strong th:text=\"#{principal.usuarios}\"></strong></h1>\n" +
                    "        </div>\n" +
                    "    </div>\n" +
                    "\n" +
                    "    <div layout:fragment=\"content\">\n" +
                    "\n" +
                    "        <div class=\"alert alert-info py-4 mb-3\" th:if=\"${param.exito}\" th:text=\"'Success'\"></div>\n" +
                    "        <div class=\"alert alert-danger py-4 mb-3\" th:if=\"${param.fallo}\" th:text=\"'Failed'\"></div>\n" +
                    "        <div class=\"alert alert-info py-4 mb-3\" th:if=\"${param.exitoDelete}\" th:text=\"'Success'\"></div>\n" +
                    "\n" +
                    "        <div class=\"container\">\n" +
                    "            <table class=\"table table-bordered table-responsive\">\n" +
                    "                <thead>\n" +
                    "                    <tr>\n" +
                    "                        <td><strong><span th:text=\"#{principal.username}\"></span></strong></td>\n" +
                    "                        <td><strong><span th:text=\"#{principal.email}\"></span></strong></td>\n" +
                    "                        <td><strong><span th:text=\"#{principal.rol}\"></span></strong></td>\n");

            for(AtributoDto atr : usuarioService.getAtributosUsuario()){
                writer.append("                        <td><strong><span th:text=\"'" + atr.getNombre() + "'\"></span></strong></td>\n");
            }

            writer.append("                        <td></td>\n" +
                    "                    </tr>\n" +
                    "                </thead>\n" +
                    "\n" +
                    "                <tbody>\n" +
                    "                    <tr th:each=\"usuario: ${usuarios}\">\n" +
                    "                        <td th:text=\"${usuario.username}\"></td>\n" +
                    "                        <td th:text=\"${usuario.email}\"></td>\n" +
                    "                        <td th:text=\"${usuario.role.roleName}\"></td>\n");

            for(AtributoDto atr : usuarioService.getAtributosUsuario()){
                writer.append("                        <td th:text=\"${usuario." + atr.getNombre() + "}\"></td>\n");
            }

            writer.append("                        <td><form th:action=\"@{/usuarios/delete/} + ${usuario.id}\" method=\"POST\"><button type=\"submit\" class=\"btn btn-danger\" th:text=\"#{delete.button}\"></button></form></td>" +
                    "                    </tr>\n" +
                    "                </tbody>\n" +
                    "\n" +
                    "            </table>\n" +
                    "        </div>\n" +
                    "\n" +
                    "        <div class=\"container py-3\"><a role=\"button\" class=\"btn btn-info\" th:href=\"@{/usuarios/registry}\" th:text=\"#{principal.registrarUsuario}\"></a></div>\n" +
                    "\n" +
                    "        <br><br>\n" +
                    "\n" +
                    "        <br><br>\n" +
                    "\n" +
                    "    </div>\n" +
                    "\n" +
                    "</body>\n" +
                    "</html>");

            writer.close();

            //registryAdmin html
            f = new File(this.resources + "/templates/Users/admin/registryAdmin.html");
            writer = new FileWriter(f);

            writer.append("<!DOCTYPE html>\n" +
                    "<html lang=\"en\" xmlns:th=\"http://www.thymeleaf.org\"\n" +
                    "      xmlns:layout=\"http://www.ultraq.net.nz/thymeleaf/layout\"\n" +
                    "      layout:decorate=\"~{layout}\">\n" +
                    "<head>\n" +
                    "    <meta th:charset=\"#{principal.charset}\">\n" +
                    "    <title th:text=\"#{principal.title}\"></title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "\n" +
                    "    <div layout:fragment=\"header\">\n" +
                    "        <h1 th:text=\"#{principal.registrarUsuario}\"></h1>\n" +
                    "    </div>\n" +
                    "\n" +
                    "\n" +
                    "    <div layout:fragment=\"content\">\n" +
                    "\n" +
                    "        <div class=\"py-4\">\n" +
                    "            <form th:action=\"@{/usuarios/registry}\" method=\"POST\" th:object=\"${usuario}\">\n" +
                    "                <div class=\"form-group input-group\">\n" +
                    "                    <label class=\"control-label input-group-text\" for=\"nombre\" th:text=\"#{principal.username}\"></label>\n" +
                    "                    <input id=\"nombre\" type=\"text\" class=\"form-control\" name=\"nombre\" th:field=\"*{username}\" required autofocus=\"autofocus\">\n" +
                    "                </div>\n" +
                    "                <br>\n" +
                    "\n" +
                    "                <div class=\"form-group input-group\">\n" +
                    "                    <label class=\"control-label input-group-text\" for=\"email\" th:text=\"#{principal.email}\"></label>\n" +
                    "                    <input id=\"email\" class=\"form-control\" type=\"email\" name=\"email\" th:field=\"*{email}\" required autofocus=\"autofocus\">\n" +
                    "                </div>\n" +
                    "                <br>\n" +
                    "\n" +
                    "                <div class=\"form-group input-group\">\n" +
                    "                    <label class=\"control-label input-group-text\" for=\"password\" th:text=\"#{principal.password}\"></label>\n" +
                    "                    <input id=\"password\" class=\"form-control\" type=\"password\" name=\"password\" th:field=\"*{password}\" required autofocus=\"autofocus\">\n" +
                    "                </div>\n" +
                    "                <br>\n" +
                    "\n" +
                    "                <div class=\"form-floating mt-2 py-3\">\n" +
                    "                    <select id=\"select_role\" class=\"form-select\" th:field=\"*{role}\">\n" +
                    "                        <option th:each=\"rol : ${roles}\" th:text=\"${rol.roleName}\" th:value=\"${rol.roleName}\"></option>\n" +
                    "                    </select>\n" +
                    "                    <label for=\"select_role\" th:text=\"#{principal.rol}\"></label>\n" +
                    "                </div>\n");

            for(AtributoDto atr : usuarioService.getAtributosUsuario()){
                writer.append("                <div class=\"form-group input-group\">\n" +
                        "                    <label class=\"control-label input-group-text\" for=\"" + atr.getNombre() + "\" th:text=\"'" + atr.getNombre() + "'\"></label>\n" +
                        "                    <input id=\"" + atr.getNombre() + "\" class=\"form-control\" type=\"text\" name=\"" + atr.getNombre() + "\" th:field=\"*{" + atr.getNombre() + "}\" required autofocus=\"autofocus\">\n" +
                        "                </div>\n" +
                        "                <br>\n" +
                        "\n");
            }

            writer.append("\n" +
                    "                <div class=\"form-group mt-5\">\n" +
                    "                    <button type=\"submit\" class=\"btn btn-primary form-control\" th:text=\"#{principal.registrarUsuario}\"></button>\n" +
                    "                </div>\n" +
                    "\n" +
                    "            </form>\n" +
                    "        </div>\n" +
                    "    </div>\n" +
                    "\n" +
                    "\n" +
                    "</body>\n" +
                    "</html>");

            writer.close();

        }catch (Exception ex){
            throw new UserException(ex.getMessage());
        }finally {
            try{
                if(writer != null) {
                    writer.close();
                }
            }catch (Exception ex){
                System.out.println(ex.getMessage());
            }
        }
    }

    private String addLineaRegistro(boolean var) {
        if(var) {
            return "                <a class=\"\" th:href=\"@{/registro}\"><span th:text=\"#{principal.registrarUsuario}\"></span></a>\n";
        }else {
            return "";
        }
    }


    /**
     * Generate Role Docs.
     * @throws RoleException Exception
     */
    private void generateRoleDocs() throws RoleException{

        File f = null;
        FileWriter writer = null;

        try{

            //Role - Model
            f = new File(this.src + "/model/Role.java");
            writer = new FileWriter(f);

            writer.append("package " + this.pathCode + ".model;\n\n" +
                    "import jakarta.persistence.*;\n" +
                    "import lombok.AllArgsConstructor;\n" +
                    "import lombok.Getter;\n" +
                    "import lombok.NoArgsConstructor;\n" +
                    "import lombok.Setter;\n" +
                    "import java.io.Serializable;\n" +
                    "import java.util.Set;\n\n");

            writer.append("@Entity\n" +
                    "@Getter\n" +
                    "@Setter\n" +
                    "@AllArgsConstructor\n" +
                    "@NoArgsConstructor\n" +
                    "@Table(name = \"role\")\n" +
                    "public class Role implements Serializable{\n\n");

            writer.append("\t@Id\n" +
                    "\tprivate Integer id;\n\n");

            writer.append("\t@Column(name = \"role_name\",nullable = false)\n" +
                    "\tprivate String roleName;\n\n");

            writer.append("\t@OneToMany(mappedBy = \"role\")\n" +
                    "\tprivate Set<User> usuarios;\n\n");

            writer.append("\tpublic Role(int id, String roleName) {\n" +
                    "\t\tthis.id = id;\n" +
                    "\t\tthis.roleName = roleName;\n\t}\n\n\n");

            writer.append("\t@Override\n" +
                    "\tpublic String toString(){\n\n" +
                    "\t\treturn roleName;\n" +
                    "\t}\n\n}");

            writer.close();

            //RoleDto - Dto
            f = new File(this.src + "/dto/RoleDto.java");
            writer = new FileWriter(f);

            writer.append("package " + this.pathCode + ".dto;\n\n" +
                    "import lombok.AllArgsConstructor;\n" +
                    "import lombok.Getter;\n" +
                    "import lombok.NoArgsConstructor;\n" +
                    "import lombok.Setter;\n\n");

            writer.append("@Getter\n" +
                    "@Setter\n" +
                    "@AllArgsConstructor\n" +
                    "@NoArgsConstructor\n" +
                    "public class RoleDto {\n\n" +
                    "\tprivate int id;\n\n" +
                    "\tprivate String roleName;\n\n}");

            writer.close();

            //RoleRepository - Repository
            f = new File(this.src + "/repository/RoleRepository.java");
            writer = new FileWriter(f);

            writer.append("package " + this.pathCode + ".repository;\n\n" +
                    "import " + this.pathCode + ".model.Role;\n" +
                    "import org.springframework.data.jpa.repository.JpaRepository;\n" +
                    "import org.springframework.stereotype.Repository;\n\n");

            writer.append("@Repository\n" +
                    "public interface RoleRepository extends JpaRepository<Role,Integer> {\n\n");

            writer.append("\tRole findByRoleName(String roleName);\n\n}");


            writer.close();


        }catch (Exception ex){
            throw new RoleException(ex.getMessage());
        }finally {
            try{
                if(writer != null) {
                    writer.close();
                }
            }catch (Exception ex){
                System.out.println(ex.getMessage());
            }
        }
    }


    /**
     * Genera los documentos correspondientes a las vistas principales de la aplicación a generar.
     * Eso incluye los index, layout, error html's etc.
     * @throws IndexException Excepción
     */
    private void generateIndexDocs(String title, List<EntidadDto> entidades, List<Idioma> idiomas, ColorPicker colores, boolean nav) throws IndexException {

        File f = null;
        FileWriter writer = null;

        try{

            //HomeController - Controller
            f = new File(this.src + "/controller/HomeController.java");
            writer = new FileWriter(f);

            writer.append("package " + this.pathCode +".controller;\n\n" +
                    "import " + this.pathCode + ".service.UrlService;\n" +
                    "import " + this.pathCode + ".service.UserService;\n" +
                    "import org.springframework.beans.factory.annotation.Autowired;\n" +
                    "import org.springframework.stereotype.Controller;\n" +
                    "import org.springframework.web.bind.annotation.GetMapping;\n" +
                    "import org.springframework.security.core.Authentication;\n" +
                    "import org.springframework.security.core.context.SecurityContextHolder;\n" +
                    "import java.util.ArrayList;\n" +
                    "import java.util.List;\n\n");

            writer.append("@Controller\n" +
                    "public class HomeController {\n\n");

            writer.append("\t@Autowired\n" +
                    "\tprivate UserService userService;\n\n" +
                    "\t@Autowired\n" +
                    "\tprivate UrlService urlService;\n\n\n");

            writer.append("\t@GetMapping(\"/\")\n" +
                    "\tpublic String vistaPrincipal(){\n\n" +
                    "\t\turlService.setUrl(\"/index\");\n" +
                    "\t\tuserService.adminComprobation();\n\n" +
                    "\t\treturn \"index\";\n\t}\n\n");

            writer.append("\t@GetMapping(\"/index\")\n" +
                    "\tpublic String vistaIndex(){\n\n" +
                    "\t\turlService.setUrl(\"/index\");\n" +
                    "\t\tuserService.adminComprobation();\n\n" +
                    "\t\treturn \"index\";\n\t}\n\n");

            writer.append("\t@GetMapping(\"/loginlogout\")\n" +
                    "\tpublic String loginlogout(){\n\n" +
                    "\t\tAuthentication auth = SecurityContextHolder.getContext().getAuthentication();\n" +
                    "\t\t\tif(auth.getPrincipal() == \"anonymousUser\"){\n" +
                    "\t\t\t\treturn \"redirect:/login\";\n" +
                    "\t\t\t}else{\n" +
                    "\t\t\t\treturn \"redirect:/logout\";\n" +
                    "\t\t\t}\n" +
                    "\t\t}\n\n}");

            writer.close();

            //layout.html - Navigation bar
            String name = "";
            if(nav){
                name = "layout";
            }else{
                name = "layout2";
            }
            f = new File(this.resources + "/templates/" + name + ".html");
            writer = new FileWriter(f);

            writer.append("<!doctype html>\n" +
                    "<html th:lang=\"#{principal.lang}\"\n" +
                    "\t  xmlns:th=\"http://www.thymeleaf.org\"\n" +
                    "\t  xmlns:layout=\"http://www.ultraq.net.nz/thymeleaf/layout\"\n" +
                    "\t  xmlns:sec=\"http://www.w3.org/1999/xhtml\">\n" +
                    "<head>\n" +
                    "\t<meta th:charset=\"#{principal.charset}\">\n" +
                    "\t<title th:text=\"#{principal.title}\"></title>\n" +
                    "\t<link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css\" rel=\"stylesheet\" integrity=\"sha384T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN\" crossorigin=\"anonymous\">\n" +
                    "</head>\n" +
                    "\n" +
                    "\n" +
                    "<body style=\"background-color: " + colores.getSecondaryCodeColor() + ";\">\n" +
                    "\n" +
                    "\t<nav class=\"navbar navbar-expand-lg fixed-top mb-5\" style=\"background-color: " + colores.getPrincipalCodeColor() + ";\" >\n" +
                    "\n" +
                    "\t\t<div class=\"container\" style=\"height:3rem\">\n" +
                    "\n" +
                    "\t\t\t<a class=\"navbar-brand text-light\" th:href=\"@{/index}\"><img th:src=\"@{/images/home.png}\" alt=\"\" height=\"30\"></a>\n" +
                    "\n" +
                    "\t\t\t<a class=\"navbar-brand\" th:href=\"@{/index}\">" + title + "</a>\n" +
                    "\n" +
                    "\t\t\t<button class=\"navbar-toggler\" type=\"button\" data-bs-toggle=\"collapse\" data-bs-target=\"#navbar\" aria-controls=\"navbarScroll\" aria-expanded=\"false\" aria-label=\"Toggle navigation\">\n" +
                    "\t\t\t\t<span class=\"navbar-toggler-icon\"></span>\n" +
                    "\t\t\t</button>\n" +
                    "\n" +
                    "\t\t\t<div class=\"collapse navbar-collapse ms-5\" id=\"navbar\">\n" +
                    "\n" +
                    "\t\t\t\t<ul class=\"navbar-nav\" style=\"--bs-scroll-height: 100px;\" >\n" +
                    "\t\t\t\t\t<li class=\"nav-item\">\n" +
                    "\t\t\t\t\t\t<a class=\"nav-link\" th:href=\"@{/index}\"><strong th:text=\"#{principal.home}\"></strong></a>\n" +
                    "\t\t\t\t\t</li>\n" +
                    "\n" +
                    "\t\t\t\t\t<li class=\"nav-item dropdown ms-3\">\n" +
                    "\t\t\t\t\t\t<a class=\"nav-link dropdown-toggle\" href=\"#\" role=\"button\" data-bs-toggle=\"dropdown\" aria-expanded=\"false\"><strong th:text=\"#{principal.services}\"></strong></a>\n" +
                    "\n" +
                    "\t\t\t\t\t\t<ul class=\"dropdown-menu\" style=\"background-color: " + colores.getPrincipalCodeColor() + ";\">\n");

            boolean primeraVez = true;
            Set<RoleDto> rolesUnicos = new HashSet<RoleDto>();
            for(EntidadDto entidad : entidades) {

                rolesUnicos.clear();
                rolesUnicos.addAll(entidad.getPermisosRoles().getOperationGET());
                rolesUnicos.addAll(entidad.getPermisosRoles().getOperationPOST());
                rolesUnicos.addAll(entidad.getPermisosRoles().getOperationPUT());
                rolesUnicos.addAll(entidad.getPermisosRoles().getOperationDELETE());

                if(primeraVez){
                    for (RoleDto rol : rolesUnicos) {
                        writer.append("\t\t\t\t\t\t\t<li sec:authorize=\"hasRole('" + rol.getRoleName().toUpperCase() + "')\"><a class=\"dropdown-item\" th:href=\"@{/" + entidad.getNombre() + "}\"><strong>" + entidad.getNombre() + "</strong></a></li>\n");
                    }
                    primeraVez = false;
                }else{
                    writer.append("\t\t\t\t\t\t\t<li><hr class=\"dropdown-divider\"></li>\n");
                    for (RoleDto rol : rolesUnicos) {
                        writer.append("\t\t\t\t\t\t\t<li sec:authorize=\"hasRole('" + rol.getRoleName().toUpperCase() + "')\"><a class=\"dropdown-item\" th:href=\"@{/" + entidad.getNombre() + "}\"><strong>" + entidad.getNombre() + "</strong></a></li>\n");
                    }
                }

            }

            writer.append("\t\t\t\t\t\t</ul>\n" +
                    "\t\t\t\t\t</li>\n" +
                    "\t\t\t\t\t<li sec:authorize=\"hasRole('ADMIN')\" class=\"nav-item ms-3\">\n" +
                    "\t\t\t\t\t\t<a class=\"nav-link\" th:href=\"@{/usuarios}\"><strong th:text=\"#{principal.usuarios}\"></strong></a>\n" +
                    "\t\t\t\t\t</li>\n" +
                    "\t\t\t\t</ul>\n" +
                    "\t\t\t</div>\n" +
                    "\n" +
                    "\t\t\t<div class=\"nav-item dropdown me-4\">\n" +
                    "\n" +
                    "\t\t\t\t<a class=\"nav-link dropdown-toggle\" href=\"#\" role=\"button\" data-bs-toggle=\"dropdown\" aria-expanded=\"false\"><strong th:text=\"#{principal.language}\"></strong></a>\n" +
                    "\n" +
                    "\t\t\t\t<ul class=\"dropdown-menu text-center\" style=\"background-color: " + colores.getPrincipalCodeColor() + ";\">\n");

            String letraInicial, resto, abreviatura = "";
            primeraVez = true;
            for(Idioma idioma : idiomas) {
                letraInicial = idioma.getAbreviatura().substring(0, 1).toUpperCase();
                resto = idioma.getAbreviatura().substring(1).toLowerCase();
                abreviatura = letraInicial + resto;

                if(primeraVez){
                    writer.append("\t\t\t\t\t<li>\n" +
                                    "\t\t\t\t\t\t<a class=\"dropdown-item px-2\" th:href=\"@{/redirect" + abreviatura + "}\"><strong><span th:text=\"'" + idioma.getNombreIdioma() + "'\"></span></strong></a>\n" +
                                    "\t\t\t\t\t</li>\n");
                    primeraVez = false;
                }else{
                    writer.append("\t\t\t\t\t<li><hr class=\"dropdown-divider\"></li>\n" +
                            "\t\t\t\t\t<li>\n" +
                            "\t\t\t\t\t\t<a class=\"dropdown-item px-2\" th:href=\"@{/redirect" + abreviatura + "}\"><strong><span th:text=\"'" + idioma.getNombreIdioma() + "'\"></span></strong></a>\n" +
                            "\t\t\t\t\t</li>\n");
                }
            }

            writer.append("\t\t\t\t</ul>\n" +
                    "\n" +
                    "\t\t\t</div>\n" +
                    "\n" +
                    "\t\t\t<div class=\"nav-item dropdown me-4\">\n" +
                    "\n" +
                    "\t\t\t\t<a class=\"nav-link dropdown-toggle\" href=\"#\" role=\"button\" data-bs-toggle=\"dropdown\"><img th:src=\"@{/images/acceso.png}\" alt=\"\" height=\"30\"></a>\n" +
                    "\n" +
                    "\t\t\t\t<ul class=\"dropdown-menu text-center\" style=\"background-color: " + colores.getPrincipalCodeColor() + ";\">\n" +
                    "\t\t\t\t\t<li><a class=\"dropdown-item\" th:href=\"@{/loginlogout}\"><strong th:text=\"#{principal.loginlogout}\"></strong></a></li>\n" +
                    "\t\t\t\t</ul>\n" +
                    "\n" +
                    "\t\t\t</div>" +
                    "\t\t</div>\n" +
                    "\n" +
                    "\t</nav>\n" +
                    "\n" +
                    "\t<div class=\"container-xxl py-5\">\n" +
                    "\n" +
                    "\t\t<div class=\"py-5\">\n" +
                    "\t\t\t<section class=\"container\" layout:fragment=\"header\"> </section>\n" +
                    "\t\t\t<section class=\"container\" layout:fragment=\"content\"> </section>\n" +
                    "\t\t</div>\n" +
                    "\n" +
                    "\t</div>\n" +
                    "\n" +
                    "\n" +
                    "\t<script src=\"https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js\" integrity=\"sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL\" crossorigin=\"anonymous\"></script>\n" +
                    "\n" +
                    "</body>\n" +
                    "</html>");

            writer.close();

            //Layout2.html - Lateral bar
            name = "";
            if(nav){
                name = "layout2";
            }else{
                name = "layout";
            }
            f = new File(this.resources + "/templates/" + name + ".html");
            writer = new FileWriter(f);

            writer.append("<!doctype html>\n" +
                    "<html th:lang=\"#{principal.lang}\"\n" +
                    "\t  xmlns:th=\"http://www.thymeleaf.org\"\n" +
                    "\t  xmlns:layout=\"http://www.ultraq.net.nz/thymeleaf/layout\"\n" +
                    "\t  xmlns:sec=\"http://www.w3.org/1999/xhtml\">\n" +
                    "<head>\n" +
                    "\t<meta th:charset=\"#{principal.charset}\">\n" +
                    "\t<title th:text=\"#{principal.title}\"></title>\n" +
                    "\t<link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css\" rel=\"stylesheet\" integrity=\"sha384T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN\" crossorigin=\"anonymous\">\n" +
                    "</head>\n" +
                    "\n" +
                    "\n" +
                    "<body style=\"background-color: " + colores.getSecondaryCodeColor() + ";\">\n" +
                    "\n" +
                    "\t<div class=\"d-flex flex-column flex-shrink-0 p-3 fixed-top fixed-bottom\" style=\"width: 280px; background-color: " + colores.getPrincipalCodeColor() + ";\">\n" +
                    "\n" +
                    "\t\t<a th:href=\"@{/index}\" class=\"d-flex align-items-center mb-3 mb-md-0 me-md-auto text-white text-decoration-none mx-2\">\n" +
                    "\t\t\t<img th:src=\"@{/images/home.png}\" alt=\"\" height=\"30\">\n" +
                    "\t\t\t<span>&nbsp&nbsp</span>\n" +
                    "\t\t\t<span class=\"text-dark\"><strong>" + title + "</strong></span>\n" +
                    "\t\t</a>\n" +
                    "\n" +
                    "\t\t<hr>\n" +
                    "\n" +
                    "\t\t<ul class=\"nav nav-pills flex-column mb-auto py-3\">\n" +
                    "\t\t\t<li class=\"nav-item\">\n" +
                    "\t\t\t\t<a href=\"@{/index}\" class=\"nav-link text-dark\" aria-current=\"page\"><strong th:text=\"#{principal.home}\"></strong></a>\n" +
                    "\t\t\t</li>\n" +
                    "\t\t\t<li class=\"mb-1\">\n" +
                    "\t\t\t\t<button class=\"btn btn-toggle d-inline-flex align-items-center rounded border-0 collapsed text-dark\" data-bs-toggle=\"collapse\" data-bs-target=\"#home-collapse\" aria-expanded=\"true\">\n" +
                    "\t\t\t\t\t<strong th:text=\"#{principal.services}\"></strong>\n" +
                    "\t\t\t\t</button>\n" +
                    "\t\t\t\t<div class=\"collapse\" id=\"home-collapse\">\n" +
                    "\t\t\t\t\t<ul class=\"btn-toggle-nav list-unstyled fw-normal pb-1 small text-dark\">\n");

            primeraVez = true;
            rolesUnicos = new HashSet<RoleDto>();
            for(EntidadDto entidad : entidades) {

                rolesUnicos.clear();
                rolesUnicos.addAll(entidad.getPermisosRoles().getOperationGET());
                rolesUnicos.addAll(entidad.getPermisosRoles().getOperationPOST());
                rolesUnicos.addAll(entidad.getPermisosRoles().getOperationPUT());
                rolesUnicos.addAll(entidad.getPermisosRoles().getOperationDELETE());

                if(primeraVez){
                    for (RoleDto rol : rolesUnicos) {
                        writer.append("\t\t\t\t\t\t\t<li sec:authorize=\"hasRole('" + rol.getRoleName().toUpperCase() + "')\"><a class=\"dropdown-item\" th:href=\"@{/" + entidad.getNombre() + "}\"><strong>" + entidad.getNombre() + "</strong></a></li>\n");
                    }
                    primeraVez = false;
                }else{
                    writer.append("\t\t\t\t\t\t\t<li><hr class=\"dropdown-divider\"></li>\n");
                    for (RoleDto rol : rolesUnicos) {
                        writer.append("\t\t\t\t\t\t\t<li sec:authorize=\"hasRole('" + rol.getRoleName().toUpperCase() + "')\"><a class=\"dropdown-item\" th:href=\"@{/" + entidad.getNombre() + "}\"><strong>" + entidad.getNombre() + "</strong></a></li>\n");
                    }
                }

            }

            writer.append("\t\t\t\t\t</ul>\n" +
                    "\t\t\t\t</div>\n" +
                    "\t\t\t</li>\n" +
                    "\t\t\t<li sec:authorize=\"hasRole('ADMIN')\">\n" +
                    "\t\t\t\t<a th:href=\"@{/usuarios}\" class=\"nav-link text-dark\" aria-current=\"page\"><strong th:text=\"#{principal.usuarios}\"></strong></a>\n" +
                    "\t\t\t</li>\n" +
                    "\t\t</ul>\n" +
                    "\n" +
                    "\t\t<hr>\n" +
                    "\n" +
                    "\t\t<div class=\"nav-item dropdown me-4\">\n" +
                    "\n" +
                    "\t\t\t<a class=\"nav-link dropdown-toggle\" href=\"#\" role=\"button\" data-bs-toggle=\"dropdown\"><img th:src=\"@{/images/acceso.png}\" alt=\"\" height=\"30\"></a>\n" +
                    "\n" +
                    "\t\t\t<ul class=\"dropdown-menu text-center\" style=\"background-color: " + colores.getPrincipalCodeColor() + ";\">\n" +
                    "\t\t\t\t<li><a class=\"dropdown-item\" th:href=\"@{/loginlogout}\"><strong th:text=\"#{principal.loginlogout}\"></strong></a></li>\n" +
                    "\t\t\t</ul>\n" +
                    "\n" +
                    "\t\t</div>\n" +
                    "\n" +
                    "\t\t<hr>\n" +
                    "\n" +
                    "\t\t<div class=\"nav-item dropdown me-4\">\n" +
                    "\n" +
                    "\t\t\t<a class=\"nav-link dropdown-toggle\" href=\"#\" role=\"button\" data-bs-toggle=\"dropdown\" aria-expanded=\"false\"><strong th:text=\"#{principal.language}\"></strong></a>\n" +
                    "\n" +
                    "\t\t\t<ul class=\"dropdown-menu text-center\" style=\"background-color: " + colores.getPrincipalCodeColor() + ";\">\n");

            letraInicial = ""; resto = ""; abreviatura = "";
            primeraVez = true;
            for(Idioma idioma : idiomas) {
                letraInicial = idioma.getAbreviatura().substring(0, 1).toUpperCase();
                resto = idioma.getAbreviatura().substring(1).toLowerCase();
                abreviatura = letraInicial + resto;

                if(primeraVez){
                    writer.append("\t\t\t\t\t<li>\n" +
                            "\t\t\t\t\t\t<a class=\"dropdown-item px-2\" th:href=\"@{/redirect" + abreviatura + "}\"><strong><span th:text=\"'" + idioma.getNombreIdioma() + "'\"></span></strong></a>\n" +
                            "\t\t\t\t\t</li>\n");
                    primeraVez = false;
                }else{
                    writer.append("\t\t\t\t\t<li><hr class=\"dropdown-divider\"></li>\n" +
                            "\t\t\t\t\t<li>\n" +
                            "\t\t\t\t\t\t<a class=\"dropdown-item px-2\" th:href=\"@{/redirect" + abreviatura + "}\"><strong><span th:text=\"'" + idioma.getNombreIdioma() + "'\"></span></strong></a>\n" +
                            "\t\t\t\t\t</li>\n");
                }
            }

            writer.append("\t\t\t</ul>\n" +
                    "\n" +
                    "\t\t</div>\n" +
                    "\n" +
                    "\t</div>\n" +
                    "\n" +
                    "\n" +
                    "\t<div class=\"container-xxl py-5\">\n" +
                    "\n" +
                    "\t\t<div class=\"py-5\">\n" +
                    "\t\t\t<!-- Aqui vamos a escribir las cabeceras de los bloques  -->\n" +
                    "\t\t\t<section layout:fragment=\"header\"></section>\n" +
                    "\t\t\t<!-- Aqui vamos a escribir el contenido de los bloques  -->\n" +
                    "\t\t\t<section layout:fragment=\"content\"></section>\n" +
                    "\t\t</div>\n" +
                    "\n" +
                    "\t</div>\n" +
                    "\n" +
                    "\n" +
                    "\t<script src=\"https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js\" integrity=\"sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL\" crossorigin=\"anonymous\"></script>\n" +
                    "\n" +
                    "</body>\n" +
                    "</html>");

            writer.close();

            //Index.html
            f = new File(this.resources + "/templates/index.html");
            writer = new FileWriter(f);

            writer.append("<!doctype html>\n" +
                    "<html th:lang=\"#{principal.lang}\"\n" +
                    "      xmlns:th=\"http://www.thymeleaf.org\"\n" +
                    "      xmlns:sec=\"http://www.w3.org/1999/xhtml\"\n" +
                    "      xmlns:layout=\"http://www.ultraq.net.nz/thymeleaf/layout\"\n" +
                    "      layout:decorate=\"~{layout}\">\n" +
                    "<head>\n" +
                    "    <meta th:charset=\"#{principal.charset}\">\n" +
                    "    <title th:text=\"#{principal.title}\"> </title>\n" +
                    "</head>\n" +
                    "\n" +
                    "\n" +
                    "<body>\n" +
                    "\n" +
                    "    <div layout:fragment=\"header\" class=\"container-fluid\">\n" +
                    "        <div class=\"p-sm-5 mb-4 rounded text-body-emphasis text-center shadow\" style=\"background-color: #edf0eb;\">\n" +
                    "            <h1 class=\"display-1\" th:text=\"#{principal.title}\"></h1>\n" +
                    "        </div>\n" +
                    "    </div>\n" +
                    "\n" +
                    "    <div layout:fragment=\"content\">\n" +
                    "        <div class=\"row\">\n");

            for(EntidadDto entidad : entidades) {

                rolesUnicos.clear();
                rolesUnicos.addAll(entidad.getPermisosRoles().getOperationGET());
                rolesUnicos.addAll(entidad.getPermisosRoles().getOperationPOST());
                rolesUnicos.addAll(entidad.getPermisosRoles().getOperationPUT());
                rolesUnicos.addAll(entidad.getPermisosRoles().getOperationDELETE());

                for (RoleDto rol : rolesUnicos) {
                    writer.append("            <!-- class " + entidad.getNombre() + " - Role " + rol.getRoleName().toUpperCase() + " -->\n" +
                            "            <div sec:authorize=\"hasRole('" + rol.getRoleName().toUpperCase() + "')\" class=\"col-1 mt-5\"></div>\n" +
                            "            <div sec:authorize=\"hasRole('" + rol.getRoleName().toUpperCase() + "')\" class=\"col-4 p-4 mt-5 border rounded text-center card\" style=\"background-color: #edf0eb;\">\n" +
                            "                <h3 class=\"display-4\">" + entidad.getNombre() + "</h3>\n" +
                            "                <a th:href=\"@{/" + entidad.getNombre() + "}\" class=\"stretched-link\"></a>\n" +
                            "            </div>\n" +
                            "            <div sec:authorize=\"hasRole('" + rol.getRoleName().toUpperCase() + "')\" class=\"col-1 mt-5\"></div>\n\n");
                }
            }
            writer.append("        </div>\n" +
                    "    </div>\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "</body>\n" +
                    "</html>");

            writer.close();

            //Error.html
            f = new File(this.resources + "/templates/error.html");
            writer = new FileWriter(f);

            writer.append("<!DOCTYPE html>\n" +
                    "<html th:lang=\"#{principal.lang}\" xmlns:th=\"http://www.thymeleaf.org\"\n" +
                    "      xmlns:layout=\"http://www.ultraq.net.nz/thymeleaf/layout\"\n" +
                    "      layout:decorate=\"~{layout}\">\n" +
                    "<head>\n" +
                    "    <meta th:charset=\"#{principal.charset}\">\n" +
                    "    <title th:text=\"#{principal.title}\"></title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "\n" +
                    "    <div layout:fragment=\"header\" class=\"container py-4\">\n" +
                    "\n" +
                    "        <h1 class=\"alert alert-danger\" th:text=\"#{principal.errorPage}\"></h1>\n" +
                    "    </div>\n" +
                    "\n" +
                    "    <div layout:fragment=\"content\" class=\"container py-4\">\n" +
                    "    </div>\n" +
                    "\n" +
                    "</body>\n" +
                    "</html>");

            writer.close();

        }catch (Exception ex){
            throw new IndexException(ex.getMessage());
        }finally {
            try{
                if(writer != null) {
                    writer.close();
                }
            }catch (Exception ex){
                System.out.println(ex.getMessage());
            }
        }

    }


    /**
     * Genera los archivos properties (application_properties, y messages_properties + cada idioma)
     * @param idiomas Idiomas
     * @param defaultLanguage Idioma por defecto
     * @param title Titulo
     * @throws PropertiesException Excepcion
     */
    private void generatePropertiesDocs(List<Idioma> idiomas, Idioma defaultLanguage, String title, String nameDB, int numPuerto) throws PropertiesException{

        File f = null;
        FileWriter writer = null;

        try{

            //application.properties
            f = new File(this.resources + "/application.properties");
            writer = new FileWriter(f);

            writer.append("#Data source\n" +
                    "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver\n" +
                    "\n" +
                    "spring.datasource.url=jdbc:mysql://localhost:3306/" + nameDB + "\n" +
                    "\n" +
                    "spring.datasource.username=root\n" +
                    "spring.datasource.password=avbavbMysql\n" +
                    "\n" +
                    "spring.jpa.show-sql = true\n" +
                    "spring.jpa.hibernate.ddl-auto=update\n" +
                    "server.error.whitelabel.enabled=true\n" +
                    "server.port=" + numPuerto + "\n" +
                    "spring.servlet.multipart.max-file-size=128KB\n" +
                    "spring.servlet.multipart.max-request-size=128KB\n" +
                    "spring.servlet.multipart.enabled=true\n" +
                    "\n" +
                    "\n" +
                    "spring.main.allow-circular-references=true\n");

            writer.close();

            //messages.properties
            f = new File(this.resources + "/messages.properties");
            writer = new FileWriter(f);

            writer.append("principal.title = " + title + "\n" +
                    "principal.lang = " + defaultLanguage.getAbreviatura() + "\n" +
                    "principal.charset = UTF-8\n" +
                    "principal.home = " + defaultLanguage.getInicio() + "\n" +
                    "principal.services = " + defaultLanguage.getServicios() + "\n" +
                    "principal.errorPage = " + defaultLanguage.getPaginaError() + "\n" +
                    "principal.username = " + defaultLanguage.getUsername() + "\n" +
                    "principal.password = " + defaultLanguage.getPassword() + "\n" +
                    "principal.email = " + defaultLanguage.getEmail() + "\n" +
                    "principal.rol = " + defaultLanguage.getRol() + "\n" +
                    "principal.usuarios = " + defaultLanguage.getUsuarios() + "\n" +
                    "principal.save = " + defaultLanguage.getSave() + "\n" +
                    "principal.back = " + defaultLanguage.getBack() + "\n" +
                    "principal.login = " + defaultLanguage.getLogin() + "\n" +
                    "principal.logout = " + defaultLanguage.getLogout() + "\n" +
                    "principal.loginlogout = " + defaultLanguage.getLogin() + "/" + defaultLanguage.getLogout() + "\n" +
                    "principal.registrarUsuario = " + defaultLanguage.getRegistrarUsuario() + "\n" +
                    "principal.language = " + defaultLanguage.getIdioma() + "\n\n");

            writer.append("get.button = " + defaultLanguage.getButton_GET() + "\n" +
                    "post.button = " + defaultLanguage.getButton_POST() + "\n" +
                    "put.button = " + defaultLanguage.getButton_PUT() + "\n" +
                    "delete.button = " + defaultLanguage.getButton_DELETE());


            writer.close();

            //messages_lan.properties
            for(Idioma idioma : idiomas){
                f = new File(this.resources + "/messages_" + idioma.getAbreviatura().toLowerCase() + ".properties");
                writer = new FileWriter(f);

                writer.append("principal.title = " + title + "\n" +
                        "principal.lang = " + idioma.getAbreviatura() + "\n" +
                        "principal.charset = UTF-8\n" +
                        "principal.home = " + idioma.getInicio() + "\n" +
                        "principal.services = " + idioma.getServicios() + "\n" +
                        "principal.errorPage = " + idioma.getPaginaError() + "\n" +
                        "principal.username = " + idioma.getUsername() + "\n" +
                        "principal.password = " + idioma.getPassword() + "\n" +
                        "principal.email = " + idioma.getEmail() + "\n" +
                        "principal.rol = " + idioma.getRol() + "\n" +
                        "principal.usuarios = " + idioma.getUsuarios() + "\n" +
                        "principal.save = " + idioma.getSave() + "\n" +
                        "principal.back = " + idioma.getBack() + "\n" +
                        "principal.login = " + idioma.getLogin() + "\n" +
                        "principal.logout = " + idioma.getLogout() + "\n" +
                        "principal.loginlogout = " + idioma.getLogin() + "/" + idioma.getLogout() + "\n" +
                        "principal.registrarUsuario = " + idioma.getRegistrarUsuario() + "\n" +
                        "principal.language = " + defaultLanguage.getIdioma() + "\n\n");

                writer.append("get.button = " + idioma.getButton_GET() + "\n" +
                        "post.button = " + idioma.getButton_POST() + "\n" +
                        "put.button = " + idioma.getButton_PUT() + "\n" +
                        "delete.button = " + idioma.getButton_DELETE());

                writer.close();
            }

        }catch (Exception ex){
            throw new PropertiesException(ex.getMessage());
        }finally {
            try{
                if(writer != null) {
                    writer.close();
                }
            }catch (Exception ex){
                System.out.println(ex.getMessage());
            }
        }

    }


    /**
     * Genera los archivos pom.xml, .gitignore, mvnw, y mvnw.cmd
     */
    private void generateOthers() throws OthersException{

        File f = null;
        FileWriter writer = null;

        try{

            //pom.xml
            f = new File(projectService.getTitleProject() + "/pom.xml");
            writer = new FileWriter(f);

            writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "\txsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                    "\t<modelVersion>4.0.0</modelVersion>\n" +
                    "\t<parent>\n" +
                    "\t\t<groupId>org.springframework.boot</groupId>\n" +
                    "\t\t<artifactId>spring-boot-starter-parent</artifactId>\n" +
                    "\t\t<version>3.0.0</version>\n" +
                    "\t\t<relativePath/> <!-- lookup parent from repository -->\n" +
                    "\t</parent>\n" +
                    "\t<groupId>com.example</groupId>\n" +
                    "\t<artifactId>" + projectService.getModule() + "</artifactId>\n" +
                    "\t<version>0.0.1-SNAPSHOT</version>\n" +
                    "\t<name>" + projectService.getNameApplication() + "</name>\n" +
                    "\t<description></description>\n" +
                    "\t<properties>\n" +
                    "\t\t<java.version>17</java.version>\n" +
                    "\t</properties>\n" +
                    "\t<dependencies>\n" +
                    "\t\t<!-- Apache Commons FileUpload -->\n" +
                    "\t\t<dependency>\n" +
                    "\t\t\t<groupId>commons-fileupload</groupId>\n" +
                    "\t\t\t<artifactId>commons-fileupload</artifactId>\n" +
                    "\t\t\t<version>1.4</version>\n" +
                    "\t\t</dependency>\n" +
                    "\n" +
                    "\t\t<!-- Apache Commons IO -->\n" +
                    "\t\t<dependency>\n" +
                    "\t\t\t<groupId>commons-io</groupId>\n" +
                    "\t\t\t<artifactId>commons-io</artifactId>\n" +
                    "\t\t\t<version>2.4</version>\n" +
                    "\t\t</dependency>\n" +
                    "\t\t<dependency>\n" +
                    "\t\t\t<groupId>nz.net.ultraq.thymeleaf</groupId>\n" +
                    "\t\t\t<artifactId>thymeleaf-layout-dialect</artifactId>\n" +
                    "\t\t\t<version>3.1.0</version>\n" +
                    "\t\t</dependency>\n" +
                    "\t\t<dependency>\n" +
                    "\t\t\t<groupId>com.paypal.sdk</groupId>\n" +
                    "\t\t\t<artifactId>rest-api-sdk</artifactId>\n" +
                    "\t\t\t<version>1.4.2</version>\n" +
                    "\t\t</dependency>\n" +
                    "\t\t<dependency>\n" +
                    "\t\t\t<groupId>org.springframework.boot</groupId>\n" +
                    "\t\t\t<artifactId>spring-boot-starter-data-jpa</artifactId>\n" +
                    "\t\t</dependency>\n" +
                    "\t\t<!-- tag::security-dependencies[] -->\n" +
                    "\t\t<dependency>\n" +
                    "\t\t\t<groupId>mysql</groupId>\n" +
                    "\t\t\t<artifactId>mysql-connector-java</artifactId>\n" +
                    "\t\t\t<scope>runtime</scope>\n" +
                    "\t\t</dependency>\n" +
                    "\t\t<dependency>\n" +
                    "\t\t\t<groupId>org.springframework.boot</groupId>\n" +
                    "\t\t\t<artifactId>spring-boot-starter-thymeleaf</artifactId>\n" +
                    "\t\t</dependency>\n" +
                    "\t\t<dependency>\n" +
                    "\t\t\t<groupId>org.springframework.boot</groupId>\n" +
                    "\t\t\t<artifactId>spring-boot-starter-test</artifactId>\n" +
                    "\t\t\t<scope>test</scope>\n" +
                    "\t\t</dependency>\n" +
                    "\t\t<dependency>\n" +
                    "\t\t\t<groupId>org.projectlombok</groupId>\n" +
                    "\t\t\t<artifactId>lombok</artifactId>\n" +
                    "\t\t\t<optional>true</optional>\n" +
                    "\t\t</dependency>\n" +
                    "\t\t<dependency>\n" +
                    "\t\t\t<groupId>org.springframework.boot</groupId>\n" +
                    "\t\t\t<artifactId>spring-boot-starter-web</artifactId>\n" +
                    "\t\t</dependency>\n" +
                    "\t\t<dependency>\n" +
                    "\t\t\t<groupId>org.modelmapper</groupId>\n" +
                    "\t\t\t<artifactId>modelmapper</artifactId>\n" +
                    "\t\t\t<version>2.3.1</version>\n" +
                    "\t\t</dependency>\n" +
                    "\t\t<!--seguridad-->\n" +
                    "\t\t<dependency>\n" +
                    "\t\t\t<groupId>org.springframework.boot</groupId>\n" +
                    "\t\t\t<artifactId>spring-boot-starter-security</artifactId>\n" +
                    "\t\t\t<version>3.0.0</version>\n" +
                    "\t\t</dependency>\n" +
                    "\t\t<dependency>\n" +
                    "\t\t\t<groupId>org.thymeleaf</groupId>\n" +
                    "\t\t\t<artifactId>thymeleaf-spring6</artifactId>\n" +
                    "\t\t\t<version>3.1.0.RELEASE</version>\n" +
                    "\t\t</dependency>\n" +
                    "\t\t<dependency>\n" +
                    "\t\t\t<groupId>org.springframework.security</groupId>\n" +
                    "\t\t\t<artifactId>spring-security-core</artifactId>\n" +
                    "\t\t\t<version>6.0.0</version>\n" +
                    "\t\t</dependency>\n" +
                    "\t\t<dependency>\n" +
                    "\t\t\t<groupId>org.thymeleaf.extras</groupId>\n" +
                    "\t\t\t<artifactId>thymeleaf-extras-springsecurity6</artifactId>\n" +
                    "\t\t</dependency>\n" +
                    "\n" +
                    "\t\t<!--captcha-->\n" +
                    "\t\t<dependency>\n" +
                    "\t\t\t<groupId>org.springframework.boot</groupId>\n" +
                    "\t\t\t<artifactId>spring-boot-configuration-processor</artifactId>\n" +
                    "\t\t\t<optional>true</optional>\n" +
                    "\t\t</dependency>\n" +
                    "\t\t<!--refresco de contenidos -->\n" +
                    "\t\t<dependency>\n" +
                    "\t\t\t<groupId>org.springframework.boot</groupId>\n" +
                    "\t\t\t<artifactId>spring-boot-devtools</artifactId>\n" +
                    "\t\t\t<optional>true</optional>\n" +
                    "\t\t</dependency>\n" +
                    "        <dependency>\n" +
                    "            <groupId>org.xmlunit</groupId>\n" +
                    "            <artifactId>xmlunit-core</artifactId>\n" +
                    "            <version>2.9.0</version>\n" +
                    "        </dependency>\n" +
                    "    </dependencies>\n" +
                    "\n" +
                    "\t<build>\n" +
                    "\t\t<plugins>\n" +
                    "\t\t\t<plugin>\n" +
                    "\t\t\t\t<groupId>org.springframework.boot</groupId>\n" +
                    "\t\t\t\t<artifactId>spring-boot-maven-plugin</artifactId>\n" +
                    "\t\t\t\t<version>3.0.2</version>\n" +
                    "\t\t\t</plugin>\n" +
                    "\t\t\t<plugin>\n" +
                    "\t\t\t\t<groupId>org.apache.maven.plugins</groupId>\n" +
                    "\t\t\t\t<artifactId>maven-compiler-plugin</artifactId>\n" +
                    "\t\t\t\t<configuration>\n" +
                    "\t\t\t\t\t<source>17</source>\n" +
                    "\t\t\t\t\t<target>17</target>\n" +
                    "\t\t\t\t</configuration>\n" +
                    "\t\t\t</plugin>\n" +
                    "\t\t</plugins>\n" +
                    "\t</build>\n" +
                    "\n" +
                    "</project>");

            writer.close();

            //.gitignore
            f = new File(projectService.getTitleProject() + "/.gitignore");
            writer = new FileWriter(f);

            writer.append("HELP.md\n" +
                    "target/\n" +
                    "!.mvn/wrapper/maven-wrapper.jar\n" +
                    "!**/src/main/**/target/\n" +
                    "!**/src/test/**/target/\n" +
                    "\n" +
                    "### STS ###\n" +
                    ".apt_generated\n" +
                    ".classpath\n" +
                    ".factorypath\n" +
                    ".project\n" +
                    ".settings\n" +
                    ".springBeans\n" +
                    ".sts4-cache\n" +
                    "\n" +
                    "### IntelliJ IDEA ###\n" +
                    ".idea\n" +
                    "*.iws\n" +
                    "*.iml\n" +
                    "*.ipr\n" +
                    "\n" +
                    "### NetBeans ###\n" +
                    "/nbproject/private/\n" +
                    "/nbbuild/\n" +
                    "/dist/\n" +
                    "/nbdist/\n" +
                    "/.nb-gradle/\n" +
                    "build/\n" +
                    "!**/src/main/**/build/\n" +
                    "!**/src/test/**/build/\n" +
                    "\n" +
                    "### VS Code ###\n" +
                    ".vscode/");

            writer.close();

            //mvnw.cmd
            f = new File(projectService.getTitleProject() + "/mvnw.cmd");
            writer = new FileWriter(f);

            writer.append("@REM ----------------------------------------------------------------------------\n" +
                    "@REM Licensed to the Apache Software Foundation (ASF) under one\n" +
                    "@REM or more contributor license agreements.  See the NOTICE file\n" +
                    "@REM distributed with this work for additional information\n" +
                    "@REM regarding copyright ownership.  The ASF licenses this file\n" +
                    "@REM to you under the Apache License, Version 2.0 (the\n" +
                    "@REM \"License\"); you may not use this file except in compliance\n" +
                    "@REM with the License.  You may obtain a copy of the License at\n" +
                    "@REM\n" +
                    "@REM    https://www.apache.org/licenses/LICENSE-2.0\n" +
                    "@REM\n" +
                    "@REM Unless required by applicable law or agreed to in writing,\n" +
                    "@REM software distributed under the License is distributed on an\n" +
                    "@REM \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY\n" +
                    "@REM KIND, either express or implied.  See the License for the\n" +
                    "@REM specific language governing permissions and limitations\n" +
                    "@REM under the License.\n" +
                    "@REM ----------------------------------------------------------------------------\n" +
                    "\n" +
                    "@REM ----------------------------------------------------------------------------\n" +
                    "@REM Maven Start Up Batch script\n" +
                    "@REM\n" +
                    "@REM Required ENV vars:\n" +
                    "@REM JAVA_HOME - location of a JDK home dir\n" +
                    "@REM\n" +
                    "@REM Optional ENV vars\n" +
                    "@REM M2_HOME - location of maven2's installed home dir\n" +
                    "@REM MAVEN_BATCH_ECHO - set to 'on' to enable the echoing of the batch commands\n" +
                    "@REM MAVEN_BATCH_PAUSE - set to 'on' to wait for a keystroke before ending\n" +
                    "@REM MAVEN_OPTS - parameters passed to the Java VM when running Maven\n" +
                    "@REM     e.g. to debug Maven itself, use\n" +
                    "@REM set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000\n" +
                    "@REM MAVEN_SKIP_RC - flag to disable loading of mavenrc files\n" +
                    "@REM ----------------------------------------------------------------------------\n" +
                    "\n" +
                    "@REM Begin all REM lines with '@' in case MAVEN_BATCH_ECHO is 'on'\n" +
                    "@echo off\n" +
                    "@REM set title of command window\n" +
                    "title %0\n" +
                    "@REM enable echoing by setting MAVEN_BATCH_ECHO to 'on'\n" +
                    "@if \"%MAVEN_BATCH_ECHO%\" == \"on\"  echo %MAVEN_BATCH_ECHO%\n" +
                    "\n" +
                    "@REM set %HOME% to equivalent of $HOME\n" +
                    "if \"%HOME%\" == \"\" (set \"HOME=%HOMEDRIVE%%HOMEPATH%\")\n" +
                    "\n" +
                    "@REM Execute a user defined script before this one\n" +
                    "if not \"%MAVEN_SKIP_RC%\" == \"\" goto skipRcPre\n" +
                    "@REM check for pre script, once with legacy .bat ending and once with .cmd ending\n" +
                    "if exist \"%USERPROFILE%\\mavenrc_pre.bat\" call \"%USERPROFILE%\\mavenrc_pre.bat\" %*\n" +
                    "if exist \"%USERPROFILE%\\mavenrc_pre.cmd\" call \"%USERPROFILE%\\mavenrc_pre.cmd\" %*\n" +
                    ":skipRcPre\n" +
                    "\n" +
                    "@setlocal\n" +
                    "\n" +
                    "set ERROR_CODE=0\n" +
                    "\n" +
                    "@REM To isolate internal variables from possible post scripts, we use another setlocal\n" +
                    "@setlocal\n" +
                    "\n" +
                    "@REM ==== START VALIDATION ====\n" +
                    "if not \"%JAVA_HOME%\" == \"\" goto OkJHome\n" +
                    "\n" +
                    "echo.\n" +
                    "echo Error: JAVA_HOME not found in your environment. >&2\n" +
                    "echo Please set the JAVA_HOME variable in your environment to match the >&2\n" +
                    "echo location of your Java installation. >&2\n" +
                    "echo.\n" +
                    "goto error\n" +
                    "\n" +
                    ":OkJHome\n" +
                    "if exist \"%JAVA_HOME%\\bin\\java.exe\" goto init\n" +
                    "\n" +
                    "echo.\n" +
                    "echo Error: JAVA_HOME is set to an invalid directory. >&2\n" +
                    "echo JAVA_HOME = \"%JAVA_HOME%\" >&2\n" +
                    "echo Please set the JAVA_HOME variable in your environment to match the >&2\n" +
                    "echo location of your Java installation. >&2\n" +
                    "echo.\n" +
                    "goto error\n" +
                    "\n" +
                    "@REM ==== END VALIDATION ====\n" +
                    "\n" +
                    ":init\n" +
                    "\n" +
                    "@REM Find the project base dir, i.e. the directory that contains the folder \".mvn\".\n" +
                    "@REM Fallback to current working directory if not found.\n" +
                    "\n" +
                    "set MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%\n" +
                    "IF NOT \"%MAVEN_PROJECTBASEDIR%\"==\"\" goto endDetectBaseDir\n" +
                    "\n" +
                    "set EXEC_DIR=%CD%\n" +
                    "set WDIR=%EXEC_DIR%\n" +
                    ":findBaseDir\n" +
                    "IF EXIST \"%WDIR%\"\\.mvn goto baseDirFound\n" +
                    "cd ..\n" +
                    "IF \"%WDIR%\"==\"%CD%\" goto baseDirNotFound\n" +
                    "set WDIR=%CD%\n" +
                    "goto findBaseDir\n" +
                    "\n" +
                    ":baseDirFound\n" +
                    "set MAVEN_PROJECTBASEDIR=%WDIR%\n" +
                    "cd \"%EXEC_DIR%\"\n" +
                    "goto endDetectBaseDir\n" +
                    "\n" +
                    ":baseDirNotFound\n" +
                    "set MAVEN_PROJECTBASEDIR=%EXEC_DIR%\n" +
                    "cd \"%EXEC_DIR%\"\n" +
                    "\n" +
                    ":endDetectBaseDir\n" +
                    "\n" +
                    "IF NOT EXIST \"%MAVEN_PROJECTBASEDIR%\\.mvn\\jvm.config\" goto endReadAdditionalConfig\n" +
                    "\n" +
                    "@setlocal EnableExtensions EnableDelayedExpansion\n" +
                    "for /F \"usebackq delims=\" %%a in (\"%MAVEN_PROJECTBASEDIR%\\.mvn\\jvm.config\") do set JVM_CONFIG_MAVEN_PROPS=!JVM_CONFIG_MAVEN_PROPS! %%a\n" +
                    "@endlocal & set JVM_CONFIG_MAVEN_PROPS=%JVM_CONFIG_MAVEN_PROPS%\n" +
                    "\n" +
                    ":endReadAdditionalConfig\n" +
                    "\n" +
                    "SET MAVEN_JAVA_EXE=\"%JAVA_HOME%\\bin\\java.exe\"\n" +
                    "set WRAPPER_JAR=\"%MAVEN_PROJECTBASEDIR%\\.mvn\\wrapper\\maven-wrapper.jar\"\n" +
                    "set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain\n" +
                    "\n" +
                    "set DOWNLOAD_URL=\"https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.1.0/maven-wrapper-3.1.0.jar\"\n" +
                    "\n" +
                    "FOR /F \"usebackq tokens=1,2 delims==\" %%A IN (\"%MAVEN_PROJECTBASEDIR%\\.mvn\\wrapper\\maven-wrapper.properties\") DO (\n" +
                    "    IF \"%%A\"==\"wrapperUrl\" SET DOWNLOAD_URL=%%B\n" +
                    ")\n" +
                    "\n" +
                    "@REM Extension to allow automatically downloading the maven-wrapper.jar from Maven-central\n" +
                    "@REM This allows using the maven wrapper in projects that prohibit checking in binary data.\n" +
                    "if exist %WRAPPER_JAR% (\n" +
                    "    if \"%MVNW_VERBOSE%\" == \"true\" (\n" +
                    "        echo Found %WRAPPER_JAR%\n" +
                    "    )\n" +
                    ") else (\n" +
                    "    if not \"%MVNW_REPOURL%\" == \"\" (\n" +
                    "        SET DOWNLOAD_URL=\"%MVNW_REPOURL%/org/apache/maven/wrapper/maven-wrapper/3.1.0/maven-wrapper-3.1.0.jar\"\n" +
                    "    )\n" +
                    "    if \"%MVNW_VERBOSE%\" == \"true\" (\n" +
                    "        echo Couldn't find %WRAPPER_JAR%, downloading it ...\n" +
                    "        echo Downloading from: %DOWNLOAD_URL%\n" +
                    "    )\n" +
                    "\n" +
                    "    powershell -Command \"&{\"^\n" +
                    "\t\t\"$webclient = new-object System.Net.WebClient;\"^\n" +
                    "\t\t\"if (-not ([string]::IsNullOrEmpty('%MVNW_USERNAME%') -and [string]::IsNullOrEmpty('%MVNW_PASSWORD%'))) {\"^\n" +
                    "\t\t\"$webclient.Credentials = new-object System.Net.NetworkCredential('%MVNW_USERNAME%', '%MVNW_PASSWORD%');\"^\n" +
                    "\t\t\"}\"^\n" +
                    "\t\t\"[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $webclient.DownloadFile('%DOWNLOAD_URL%', '%WRAPPER_JAR%')\"^\n" +
                    "\t\t\"}\"\n" +
                    "    if \"%MVNW_VERBOSE%\" == \"true\" (\n" +
                    "        echo Finished downloading %WRAPPER_JAR%\n" +
                    "    )\n" +
                    ")\n" +
                    "@REM End of extension\n" +
                    "\n" +
                    "@REM Provide a \"standardized\" way to retrieve the CLI args that will\n" +
                    "@REM work with both Windows and non-Windows executions.\n" +
                    "set MAVEN_CMD_LINE_ARGS=%*\n" +
                    "\n" +
                    "%MAVEN_JAVA_EXE% ^\n" +
                    "  %JVM_CONFIG_MAVEN_PROPS% ^\n" +
                    "  %MAVEN_OPTS% ^\n" +
                    "  %MAVEN_DEBUG_OPTS% ^\n" +
                    "  -classpath %WRAPPER_JAR% ^\n" +
                    "  \"-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%\" ^\n" +
                    "  %WRAPPER_LAUNCHER% %MAVEN_CONFIG% %*\n" +
                    "if ERRORLEVEL 1 goto error\n" +
                    "goto end\n" +
                    "\n" +
                    ":error\n" +
                    "set ERROR_CODE=1\n" +
                    "\n" +
                    ":end\n" +
                    "@endlocal & set ERROR_CODE=%ERROR_CODE%\n" +
                    "\n" +
                    "if not \"%MAVEN_SKIP_RC%\"==\"\" goto skipRcPost\n" +
                    "@REM check for post script, once with legacy .bat ending and once with .cmd ending\n" +
                    "if exist \"%USERPROFILE%\\mavenrc_post.bat\" call \"%USERPROFILE%\\mavenrc_post.bat\"\n" +
                    "if exist \"%USERPROFILE%\\mavenrc_post.cmd\" call \"%USERPROFILE%\\mavenrc_post.cmd\"\n" +
                    ":skipRcPost\n" +
                    "\n" +
                    "@REM pause the script if MAVEN_BATCH_PAUSE is set to 'on'\n" +
                    "if \"%MAVEN_BATCH_PAUSE%\"==\"on\" pause\n" +
                    "\n" +
                    "if \"%MAVEN_TERMINATE_CMD%\"==\"on\" exit %ERROR_CODE%\n" +
                    "\n" +
                    "cmd /C exit /B %ERROR_CODE%");

            writer.close();

            //mvnw
            f = new File(projectService.getTitleProject() + "/mvnw");
            writer = new FileWriter(f);

            writer.append("#!/bin/sh\n" +
                    "# ----------------------------------------------------------------------------\n" +
                    "# Licensed to the Apache Software Foundation (ASF) under one\n" +
                    "# or more contributor license agreements.  See the NOTICE file\n" +
                    "# distributed with this work for additional information\n" +
                    "# regarding copyright ownership.  The ASF licenses this file\n" +
                    "# to you under the Apache License, Version 2.0 (the\n" +
                    "# \"License\"); you may not use this file except in compliance\n" +
                    "# with the License.  You may obtain a copy of the License at\n" +
                    "#\n" +
                    "#    https://www.apache.org/licenses/LICENSE-2.0\n" +
                    "#\n" +
                    "# Unless required by applicable law or agreed to in writing,\n" +
                    "# software distributed under the License is distributed on an\n" +
                    "# \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY\n" +
                    "# KIND, either express or implied.  See the License for the\n" +
                    "# specific language governing permissions and limitations\n" +
                    "# under the License.\n" +
                    "# ----------------------------------------------------------------------------\n" +
                    "\n" +
                    "# ----------------------------------------------------------------------------\n" +
                    "# Maven Start Up Batch script\n" +
                    "#\n" +
                    "# Required ENV vars:\n" +
                    "# ------------------\n" +
                    "#   JAVA_HOME - location of a JDK home dir\n" +
                    "#\n" +
                    "# Optional ENV vars\n" +
                    "# -----------------\n" +
                    "#   M2_HOME - location of maven2's installed home dir\n" +
                    "#   MAVEN_OPTS - parameters passed to the Java VM when running Maven\n" +
                    "#     e.g. to debug Maven itself, use\n" +
                    "#       set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000\n" +
                    "#   MAVEN_SKIP_RC - flag to disable loading of mavenrc files\n" +
                    "# ----------------------------------------------------------------------------\n" +
                    "\n" +
                    "if [ -z \"$MAVEN_SKIP_RC\" ] ; then\n" +
                    "\n" +
                    "  if [ -f /usr/local/etc/mavenrc ] ; then\n" +
                    "    . /usr/local/etc/mavenrc\n" +
                    "  fi\n" +
                    "\n" +
                    "  if [ -f /etc/mavenrc ] ; then\n" +
                    "    . /etc/mavenrc\n" +
                    "  fi\n" +
                    "\n" +
                    "  if [ -f \"$HOME/.mavenrc\" ] ; then\n" +
                    "    . \"$HOME/.mavenrc\"\n" +
                    "  fi\n" +
                    "\n" +
                    "fi\n" +
                    "\n" +
                    "# OS specific support.  $var _must_ be set to either true or false.\n" +
                    "cygwin=false;\n" +
                    "darwin=false;\n" +
                    "mingw=false\n" +
                    "case \"`uname`\" in\n" +
                    "  CYGWIN*) cygwin=true ;;\n" +
                    "  MINGW*) mingw=true;;\n" +
                    "  Darwin*) darwin=true\n" +
                    "    # Use /usr/libexec/java_home if available, otherwise fall back to /Library/Java/Home\n" +
                    "    # See https://developer.apple.com/library/mac/qa/qa1170/_index.html\n" +
                    "    if [ -z \"$JAVA_HOME\" ]; then\n" +
                    "      if [ -x \"/usr/libexec/java_home\" ]; then\n" +
                    "        export JAVA_HOME=\"`/usr/libexec/java_home`\"\n" +
                    "      else\n" +
                    "        export JAVA_HOME=\"/Library/Java/Home\"\n" +
                    "      fi\n" +
                    "    fi\n" +
                    "    ;;\n" +
                    "esac\n" +
                    "\n" +
                    "if [ -z \"$JAVA_HOME\" ] ; then\n" +
                    "  if [ -r /etc/gentoo-release ] ; then\n" +
                    "    JAVA_HOME=`java-config --jre-home`\n" +
                    "  fi\n" +
                    "fi\n" +
                    "\n" +
                    "if [ -z \"$M2_HOME\" ] ; then\n" +
                    "  ## resolve links - $0 may be a link to maven's home\n" +
                    "  PRG=\"$0\"\n" +
                    "\n" +
                    "  # need this for relative symlinks\n" +
                    "  while [ -h \"$PRG\" ] ; do\n" +
                    "    ls=`ls -ld \"$PRG\"`\n" +
                    "    link=`expr \"$ls\" : '.*-> \\(.*\\)$'`\n" +
                    "    if expr \"$link\" : '/.*' > /dev/null; then\n" +
                    "      PRG=\"$link\"\n" +
                    "    else\n" +
                    "      PRG=\"`dirname \"$PRG\"`/$link\"\n" +
                    "    fi\n" +
                    "  done\n" +
                    "\n" +
                    "  saveddir=`pwd`\n" +
                    "\n" +
                    "  M2_HOME=`dirname \"$PRG\"`/..\n" +
                    "\n" +
                    "  # make it fully qualified\n" +
                    "  M2_HOME=`cd \"$M2_HOME\" && pwd`\n" +
                    "\n" +
                    "  cd \"$saveddir\"\n" +
                    "  # echo Using m2 at $M2_HOME\n" +
                    "fi\n" +
                    "\n" +
                    "# For Cygwin, ensure paths are in UNIX format before anything is touched\n" +
                    "if $cygwin ; then\n" +
                    "  [ -n \"$M2_HOME\" ] &&\n" +
                    "    M2_HOME=`cygpath --unix \"$M2_HOME\"`\n" +
                    "  [ -n \"$JAVA_HOME\" ] &&\n" +
                    "    JAVA_HOME=`cygpath --unix \"$JAVA_HOME\"`\n" +
                    "  [ -n \"$CLASSPATH\" ] &&\n" +
                    "    CLASSPATH=`cygpath --path --unix \"$CLASSPATH\"`\n" +
                    "fi\n" +
                    "\n" +
                    "# For Mingw, ensure paths are in UNIX format before anything is touched\n" +
                    "if $mingw ; then\n" +
                    "  [ -n \"$M2_HOME\" ] &&\n" +
                    "    M2_HOME=\"`(cd \"$M2_HOME\"; pwd)`\"\n" +
                    "  [ -n \"$JAVA_HOME\" ] &&\n" +
                    "    JAVA_HOME=\"`(cd \"$JAVA_HOME\"; pwd)`\"\n" +
                    "fi\n" +
                    "\n" +
                    "if [ -z \"$JAVA_HOME\" ]; then\n" +
                    "  javaExecutable=\"`which javac`\"\n" +
                    "  if [ -n \"$javaExecutable\" ] && ! [ \"`expr \\\"$javaExecutable\\\" : '\\([^ ]*\\)'`\" = \"no\" ]; then\n" +
                    "    # readlink(1) is not available as standard on Solaris 10.\n" +
                    "    readLink=`which readlink`\n" +
                    "    if [ ! `expr \"$readLink\" : '\\([^ ]*\\)'` = \"no\" ]; then\n" +
                    "      if $darwin ; then\n" +
                    "        javaHome=\"`dirname \\\"$javaExecutable\\\"`\"\n" +
                    "        javaExecutable=\"`cd \\\"$javaHome\\\" && pwd -P`/javac\"\n" +
                    "      else\n" +
                    "        javaExecutable=\"`readlink -f \\\"$javaExecutable\\\"`\"\n" +
                    "      fi\n" +
                    "      javaHome=\"`dirname \\\"$javaExecutable\\\"`\"\n" +
                    "      javaHome=`expr \"$javaHome\" : '\\(.*\\)/bin'`\n" +
                    "      JAVA_HOME=\"$javaHome\"\n" +
                    "      export JAVA_HOME\n" +
                    "    fi\n" +
                    "  fi\n" +
                    "fi\n" +
                    "\n" +
                    "if [ -z \"$JAVACMD\" ] ; then\n" +
                    "  if [ -n \"$JAVA_HOME\"  ] ; then\n" +
                    "    if [ -x \"$JAVA_HOME/jre/sh/java\" ] ; then\n" +
                    "      # IBM's JDK on AIX uses strange locations for the executables\n" +
                    "      JAVACMD=\"$JAVA_HOME/jre/sh/java\"\n" +
                    "    else\n" +
                    "      JAVACMD=\"$JAVA_HOME/bin/java\"\n" +
                    "    fi\n" +
                    "  else\n" +
                    "    JAVACMD=\"`\\\\unset -f command; \\\\command -v java`\"\n" +
                    "  fi\n" +
                    "fi\n" +
                    "\n" +
                    "if [ ! -x \"$JAVACMD\" ] ; then\n" +
                    "  echo \"Error: JAVA_HOME is not defined correctly.\" >&2\n" +
                    "  echo \"  We cannot execute $JAVACMD\" >&2\n" +
                    "  exit 1\n" +
                    "fi\n" +
                    "\n" +
                    "if [ -z \"$JAVA_HOME\" ] ; then\n" +
                    "  echo \"Warning: JAVA_HOME environment variable is not set.\"\n" +
                    "fi\n" +
                    "\n" +
                    "CLASSWORLDS_LAUNCHER=org.codehaus.plexus.classworlds.launcher.Launcher\n" +
                    "\n" +
                    "# traverses directory structure from process work directory to filesystem root\n" +
                    "# first directory with .mvn subdirectory is considered project base directory\n" +
                    "find_maven_basedir() {\n" +
                    "\n" +
                    "  if [ -z \"$1\" ]\n" +
                    "  then\n" +
                    "    echo \"Path not specified to find_maven_basedir\"\n" +
                    "    return 1\n" +
                    "  fi\n" +
                    "\n" +
                    "  basedir=\"$1\"\n" +
                    "  wdir=\"$1\"\n" +
                    "  while [ \"$wdir\" != '/' ] ; do\n" +
                    "    if [ -d \"$wdir\"/.mvn ] ; then\n" +
                    "      basedir=$wdir\n" +
                    "      break\n" +
                    "    fi\n" +
                    "    # workaround for JBEAP-8937 (on Solaris 10/Sparc)\n" +
                    "    if [ -d \"${wdir}\" ]; then\n" +
                    "      wdir=`cd \"$wdir/..\"; pwd`\n" +
                    "    fi\n" +
                    "    # end of workaround\n" +
                    "  done\n" +
                    "  echo \"${basedir}\"\n" +
                    "}\n" +
                    "\n" +
                    "# concatenates all lines of a file\n" +
                    "concat_lines() {\n" +
                    "  if [ -f \"$1\" ]; then\n" +
                    "    echo \"$(tr -s '\\n' ' ' < \"$1\")\"\n" +
                    "  fi\n" +
                    "}\n" +
                    "\n" +
                    "BASE_DIR=`find_maven_basedir \"$(pwd)\"`\n" +
                    "if [ -z \"$BASE_DIR\" ]; then\n" +
                    "  exit 1;\n" +
                    "fi\n" +
                    "\n" +
                    "##########################################################################################\n" +
                    "# Extension to allow automatically downloading the maven-wrapper.jar from Maven-central\n" +
                    "# This allows using the maven wrapper in projects that prohibit checking in binary data.\n" +
                    "##########################################################################################\n" +
                    "if [ -r \"$BASE_DIR/.mvn/wrapper/maven-wrapper.jar\" ]; then\n" +
                    "    if [ \"$MVNW_VERBOSE\" = true ]; then\n" +
                    "      echo \"Found .mvn/wrapper/maven-wrapper.jar\"\n" +
                    "    fi\n" +
                    "else\n" +
                    "    if [ \"$MVNW_VERBOSE\" = true ]; then\n" +
                    "      echo \"Couldn't find .mvn/wrapper/maven-wrapper.jar, downloading it ...\"\n" +
                    "    fi\n" +
                    "    if [ -n \"$MVNW_REPOURL\" ]; then\n" +
                    "      jarUrl=\"$MVNW_REPOURL/org/apache/maven/wrapper/maven-wrapper/3.1.0/maven-wrapper-3.1.0.jar\"\n" +
                    "    else\n" +
                    "      jarUrl=\"https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.1.0/maven-wrapper-3.1.0.jar\"\n" +
                    "    fi\n" +
                    "    while IFS=\"=\" read key value; do\n" +
                    "      case \"$key\" in (wrapperUrl) jarUrl=\"$value\"; break ;;\n" +
                    "      esac\n" +
                    "    done < \"$BASE_DIR/.mvn/wrapper/maven-wrapper.properties\"\n" +
                    "    if [ \"$MVNW_VERBOSE\" = true ]; then\n" +
                    "      echo \"Downloading from: $jarUrl\"\n" +
                    "    fi\n" +
                    "    wrapperJarPath=\"$BASE_DIR/.mvn/wrapper/maven-wrapper.jar\"\n" +
                    "    if $cygwin; then\n" +
                    "      wrapperJarPath=`cygpath --path --windows \"$wrapperJarPath\"`\n" +
                    "    fi\n" +
                    "\n" +
                    "    if command -v wget > /dev/null; then\n" +
                    "        if [ \"$MVNW_VERBOSE\" = true ]; then\n" +
                    "          echo \"Found wget ... using wget\"\n" +
                    "        fi\n" +
                    "        if [ -z \"$MVNW_USERNAME\" ] || [ -z \"$MVNW_PASSWORD\" ]; then\n" +
                    "            wget \"$jarUrl\" -O \"$wrapperJarPath\" || rm -f \"$wrapperJarPath\"\n" +
                    "        else\n" +
                    "            wget --http-user=$MVNW_USERNAME --http-password=$MVNW_PASSWORD \"$jarUrl\" -O \"$wrapperJarPath\" || rm -f \"$wrapperJarPath\"\n" +
                    "        fi\n" +
                    "    elif command -v curl > /dev/null; then\n" +
                    "        if [ \"$MVNW_VERBOSE\" = true ]; then\n" +
                    "          echo \"Found curl ... using curl\"\n" +
                    "        fi\n" +
                    "        if [ -z \"$MVNW_USERNAME\" ] || [ -z \"$MVNW_PASSWORD\" ]; then\n" +
                    "            curl -o \"$wrapperJarPath\" \"$jarUrl\" -f\n" +
                    "        else\n" +
                    "            curl --user $MVNW_USERNAME:$MVNW_PASSWORD -o \"$wrapperJarPath\" \"$jarUrl\" -f\n" +
                    "        fi\n" +
                    "\n" +
                    "    else\n" +
                    "        if [ \"$MVNW_VERBOSE\" = true ]; then\n" +
                    "          echo \"Falling back to using Java to download\"\n" +
                    "        fi\n" +
                    "        javaClass=\"$BASE_DIR/.mvn/wrapper/MavenWrapperDownloader.java\"\n" +
                    "        # For Cygwin, switch paths to Windows format before running javac\n" +
                    "        if $cygwin; then\n" +
                    "          javaClass=`cygpath --path --windows \"$javaClass\"`\n" +
                    "        fi\n" +
                    "        if [ -e \"$javaClass\" ]; then\n" +
                    "            if [ ! -e \"$BASE_DIR/.mvn/wrapper/MavenWrapperDownloader.class\" ]; then\n" +
                    "                if [ \"$MVNW_VERBOSE\" = true ]; then\n" +
                    "                  echo \" - Compiling MavenWrapperDownloader.java ...\"\n" +
                    "                fi\n" +
                    "                # Compiling the Java class\n" +
                    "                (\"$JAVA_HOME/bin/javac\" \"$javaClass\")\n" +
                    "            fi\n" +
                    "            if [ -e \"$BASE_DIR/.mvn/wrapper/MavenWrapperDownloader.class\" ]; then\n" +
                    "                # Running the downloader\n" +
                    "                if [ \"$MVNW_VERBOSE\" = true ]; then\n" +
                    "                  echo \" - Running MavenWrapperDownloader.java ...\"\n" +
                    "                fi\n" +
                    "                (\"$JAVA_HOME/bin/java\" -cp .mvn/wrapper MavenWrapperDownloader \"$MAVEN_PROJECTBASEDIR\")\n" +
                    "            fi\n" +
                    "        fi\n" +
                    "    fi\n" +
                    "fi\n" +
                    "##########################################################################################\n" +
                    "# End of extension\n" +
                    "##########################################################################################\n" +
                    "\n" +
                    "export MAVEN_PROJECTBASEDIR=${MAVEN_BASEDIR:-\"$BASE_DIR\"}\n" +
                    "if [ \"$MVNW_VERBOSE\" = true ]; then\n" +
                    "  echo $MAVEN_PROJECTBASEDIR\n" +
                    "fi\n" +
                    "MAVEN_OPTS=\"$(concat_lines \"$MAVEN_PROJECTBASEDIR/.mvn/jvm.config\") $MAVEN_OPTS\"\n" +
                    "\n" +
                    "# For Cygwin, switch paths to Windows format before running java\n" +
                    "if $cygwin; then\n" +
                    "  [ -n \"$M2_HOME\" ] &&\n" +
                    "    M2_HOME=`cygpath --path --windows \"$M2_HOME\"`\n" +
                    "  [ -n \"$JAVA_HOME\" ] &&\n" +
                    "    JAVA_HOME=`cygpath --path --windows \"$JAVA_HOME\"`\n" +
                    "  [ -n \"$CLASSPATH\" ] &&\n" +
                    "    CLASSPATH=`cygpath --path --windows \"$CLASSPATH\"`\n" +
                    "  [ -n \"$MAVEN_PROJECTBASEDIR\" ] &&\n" +
                    "    MAVEN_PROJECTBASEDIR=`cygpath --path --windows \"$MAVEN_PROJECTBASEDIR\"`\n" +
                    "fi\n" +
                    "\n" +
                    "# Provide a \"standardized\" way to retrieve the CLI args that will\n" +
                    "# work with both Windows and non-Windows executions.\n" +
                    "MAVEN_CMD_LINE_ARGS=\"$MAVEN_CONFIG $@\"\n" +
                    "export MAVEN_CMD_LINE_ARGS\n" +
                    "\n" +
                    "WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain\n" +
                    "\n" +
                    "exec \"$JAVACMD\" \\\n" +
                    "  $MAVEN_OPTS \\\n" +
                    "  $MAVEN_DEBUG_OPTS \\\n" +
                    "  -classpath \"$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar\" \\\n" +
                    "  \"-Dmaven.home=${M2_HOME}\" \\\n" +
                    "  \"-Dmaven.multiModuleProjectDirectory=${MAVEN_PROJECTBASEDIR}\" \\\n" +
                    "  ${WRAPPER_LAUNCHER} $MAVEN_CONFIG \"$@\"\n");

            writer.close();


            //.mvn
            f = new File(projectService.getTitleProject() + "/.mvn/wrapper");
            if(!f.mkdirs()){
                throw new OthersException("Error al crear la carpeta .mvn o wrapper");
            }

            File jar = new File(".mvn/wrapper/maven-wrapper.jar");
            File properties = new File(".mvn/wrapper/maven-wrapper.properties");
            File destJar = new File(projectService.getTitleProject() + "/.mvn/wrapper/maven-wrapper.jar");
            File destProperties = new File(projectService.getTitleProject() + "/.mvn/wrapper/maven-wrapper.properties");

            Files.copy(jar.toPath(), destJar.toPath());
            Files.copy(properties.toPath(), destProperties.toPath());


            //Bootstrap
            f = new File(projectService.getTitleProject() + "/Bootstrap");
            if(!f.mkdir()){
                throw new OthersException("Error al crear la carpeta Bootstrap.");
            }

            f = new File(projectService.getTitleProject() + "/Bootstrap/css");
            if(!f.mkdir()){
                throw new OthersException("Error al crear la carpeta css. Path: Bootstrap/css");
            }

            File bootstrap = new File("plantilla/css/bootstrap.min.css");
            File style = new File("plantilla/css/style.css");
            File destBoostrap = new File(projectService.getTitleProject() + "/Bootstrap/css/bootstrap.min.css");
            File destStyle = new File(projectService.getTitleProject() + "/Bootstrap/css/style.css");

            Files.copy(bootstrap.toPath(), destBoostrap.toPath());
            Files.copy(style.toPath(), destStyle.toPath());

            //css bootstrap
            File animate = new File("src/main/resources/static/css/animate.css");
            File bootstrap2 = new File("src/main/resources/static/css/bootstrap.css");
            File flexslider = new File("src/main/resources/static/css/flexslider.css");
            File icomoon = new File("src/main/resources/static/css/icomoon.css");
            File style2 = new File("src/main/resources/static/css/style.css");
            File superfish = new File("src/main/resources/static/css/superfish.css");

            File destAnimate = new File(projectService.getTitleProject() + "/src/main/resources/static/css/animate.css");
            File destBootstrap2 = new File(projectService.getTitleProject() + "/src/main/resources/static/css/bootstrap.css");
            File destFlexslider = new File(projectService.getTitleProject() + "/src/main/resources/static/css/flexslider.css");
            File destIcomoon = new File(projectService.getTitleProject() + "/src/main/resources/static/css/icomoon.css");
            File destStyle2 = new File(projectService.getTitleProject() + "/src/main/resources/static/css/style.css");
            File destSuperfish = new File(projectService.getTitleProject() + "/src/main/resources/static/css/superfish.css");

            Files.copy(animate.toPath(), destAnimate.toPath());
            Files.copy(bootstrap2.toPath(), destBootstrap2.toPath());
            Files.copy(flexslider.toPath(), destFlexslider.toPath());
            Files.copy(icomoon.toPath(), destIcomoon.toPath());
            Files.copy(style2.toPath(), destStyle2.toPath());
            Files.copy(superfish.toPath(), destSuperfish.toPath());

            //.idea
            f = new File(projectService.getTitleProject() + "/.idea");
            if(!f.mkdir()){
                throw new OthersException("Error al crear la carpeta \".idea\".");
            }

            f = new File(projectService.getTitleProject() + "/.idea/.gitignore");
            writer = new FileWriter(f);

            writer.append("# Default ignored files\n" +
                    "/shelf/\n" +
                    "/workspace.xml\n" +
                    "# Editor-based HTTP Client requests\n" +
                    "/httpRequests/\n" +
                    "# Datasource local storage ignored files\n" +
                    "/dataSources/\n" +
                    "/dataSources.local.xml");

            writer.close();

            //compiler.xml
            f = new File(projectService.getTitleProject() + "/.idea/compiler.xml");
            writer = new FileWriter(f);

            writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<project version=\"4\">\n" +
                    "  <component name=\"CompilerConfiguration\">\n" +
                    "    <annotationProcessing>\n" +
                    "      <profile name=\"Maven default annotation processors profile\" enabled=\"true\">\n" +
                    "        <sourceOutputDir name=\"target/generated-sources/annotations\" />\n" +
                    "        <sourceTestOutputDir name=\"target/generated-test-sources/test-annotations\" />\n" +
                    "        <outputRelativeToContentRoot value=\"true\" />\n" +
                    "        <module name=\"" + projectService.getModule() + "\" />\n" +
                    "      </profile>\n" +
                    "    </annotationProcessing>\n" +
                    "  </component>\n" +
                    "  <component name=\"JavacSettings\">\n" +
                    "    <option name=\"ADDITIONAL_OPTIONS_OVERRIDE\">\n" +
                    "      <module name=\"" + projectService.getModule() + "\" options=\"-parameters\" />\n" +
                    "    </option>\n" +
                    "  </component>\n" +
                    "</project>");

            writer.close();

            //encodings.xml
            f = new File(projectService.getTitleProject() + "/.idea/encodings.xml");
            writer = new FileWriter(f);

            writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<project version=\"4\">\n" +
                    "  <component name=\"Encoding\">\n" +
                    "    <file url=\"file://$PROJECT_DIR$/src/main/java\" charset=\"UTF-8\" />\n" +
                    "  </component>\n" +
                    "</project>");

            writer.close();

            //jarRepositories.xml
            f = new File(projectService.getTitleProject() + "/.idea/jarRepositories.xml");
            writer = new FileWriter(f);

            writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<project version=\"4\">\n" +
                    "  <component name=\"RemoteRepositoriesConfiguration\">\n" +
                    "    <remote-repository>\n" +
                    "      <option name=\"id\" value=\"central\" />\n" +
                    "      <option name=\"name\" value=\"Central Repository\" />\n" +
                    "      <option name=\"url\" value=\"https://repo.maven.apache.org/maven2\" />\n" +
                    "    </remote-repository>\n" +
                    "    <remote-repository>\n" +
                    "      <option name=\"id\" value=\"central\" />\n" +
                    "      <option name=\"name\" value=\"Maven Central repository\" />\n" +
                    "      <option name=\"url\" value=\"https://repo1.maven.org/maven2\" />\n" +
                    "    </remote-repository>\n" +
                    "    <remote-repository>\n" +
                    "      <option name=\"id\" value=\"jboss.community\" />\n" +
                    "      <option name=\"name\" value=\"JBoss Community repository\" />\n" +
                    "      <option name=\"url\" value=\"https://repository.jboss.org/nexus/content/repositories/public/\" />\n" +
                    "    </remote-repository>\n" +
                    "  </component>\n" +
                    "</project>");

            writer.close();

            //misc.xml
            f = new File(projectService.getTitleProject() + "/.idea/misc.xml");
            writer = new FileWriter(f);

            writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<project version=\"4\">\n" +
                    "  <component name=\"ExternalStorageConfigurationManager\" enabled=\"true\" />\n" +
                    "  <component name=\"MavenProjectsManager\">\n" +
                    "    <option name=\"originalFiles\">\n" +
                    "      <list>\n" +
                    "        <option value=\"$PROJECT_DIR$/pom.xml\" />\n" +
                    "      </list>\n" +
                    "    </option>\n" +
                    "  </component>\n" +
                    "  <component name=\"ProjectRootManager\" version=\"2\" languageLevel=\"JDK_19\" default=\"true\" project-jdk-name=\"19\" project-jdk-type=\"JavaSDK\" />\n" +
                    "</project>");

            writer.close();

            //workspace.xml
            f = new File(projectService.getTitleProject() + "/.idea/workspace.xml");
            writer = new FileWriter(f);

            writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<project version=\"4\">\n" +
                    "  <component name=\"AutoImportSettings\">\n" +
                    "    <option name=\"autoReloadType\" value=\"SELECTIVE\" />\n" +
                    "  </component>\n" +
                    "  <component name=\"ChangeListManager\">\n" +
                    "    <list default=\"true\" id=\"7705e50e-80ea-49c7-8a6a-26513a3af206\" name=\"Changes\" comment=\"\" />\n" +
                    "    <option name=\"SHOW_DIALOG\" value=\"false\" />\n" +
                    "    <option name=\"HIGHLIGHT_CONFLICTS\" value=\"true\" />\n" +
                    "    <option name=\"HIGHLIGHT_NON_ACTIVE_CHANGELIST\" value=\"false\" />\n" +
                    "    <option name=\"LAST_RESOLUTION\" value=\"IGNORE\" />\n" +
                    "  </component>\n" +
                    "  <component name=\"MavenImportPreferences\">\n" +
                    "    <option name=\"generalSettings\">\n" +
                    "      <MavenGeneralSettings>\n" +
                    "        <option name=\"mavenHome\" value=\"Use Maven wrapper\" />\n" +
                    "      </MavenGeneralSettings>\n" +
                    "    </option>\n" +
                    "  </component>\n" +
                    "  <component name=\"ProjectColorInfo\"><![CDATA[{\n" +
                    "  \"customColor\": \"\",\n" +
                    "  \"associatedIndex\": 0\n" +
                    "}]]></component>\n" +
                    "  <component name=\"ProjectId\" id=\"2frVOs6UPFd57l3deCKSi7w3pOY\" />\n" +
                    "  <component name=\"ProjectViewState\">\n" +
                    "    <option name=\"hideEmptyMiddlePackages\" value=\"true\" />\n" +
                    "    <option name=\"showLibraryContents\" value=\"true\" />\n" +
                    "  </component>\n" +
                    "  <component name=\"PropertiesComponent\"><![CDATA[{\n" +
                    "  \"keyToString\": {\n" +
                    "    \"RunOnceActivity.OpenProjectViewOnStart\": \"true\",\n" +
                    "    \"RunOnceActivity.ShowReadmeOnStart\": \"true\",\n" +
                    "    \"WebServerToolWindowFactoryState\": \"false\",\n" +
                    "    \"node.js.detected.package.eslint\": \"true\",\n" +
                    "    \"node.js.detected.package.tslint\": \"true\",\n" +
                    "    \"node.js.selected.package.eslint\": \"(autodetect)\",\n" +
                    "    \"node.js.selected.package.tslint\": \"(autodetect)\",\n" +
                    "    \"vue.rearranger.settings.migration\": \"true\"\n" +
                    "  }\n" +
                    "}]]></component>\n" +
                    "  <component name=\"RunManager\">\n" +
                    "    <configuration name=\"" + projectService.getNameApplication() + "Application\" type=\"SpringBootApplicationConfigurationType\" factoryName=\"Spring Boot\" nameIsGenerated=\"true\">\n" +
                    "      <option name=\"FRAME_DEACTIVATION_UPDATE_POLICY\" value=\"UpdateClassesAndResources\" />\n" +
                    "      <module name=\"" + projectService.getModule() + "\" />\n" +
                    "      <option name=\"SPRING_BOOT_MAIN_CLASS\" value=\"" + this.pathCode + "." + projectService.getNameApplication() + "Application\" />\n" +
                    "      <method v=\"2\">\n" +
                    "        <option name=\"Make\" enabled=\"true\" />\n" +
                    "      </method>\n" +
                    "    </configuration>\n" +
                    "  </component>\n" +
                    "  <component name=\"SpellCheckerSettings\" RuntimeDictionaries=\"0\" Folders=\"0\" CustomDictionaries=\"0\" DefaultDictionary=\"application-level\" UseSingleDictionary=\"true\" transferred=\"true\" />\n" +
                    "  <component name=\"TaskManager\">\n" +
                    "    <task active=\"true\" id=\"Default\" summary=\"Default task\">\n" +
                    "      <changelist id=\"7705e50e-80ea-49c7-8a6a-26513a3af206\" name=\"Changes\" comment=\"\" />\n" +
                    "      <created>1714559156160</created>\n" +
                    "      <option name=\"number\" value=\"Default\" />\n" +
                    "      <option name=\"presentableId\" value=\"Default\" />\n" +
                    "      <updated>1714559156160</updated>\n" +
                    "      <workItem from=\"1714559157433\" duration=\"620000\" />\n" +
                    "    </task>\n" +
                    "    <servers />\n" +
                    "  </component>\n" +
                    "  <component name=\"TypeScriptGeneratedFilesManager\">\n" +
                    "    <option name=\"version\" value=\"3\" />\n" +
                    "  </component>\n" +
                    "</project>");

            writer.close();

        }catch (Exception ex){
            throw new OthersException(ex.getMessage());
        }finally {
            try{
                if(writer != null) {
                    writer.close();
                }
            }catch (Exception ex){
                System.out.println(ex.getMessage());
            }
        }

    }


    /**
     * Copia las imagenes que se necesitan para el proyecto a generar.
     * @throws ImagesException Excepcion
     */
    private void copyImages() throws ImagesException{

        //Source
        File get = new File("src/main/resources/static/images/CRUDoperations/get.png");
        File post = new File("src/main/resources/static/images/CRUDoperations/post.png");
        File put = new File("src/main/resources/static/images/CRUDoperations/put.png");
        File delete = new File("src/main/resources/static/images/CRUDoperations/delete.png");
        File home = new File("src/main/resources/static/images/home.png");
        File acceso = new File("src/main/resources/static/images/acceso.png");

        //Destination
        File destGet = new File(this.resources + "/static/images/CRUDoperations/get.png");
        File destPost = new File(this.resources + "/static/images/CRUDoperations/post.png");
        File destPut = new File(this.resources + "/static/images/CRUDoperations/put.png");
        File destDelete = new File(this.resources + "/static/images/CRUDoperations/delete.png");
        File destHome = new File(this.resources + "/static/images/home.png");
        File destAcceso = new File(this.resources + "/static/images/acceso.png");

        try {
            Files.copy(get.toPath(), destGet.toPath());
            Files.copy(post.toPath(), destPost.toPath());
            Files.copy(put.toPath(), destPut.toPath());
            Files.copy(delete.toPath(), destDelete.toPath());
            Files.copy(home.toPath(), destHome.toPath());
            Files.copy(acceso.toPath(), destAcceso.toPath());
        }catch (Exception ex){
            throw new ImagesException(ex.getMessage());
        }


    }


}

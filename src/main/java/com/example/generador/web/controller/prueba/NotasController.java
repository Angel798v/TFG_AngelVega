package com.example.generador.web.controller.prueba;


import com.example.generador.dto.prueba.AlumnoDto;
import com.example.generador.dto.prueba.NotasDto;
import com.example.generador.dto.prueba.ProfesorDto;
import com.example.generador.dto.prueba.RevisionDto;
import com.example.generador.model.Usuario;
import com.example.generador.model.prueba.Alumno;
import com.example.generador.model.prueba.Notas;
import com.example.generador.model.prueba.Profesor;
import com.example.generador.model.prueba.Revision;
import com.example.generador.repository.UsuarioRepository;
import com.example.generador.repository.prueba.AlumnoRepository;
import com.example.generador.repository.prueba.NotasRepository;
import com.example.generador.repository.prueba.ProfesorRepository;
import com.example.generador.repository.prueba.RevisionRepository;
import com.example.generador.service.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@Controller
@RequestMapping("/Notas")
public class NotasController {

    @Autowired
    private NotasRepository notasRepository;

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private RevisionRepository revisionRepository;

    @Autowired
    private ProfesorRepository profesorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UrlService urlService;


    //Principal entity operations view

    @GetMapping
    public String principalNotas(){

        urlService.setUrl("/Notas");

        return "/Notas/principalNotas";
    }

    //Operation GET

    @GetMapping("/Get")
    public String operationGetNotas(Model model){

        urlService.setUrl("/Notas/Get");

        model.addAttribute("listAll", notasRepository.findAll());

        return "/Notas/operationGet/operationGetNotas";
    }

    //COMPACT - GET

    @GetMapping("/Getcompact")
    public String viewNotas(Model model){

        urlService.setUrl("/viewNotas");

        model.addAttribute("listAll", notasRepository.findAll());

        return "Notas/compact/viewNotas";
    }

    //Operation POST

    @GetMapping("/Post")
    public String operationPostNotas(Model model){

        urlService.setUrl("/Notas/Post");

        model.addAttribute("object", new NotasDto());
        model.addAttribute("listAlumno", alumnoRepository.findAll());    //Relacion ManyToOne
        model.addAttribute("listRevision", revisionRepository.findAll());   //Relacion OneToOne IGUAL
        model.addAttribute("listProfesor", profesorRepository.findAll());   //Relacion ManyToMany


        return "/Notas/operationPost/operationPostNotas";
    }

    @PostMapping("/Post")
    public String operationPost(NotasDto notasDto){

        //Relacion ManyToOne con Alumno
        Alumno alumno = alumnoRepository.findById(notasDto.getAlumnoDto().getId());

        //Relacion OneToOne con Revision
        if(notasRepository.findByRevisionId(notasDto.getRevisionDto().getId()) != null){
            return "redirect:/Notas?failPost";
        }
        Revision revision = revisionRepository.findById(notasDto.getRevisionDto().getId());

        //Relacion ManyToMany con Profesor
        Set<Profesor> profesores = new HashSet<Profesor>();
        for(String profesorDtoId : notasDto.getProfesorDto()){
            profesores.add(profesorRepository.findById(Long.parseLong(profesorDtoId)));
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User usuarioConectado = (User) auth.getPrincipal();
        Usuario usuario = usuarioRepository.findByNombreUsuario(usuarioConectado.getUsername());


        Notas notas = new Notas();
        notas.setNombreAlumno(notasDto.getNombreAlumno()); //Atributo
        notas.setNota(notasDto.getNota());  //Atributo
        notas.setAlumno(alumno);    //Relacion ManyToOne
        notas.setRevision(revision);    //Relacion OneToOne IGUAL QUE ManyToOne
        for(Profesor profesor : profesores){
            notas.addProfesor(profesor);    //Relacion ManyToMany
        }
        notas.setUsuario(usuario);


        notasRepository.save(notas);

        return "redirect:/Notas?successPost";
    }

    //Operation PUT

    @GetMapping("/Put")
    public String operationPutNotas(Model model){

        urlService.setUrl("/Notas/Put");
        model.addAttribute("listAll", notasRepository.findAll());

        return "/Notas/operationPut/operationPutNotasView";
    }


    @GetMapping("/Put/{id}")
    public String operationPutNotas(@PathVariable("id") long id, Model model){

        Notas notas = notasRepository.findById(id);
        NotasDto notasDto = new NotasDto();

        notasDto.setId(notas.getId());
        notasDto.setNombreAlumno(notas.getNombreAlumno());  //Atributo
        notasDto.setNota(notas.getNota());  //Atributo

        AlumnoDto alumnoDto = new AlumnoDto();  //Relacion ManyToOne
        alumnoDto.setId(notas.getAlumno().getId());
        notasDto.setAlumnoDto(alumnoDto);

        RevisionDto revisionDto = new RevisionDto();    //Relacion OneToOne IGUAL QUE ManyToOne
        revisionDto.setId(notas.getRevision().getId());
        notasDto.setRevisionDto(revisionDto);

        String[] profesorDto = new String[]{};      //Relacion ManyToMany
        notasDto.setProfesorDto(profesorDto);

        model.addAttribute("object", notasDto);
        model.addAttribute("listAlumno", alumnoRepository.findAll());    //Relacion ManyToOne
        model.addAttribute("listRevision", revisionRepository.findAll());   //Relacion OneToOne
        model.addAttribute("listProfesor", profesorRepository.findAll());   //Relacion ManyToMany


        return "/Notas/operationPut/operationPutNotas";
    }


    @PostMapping("/Put/{id}")
    public String operationPut(@PathVariable("id") long id, NotasDto notasDto){

        //Relacion ManyToOne con Alumno
        Alumno alumno = alumnoRepository.findById(notasDto.getAlumnoDto().getId());

        //Relacion OneToOne con Revision
        if(notasRepository.findByRevisionId(notasDto.getRevisionDto().getId()) != null &&
                notasRepository.findByRevisionId(notasDto.getRevisionDto().getId()).getId() != notasDto.getId() ){
            return "redirect:/Notas?failPut";
        }
        Revision revision = revisionRepository.findById(notasDto.getRevisionDto().getId());

        //Relacion ManyToMany con Profesor
        Set<Profesor> profesores = new HashSet<Profesor>();
        for(String profesor : notasDto.getProfesorDto()){
            profesores.add(profesorRepository.findById(Long.parseLong(profesor)));
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User usuarioConectado = (User) auth.getPrincipal();
        Usuario usuario = usuarioRepository.findByNombreUsuario(usuarioConectado.getUsername());

        Notas notas = notasRepository.findById(id);
        notas.setNombreAlumno(notasDto.getNombreAlumno());  //Atributo
        notas.setNota(notasDto.getNota());  //Atributo
        notas.setAlumno(alumno);    //Relacion ManyToOne
        notas.setRevision(revision);    //Relacion OneToOne IGUAL
        notas.setProfesores(profesores);    //Relacion ManyToMany
        notas.setUsuario(usuario);

        notasRepository.save(notas);

        return "redirect:/Notas?successPut";
    }

    //Operation DELETE

    @GetMapping("/Delete")
    public String operationDeleteNotas(Model model){

        urlService.setUrl("/Notas/Delete");
        model.addAttribute("listAll", notasRepository.findAll());

        return "/Notas/operationDelete/operationDeleteNotas";
    }


    @PostMapping("/Delete/{id}")
    public String operationDelete(@PathVariable("id") long id){


        try {
            notasRepository.deleteById(id);
        }catch (Exception ex){
            return "redirect:/Notas?failDelete";
        }

        return "redirect:/Notas?successDelete";
    }






}

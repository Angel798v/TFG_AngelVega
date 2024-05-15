package com.example.generador.service;

import com.example.generador.model.Project;
import com.example.generador.repository.ProjectRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Getter
@Setter
@Service
public class ProjectService {

    private String titleProject;

    private String abbreviatedTitleProject;

    private String module;

    private String nameApplication;

    private String nameDB;

    private int numPuerto;

    private boolean datosIntroducidos;

    @Autowired
    private ProjectRepository projectRepository;

    /**
     * Guarda la información del proyecto en la base de datos
     * @return
     */
    public void guardarProyecto(Project project) {

        projectRepository.save(project);
    }
}

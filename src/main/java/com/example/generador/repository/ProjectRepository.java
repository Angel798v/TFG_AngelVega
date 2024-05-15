package com.example.generador.repository;

import com.example.generador.dto.ProjectDto;
import com.example.generador.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project,Integer> {

}

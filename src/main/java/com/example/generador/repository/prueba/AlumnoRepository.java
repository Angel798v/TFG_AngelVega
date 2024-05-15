package com.example.generador.repository.prueba;

import com.example.generador.model.prueba.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlumnoRepository extends JpaRepository<Alumno,Long> {

    Alumno findById(long id);

}

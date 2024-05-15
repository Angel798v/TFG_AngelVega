package com.example.generador.repository.prueba;

import com.example.generador.model.prueba.Profesor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfesorRepository extends JpaRepository<Profesor,Long> {

    Profesor findById(long id);
}

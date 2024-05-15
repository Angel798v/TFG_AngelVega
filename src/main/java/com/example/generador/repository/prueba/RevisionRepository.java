package com.example.generador.repository.prueba;

import com.example.generador.model.prueba.Revision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RevisionRepository extends JpaRepository<Revision,Long> {

    Revision findById(long id);

}

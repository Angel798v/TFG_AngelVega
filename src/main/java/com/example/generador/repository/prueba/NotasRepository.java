package com.example.generador.repository.prueba;

import com.example.generador.model.prueba.Notas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotasRepository extends JpaRepository<Notas,Long> {

    Notas findById(long id);

    void deleteById(long id);

    Notas findByRevisionId(long id);    //OneToOne

}

package com.example.workshop.repository;

import com.example.workshop.model.Tipo_servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Tipo_servicioRepository extends JpaRepository<Tipo_servicio,Long> {

	Tipo_servicio findById(long id);

	void deleteById(long id);

}
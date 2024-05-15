package com.example.workshop.repository;

import com.example.workshop.model.Mecanico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MecanicoRepository extends JpaRepository<Mecanico,Long> {

	Mecanico findById(long id);

	void deleteById(long id);

}
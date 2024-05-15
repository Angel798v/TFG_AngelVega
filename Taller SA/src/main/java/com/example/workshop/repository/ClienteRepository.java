package com.example.workshop.repository;

import com.example.workshop.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente,Long> {

	Cliente findById(long id);

	void deleteById(long id);

}
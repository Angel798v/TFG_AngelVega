package com.example.jpa_formacion.repository;

import com.example.jpa_formacion.model.TablaIzqEmbeddable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TablaIzqConDupRepository extends JpaRepository<TablaIzqEmbeddable,Long> {
}
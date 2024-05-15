package com.example.workshop.model;

import jakarta.persistence.*;
import java.io.Serializable;
import lombok.*;
import java.util.*;
import java.sql.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Mecanico")
public class Mecanico implements Serializable {

	public Mecanico(String nombre) {
		this.nombre = nombre;
	}


	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id_mecanico;

	@Column(name = "nombre", length = 20, nullable = true, unique = false)
	private String nombre;


	@OneToMany(mappedBy="mecanico")
	private Set<Reparacion> reparacions;

	public boolean addReparacion(Reparacion reparacion){
		if(reparacions == null){
			reparacions = new HashSet<Reparacion>();
		}

		if(reparacion != null && !reparacions.contains(reparacion)){
			if(reparacion.getMecanico() != null){
				reparacion.getMecanico().getReparacions().remove(reparacion);
			}
			reparacion.setMecanico(this);
			return true;
		}

		return false;
	}


	@ManyToOne
	@JoinColumn(name = "id_user")
	private User user;


	@Override
	public String toString(){

		return id_mecanico + "-" + nombre;
	}


}
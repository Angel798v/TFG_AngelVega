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
@Table(name = "Tipo_servicio")
public class Tipo_servicio implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id_tipo_servicio;

	@ManyToMany(mappedBy="tipo_servicios")
	private Set<Reparacion> reparacions;

	public void addReparacions(Reparacion reparacion){
		if(reparacions == null){
			reparacions = new HashSet<Reparacion>();
		}

		if(reparacion != null && !reparacions.contains(reparacion)){
			if(!reparacion.getTipo_servicios().contains(this)){
				reparacion.getTipo_servicios().add(this);
			}
			reparacions.add(reparacion);
		}
	}


	@ManyToOne
	@JoinColumn(name = "id_user")
	private User user;


	@Override
	public String toString(){

		return "" + id_tipo_servicio;
	}


}
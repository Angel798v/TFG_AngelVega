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
@Table(name = "Reparacion")
public class Reparacion implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id_reparacion;

	@ManyToOne
	@JoinColumn(name="id_mecanico")
	private Mecanico mecanico;

	public void setMecanico(Mecanico mecanico){
		if(this.mecanico != null){
			this.mecanico.getReparacions().remove(this);
		}
		this.mecanico = mecanico;
		if(mecanico != null && !mecanico.getReparacions().contains(this)){
			mecanico.getReparacions().add(this);
		}
	}


	@ManyToOne
	@JoinColumn(name="id_cliente")
	private Cliente cliente;

	@ManyToMany
	@JoinTable(name = "Reparacion_Tipo_servicio",
		joinColumns = @JoinColumn(name="id_reparacion"),
		inverseJoinColumns = @JoinColumn(name="id_tipo_servicio"))
	private Set<Tipo_servicio> tipo_servicios;

	public void addTipo_servicio(Tipo_servicio tipo_servicio){
		if(tipo_servicios == null){
			tipo_servicios = new HashSet<Tipo_servicio>();
		}

		if(tipo_servicio != null && !tipo_servicios.contains(tipo_servicio)){
			if(!tipo_servicio.getReparacions().contains(this)){
				tipo_servicio.getReparacions().add(this);
			}
			tipo_servicios.add(tipo_servicio);
		}
	}

	@ManyToOne
	@JoinColumn(name = "id_user")
	private User user;


	@Override
	public String toString(){

		return "" + id_reparacion;
	}


}
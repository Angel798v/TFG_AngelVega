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
@Table(name = "Cliente")
public class Cliente implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id_cliente;

	@ManyToOne
	@JoinColumn(name = "id_user")
	private User user;


	@Override
	public String toString(){

		return "" + id_cliente;
	}


}
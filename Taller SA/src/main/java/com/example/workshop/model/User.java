package com.example.workshop.model;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user")
public class User implements Serializable {

	public User(String username, String password, String email, String direccion, String apellidos, Role role) {
		this.username = username;
		this.password = password;
		this.email = email;
		this.direccion = direccion;
		this.apellidos = apellidos;
		this.role = role;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(name = "username", length = 30, nullable = false, unique = true)
	private String username;

	@Column(name = "password", length = 250)
	private String password;

	@Column(name = "email", length = 50, nullable = false, unique = true)
	private String email;

	@Column(name = "direccion", length = 20, nullable = false, unique = false)
	private String direccion;


	@Column(name = "apellidos", length = 15, nullable = true, unique = false)
	private String apellidos;


	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "idrole")
	private Role role;


	@Override
	public String toString(){

		return id + " - " + username + " - " + role;
	}

}
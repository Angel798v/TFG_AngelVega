package com.example.workshop.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "role")
public class Role implements Serializable{

	@Id
	private Integer id;

	@Column(name = "role_name",nullable = false)
	private String roleName;

	@OneToMany(mappedBy = "role")
	private Set<User> usuarios;

	public Role(int id, String roleName) {
		this.id = id;
		this.roleName = roleName;
	}


	@Override
	public String toString(){

		return roleName;
	}

}
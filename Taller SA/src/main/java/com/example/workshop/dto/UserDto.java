package com.example.workshop.dto;

import com.example.workshop.model.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
	private long id;

	private String username;

	private String email;

	private String direccion;

	private String apellidos;

	private Role role;

}
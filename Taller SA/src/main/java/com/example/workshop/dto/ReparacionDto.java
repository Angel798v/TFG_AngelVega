package com.example.workshop.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReparacionDto {

	private long id_reparacion;

	private MecanicoDto mecanicoDto;

	private ClienteDto clienteDto;

	private String[] tipo_servicioDto;

}
package com.proyecto.GestionVeterinaria.dto.mascota;

import com.proyecto.GestionVeterinaria.persistence.enumerates.Sexo;

import java.time.LocalDate;

public record MascotaResponseDto(
    Long id,
    String nombre,
    String especie,
    String raza,
    LocalDate fechaNacimiento,
    double pesoKg,
    Long clienteId,
    String clienteNombres,
    Sexo sexo,
    boolean esterilizado) {
}

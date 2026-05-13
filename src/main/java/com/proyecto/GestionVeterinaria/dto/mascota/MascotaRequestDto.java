package com.proyecto.GestionVeterinaria.dto.mascota;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record MascotaRequestDto(
    @NotNull Long clienteId,
    @NotBlank String nombre,
    @NotBlank String especie,
    String raza,
    LocalDate fechaNacimiento,
    @Positive double pesoKg) {
}

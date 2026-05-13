package com.proyecto.GestionVeterinaria.dto.servicio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ServicioRequestDto(
    @NotBlank String nombre,
    String descripcion,
    @Positive int duracionMin,
    @Positive double precio,
    boolean activo) {
}

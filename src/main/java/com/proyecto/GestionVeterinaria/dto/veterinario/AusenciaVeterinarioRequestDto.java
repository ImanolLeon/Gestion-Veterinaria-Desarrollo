package com.proyecto.GestionVeterinaria.dto.veterinario;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AusenciaVeterinarioRequestDto(
    @NotNull LocalDate fechaInicio,
    @NotNull LocalDate fechaFin,
    String motivo) {
}

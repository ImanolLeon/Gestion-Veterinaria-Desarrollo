package com.proyecto.GestionVeterinaria.dto.veterinario;

import java.time.LocalDate;

public record AusenciaVeterinarioResponseDto(
    Long id,
    Long veterinarioId,
    LocalDate fechaInicio,
    LocalDate fechaFin,
    String motivo) {
}

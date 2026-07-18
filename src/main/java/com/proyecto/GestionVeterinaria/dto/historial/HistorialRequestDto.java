package com.proyecto.GestionVeterinaria.dto.historial;

import jakarta.validation.constraints.NotNull;

public record HistorialRequestDto(
    @NotNull Long citaId,
    @NotNull Long mascotaId,
    String diagnostico,
    String tratamiento,
    String observaciones,
    Double pesoKg) {
}

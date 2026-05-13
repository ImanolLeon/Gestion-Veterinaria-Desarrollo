package com.proyecto.GestionVeterinaria.dto.historial;

import java.time.LocalDate;

public record HistorialResponseDto(
    Long id,
    Long mascotaId,
    String mascotaNombre,
    Long citaId,
    String diagnostico,
    String tratamiento,
    String observaciones,
    LocalDate fecha,
    String veterinarioNombres,
    String servicioNombre) {
}

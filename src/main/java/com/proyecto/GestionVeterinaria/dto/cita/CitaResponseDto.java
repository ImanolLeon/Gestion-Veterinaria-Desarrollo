package com.proyecto.GestionVeterinaria.dto.cita;

import com.proyecto.GestionVeterinaria.persistence.enumerates.Estado;

import java.time.LocalDateTime;

public record CitaResponseDto(
    Long id,
    LocalDateTime fechaHora,
    Estado estado,
    String notas,
    Long mascotaId,
    String mascotaNombre,
    Long veterinarioId,
    String veterinarioNombres,
    Long servicioId,
    String servicioNombre,
    double servicioPrecio) {
}

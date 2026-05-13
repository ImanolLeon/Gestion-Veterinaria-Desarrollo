package com.proyecto.GestionVeterinaria.dto.veterinario;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record DisponibilidadResponseDto(
    Long veterinarioId,
    LocalDate fecha,
    List<LocalTime> slotsDisponibles) {
}

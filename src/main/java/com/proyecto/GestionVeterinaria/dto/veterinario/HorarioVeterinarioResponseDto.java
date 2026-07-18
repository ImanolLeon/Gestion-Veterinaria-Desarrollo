package com.proyecto.GestionVeterinaria.dto.veterinario;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record HorarioVeterinarioResponseDto(
    Long id,
    Long veterinarioId,
    DayOfWeek diaSemana,
    LocalTime horaInicio,
    LocalTime horaFin) {
}

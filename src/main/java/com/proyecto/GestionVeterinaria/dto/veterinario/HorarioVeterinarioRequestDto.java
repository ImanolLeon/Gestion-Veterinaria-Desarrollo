package com.proyecto.GestionVeterinaria.dto.veterinario;

import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record HorarioVeterinarioRequestDto(
    @NotNull DayOfWeek diaSemana,
    @NotNull LocalTime horaInicio,
    @NotNull LocalTime horaFin) {
}

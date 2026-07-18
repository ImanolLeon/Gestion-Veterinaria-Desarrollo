package com.proyecto.GestionVeterinaria.dto.cita;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ReprogramarCitaDto(
    @NotNull @Future LocalDateTime fechaHora) {
}

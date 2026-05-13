package com.proyecto.GestionVeterinaria.dto.cita;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CitaRequestDto(
    @NotNull Long mascotaId,
    @NotNull Long veterinarioId,
    @NotNull Long servicioId,
    @NotNull @Future LocalDateTime fechaHora,
    String notas) {
}

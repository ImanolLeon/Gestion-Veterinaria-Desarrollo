package com.proyecto.GestionVeterinaria.dto.cita;

import com.proyecto.GestionVeterinaria.persistence.enumerates.Estado;
import jakarta.validation.constraints.NotNull;

public record EstadoUpdateDto(
    @NotNull Estado estado) {
}

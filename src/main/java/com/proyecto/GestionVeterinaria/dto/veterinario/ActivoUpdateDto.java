package com.proyecto.GestionVeterinaria.dto.veterinario;

import jakarta.validation.constraints.NotNull;

public record ActivoUpdateDto(
    @NotNull Boolean activo) {
}

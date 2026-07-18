package com.proyecto.GestionVeterinaria.dto.veterinario;

import jakarta.validation.constraints.NotBlank;

public record VeterinarioUpdateDto(
    @NotBlank String nombres,
    @NotBlank String apellidos,
    @NotBlank String especialidad,
    @NotBlank String colegiatura) {
}

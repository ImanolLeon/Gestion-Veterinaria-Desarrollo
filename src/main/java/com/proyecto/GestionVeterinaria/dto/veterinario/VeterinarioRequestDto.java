package com.proyecto.GestionVeterinaria.dto.veterinario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VeterinarioRequestDto(
    @NotNull Long usuarioId,
    @NotBlank String nombres,
    @NotBlank String apellidos,
    @NotBlank String especialidad,
    @NotBlank String colegiatura) {
}

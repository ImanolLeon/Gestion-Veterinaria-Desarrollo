package com.proyecto.GestionVeterinaria.dto.veterinario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VeterinarioRequestDto(
    @NotBlank String username,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 6) String password,
    @NotBlank String nombres,
    @NotBlank String apellidos,
    @NotBlank String especialidad,
    @NotBlank String colegiatura) {
}

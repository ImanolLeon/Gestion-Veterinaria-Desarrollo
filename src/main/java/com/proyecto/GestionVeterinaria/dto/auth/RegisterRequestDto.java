package com.proyecto.GestionVeterinaria.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
    @NotBlank String username,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 6) String password,
    @NotBlank String nombres,
    @NotBlank String apellidos,
    @NotBlank @Size(min = 8, max = 8) String dni,
    String telefono,
    String direccion) {
}

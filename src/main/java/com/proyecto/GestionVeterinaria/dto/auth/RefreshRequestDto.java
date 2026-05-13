package com.proyecto.GestionVeterinaria.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequestDto(
    @NotBlank String refreshToken) {
}

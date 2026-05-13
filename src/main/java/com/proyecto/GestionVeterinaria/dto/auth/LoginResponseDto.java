package com.proyecto.GestionVeterinaria.dto.auth;

import java.util.List;

public record LoginResponseDto(
    String token,
    String refreshToken,
    String username,
    List<String> roles) {
}

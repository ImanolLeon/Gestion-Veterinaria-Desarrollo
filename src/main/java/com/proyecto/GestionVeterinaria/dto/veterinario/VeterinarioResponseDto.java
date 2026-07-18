package com.proyecto.GestionVeterinaria.dto.veterinario;

public record VeterinarioResponseDto(
    Long id,
    String nombres,
    String apellidos,
    String especialidad,
    String colegiatura,
    String email,
    String username,
    boolean activo) {
}

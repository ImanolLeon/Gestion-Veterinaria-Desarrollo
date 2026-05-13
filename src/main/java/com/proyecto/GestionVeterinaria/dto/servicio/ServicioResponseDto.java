package com.proyecto.GestionVeterinaria.dto.servicio;

public record ServicioResponseDto(
    Long id,
    String nombre,
    String descripcion,
    int duracionMin,
    double precio,
    boolean activo) {
}

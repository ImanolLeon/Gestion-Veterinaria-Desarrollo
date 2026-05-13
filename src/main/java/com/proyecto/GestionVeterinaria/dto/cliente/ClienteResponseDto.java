package com.proyecto.GestionVeterinaria.dto.cliente;

public record ClienteResponseDto(
    Long id,
    String nombres,
    String apellidos,
    String dni,
    String telefono,
    String direccion,
    String email,
    String username) {
}

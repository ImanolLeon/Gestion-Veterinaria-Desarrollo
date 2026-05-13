package com.proyecto.GestionVeterinaria.dto.cliente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteUpdateDto(
    @NotBlank String nombres,
    @NotBlank String apellidos,
    @NotBlank @Size(min = 8, max = 15) String dni,
    String telefono,
    String direccion) {
}

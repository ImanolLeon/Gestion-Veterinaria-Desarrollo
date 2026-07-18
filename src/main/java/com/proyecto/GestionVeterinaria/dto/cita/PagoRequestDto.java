package com.proyecto.GestionVeterinaria.dto.cita;

import com.proyecto.GestionVeterinaria.persistence.enumerates.MetodoPago;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PagoRequestDto(
    @NotNull MetodoPago metodo,
    @NotNull @Positive Double monto) {
}

package com.proyecto.GestionVeterinaria.dto.historial;

public record HistorialUpdateDto(
    String diagnostico,
    String tratamiento,
    String observaciones) {
}

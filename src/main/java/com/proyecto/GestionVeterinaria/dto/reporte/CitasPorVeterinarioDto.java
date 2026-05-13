package com.proyecto.GestionVeterinaria.dto.reporte;

public record CitasPorVeterinarioDto(
    Long veterinarioId,
    String veterinarioNombres,
    long totalCitas,
    long citasCompletadas,
    long citasCanceladas) {
}

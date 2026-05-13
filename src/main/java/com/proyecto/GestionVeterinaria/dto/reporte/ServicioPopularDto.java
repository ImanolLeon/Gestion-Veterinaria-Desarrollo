package com.proyecto.GestionVeterinaria.dto.reporte;

public record ServicioPopularDto(
    Long servicioId,
    String servicioNombre,
    long totalCitas) {
}

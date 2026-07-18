package com.proyecto.GestionVeterinaria.dto.veterinario;

import com.proyecto.GestionVeterinaria.dto.cita.CitaResponseDto;

import java.util.List;

public record VeterinarioActivoResponseDto(
    VeterinarioResponseDto veterinario,
    List<CitaResponseDto> citasAfectadas) {
}

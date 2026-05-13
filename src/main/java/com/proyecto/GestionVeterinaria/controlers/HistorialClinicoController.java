package com.proyecto.GestionVeterinaria.controlers;

import com.proyecto.GestionVeterinaria.dto.historial.HistorialRequestDto;
import com.proyecto.GestionVeterinaria.dto.historial.HistorialResponseDto;
import com.proyecto.GestionVeterinaria.dto.historial.HistorialUpdateDto;
import com.proyecto.GestionVeterinaria.service.HistorialClinicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class HistorialClinicoController {

  private final HistorialClinicoService historialClinicoService;

  @GetMapping("/api/mascotas/{mascotaId}/historial")
  public List<HistorialResponseDto> findByMascota(@PathVariable Long mascotaId) {
    return historialClinicoService.findByMascota(mascotaId);
  }

  @PostMapping("/api/historial")
  @ResponseStatus(HttpStatus.CREATED)
  public HistorialResponseDto create(@Valid @RequestBody HistorialRequestDto dto) {
    return historialClinicoService.create(dto);
  }

  @PutMapping("/api/historial/{id}")
  public HistorialResponseDto update(@PathVariable Long id,
      @RequestBody HistorialUpdateDto dto) {
    return historialClinicoService.update(id, dto);
  }
}

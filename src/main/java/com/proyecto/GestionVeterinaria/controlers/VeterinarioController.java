package com.proyecto.GestionVeterinaria.controlers;

import com.proyecto.GestionVeterinaria.dto.veterinario.DisponibilidadResponseDto;
import com.proyecto.GestionVeterinaria.dto.veterinario.VeterinarioRequestDto;
import com.proyecto.GestionVeterinaria.dto.veterinario.VeterinarioResponseDto;
import com.proyecto.GestionVeterinaria.service.VeterinarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/veterinarios")
@RequiredArgsConstructor
public class VeterinarioController {

  private final VeterinarioService veterinarioService;

  @GetMapping
  public List<VeterinarioResponseDto> findAll() {
    return veterinarioService.findAll();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('ADMIN')")
  public VeterinarioResponseDto create(@Valid @RequestBody VeterinarioRequestDto dto) {
    return veterinarioService.create(dto);
  }

  @GetMapping("/{id}/disponibilidad")
  public DisponibilidadResponseDto getDisponibilidad(
      @PathVariable Long id,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
    return veterinarioService.getDisponibilidad(id, fecha);
  }
}

package com.proyecto.GestionVeterinaria.controlers;

import com.proyecto.GestionVeterinaria.dto.veterinario.ActivoUpdateDto;
import com.proyecto.GestionVeterinaria.dto.veterinario.DisponibilidadResponseDto;
import com.proyecto.GestionVeterinaria.dto.veterinario.VeterinarioActivoResponseDto;
import com.proyecto.GestionVeterinaria.dto.veterinario.VeterinarioRequestDto;
import com.proyecto.GestionVeterinaria.dto.veterinario.VeterinarioResponseDto;
import com.proyecto.GestionVeterinaria.dto.veterinario.VeterinarioUpdateDto;
import com.proyecto.GestionVeterinaria.security.AuthUtils;
import com.proyecto.GestionVeterinaria.service.VeterinarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/veterinarios")
@RequiredArgsConstructor
public class VeterinarioController {

  private final VeterinarioService veterinarioService;

  @GetMapping
  public List<VeterinarioResponseDto> findAll(Authentication authentication) {
    boolean soloActivos = !AuthUtils.hasRole(authentication, "ADMIN")
        && !AuthUtils.hasRole(authentication, "VETERINARIO");
    return veterinarioService.findAll(soloActivos);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('ADMIN')")
  public VeterinarioResponseDto create(@Valid @RequestBody VeterinarioRequestDto dto) {
    return veterinarioService.create(dto);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public VeterinarioResponseDto update(@PathVariable Long id,
      @Valid @RequestBody VeterinarioUpdateDto dto) {
    return veterinarioService.update(id, dto);
  }

  @PatchMapping("/{id}/activo")
  @PreAuthorize("hasRole('ADMIN')")
  public VeterinarioActivoResponseDto setActivo(@PathVariable Long id,
      @Valid @RequestBody ActivoUpdateDto dto) {
    return veterinarioService.setActivo(id, dto.activo());
  }

  @GetMapping("/{id}/disponibilidad")
  public DisponibilidadResponseDto getDisponibilidad(
      @PathVariable Long id,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
      @RequestParam(required = false) Long servicioId) {
    return veterinarioService.getDisponibilidad(id, fecha, servicioId);
  }
}

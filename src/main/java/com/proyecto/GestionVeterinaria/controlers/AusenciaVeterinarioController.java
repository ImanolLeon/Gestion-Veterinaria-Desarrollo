package com.proyecto.GestionVeterinaria.controlers;

import com.proyecto.GestionVeterinaria.dto.veterinario.AusenciaVeterinarioRequestDto;
import com.proyecto.GestionVeterinaria.dto.veterinario.AusenciaVeterinarioResponseDto;
import com.proyecto.GestionVeterinaria.service.AusenciaVeterinarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AusenciaVeterinarioController {

  private final AusenciaVeterinarioService ausenciaVeterinarioService;

  @GetMapping("/api/veterinarios/{veterinarioId}/ausencias")
  public List<AusenciaVeterinarioResponseDto> findByVeterinario(@PathVariable Long veterinarioId) {
    return ausenciaVeterinarioService.findByVeterinario(veterinarioId);
  }

  @PostMapping("/api/veterinarios/{veterinarioId}/ausencias")
  @ResponseStatus(HttpStatus.CREATED)
  public AusenciaVeterinarioResponseDto create(@PathVariable Long veterinarioId,
      @Valid @RequestBody AusenciaVeterinarioRequestDto dto) {
    return ausenciaVeterinarioService.create(veterinarioId, dto);
  }

  @PutMapping("/api/ausencias/{id}")
  public AusenciaVeterinarioResponseDto update(@PathVariable Long id,
      @Valid @RequestBody AusenciaVeterinarioRequestDto dto) {
    return ausenciaVeterinarioService.update(id, dto);
  }

  @DeleteMapping("/api/ausencias/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    ausenciaVeterinarioService.delete(id);
  }
}

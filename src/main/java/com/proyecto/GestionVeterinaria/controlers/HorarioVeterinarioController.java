package com.proyecto.GestionVeterinaria.controlers;

import com.proyecto.GestionVeterinaria.dto.veterinario.HorarioVeterinarioRequestDto;
import com.proyecto.GestionVeterinaria.dto.veterinario.HorarioVeterinarioResponseDto;
import com.proyecto.GestionVeterinaria.service.HorarioVeterinarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class HorarioVeterinarioController {

  private final HorarioVeterinarioService horarioVeterinarioService;

  @GetMapping("/api/veterinarios/{veterinarioId}/horarios")
  public List<HorarioVeterinarioResponseDto> findByVeterinario(@PathVariable Long veterinarioId) {
    return horarioVeterinarioService.findByVeterinario(veterinarioId);
  }

  @PostMapping("/api/veterinarios/{veterinarioId}/horarios")
  @ResponseStatus(HttpStatus.CREATED)
  public HorarioVeterinarioResponseDto create(@PathVariable Long veterinarioId,
      @Valid @RequestBody HorarioVeterinarioRequestDto dto) {
    return horarioVeterinarioService.create(veterinarioId, dto);
  }

  @PutMapping("/api/horarios/{id}")
  public HorarioVeterinarioResponseDto update(@PathVariable Long id,
      @Valid @RequestBody HorarioVeterinarioRequestDto dto) {
    return horarioVeterinarioService.update(id, dto);
  }

  @DeleteMapping("/api/horarios/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    horarioVeterinarioService.delete(id);
  }
}

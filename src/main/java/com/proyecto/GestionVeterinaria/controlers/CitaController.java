package com.proyecto.GestionVeterinaria.controlers;

import com.proyecto.GestionVeterinaria.dto.cita.CitaRequestDto;
import com.proyecto.GestionVeterinaria.dto.cita.CitaResponseDto;
import com.proyecto.GestionVeterinaria.dto.cita.EstadoUpdateDto;
import com.proyecto.GestionVeterinaria.service.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaController {

  private final CitaService citaService;

  @GetMapping
  public List<CitaResponseDto> findAll() {
    return citaService.findAll();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CitaResponseDto agendar(@Valid @RequestBody CitaRequestDto dto) {
    return citaService.agendar(dto);
  }

  @PatchMapping("/{id}/estado")
  public CitaResponseDto cambiarEstado(@PathVariable Long id,
      @Valid @RequestBody EstadoUpdateDto dto) {
    return citaService.cambiarEstado(id, dto);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void cancelar(@PathVariable Long id) {
    citaService.cancelar(id);
  }
}

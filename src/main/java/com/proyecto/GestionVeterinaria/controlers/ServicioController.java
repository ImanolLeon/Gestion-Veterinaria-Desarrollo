package com.proyecto.GestionVeterinaria.controlers;

import com.proyecto.GestionVeterinaria.dto.servicio.ServicioRequestDto;
import com.proyecto.GestionVeterinaria.dto.servicio.ServicioResponseDto;
import com.proyecto.GestionVeterinaria.service.ServicioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicios")
@RequiredArgsConstructor
public class ServicioController {

  private final ServicioService servicioService;

  @GetMapping
  public List<ServicioResponseDto> findActivos() {
    return servicioService.findActivos();
  }

  @GetMapping("/all")
  @PreAuthorize("hasRole('ADMIN')")
  public List<ServicioResponseDto> findAll() {
    return servicioService.findAll();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('ADMIN')")
  public ServicioResponseDto create(@Valid @RequestBody ServicioRequestDto dto) {
    return servicioService.create(dto);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ServicioResponseDto update(@PathVariable Long id,
      @Valid @RequestBody ServicioRequestDto dto) {
    return servicioService.update(id, dto);
  }
}

package com.proyecto.GestionVeterinaria.controlers;

import com.proyecto.GestionVeterinaria.dto.mascota.MascotaRequestDto;
import com.proyecto.GestionVeterinaria.dto.mascota.MascotaResponseDto;
import com.proyecto.GestionVeterinaria.service.MascotaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MascotaController {

  private final MascotaService mascotaService;

  @GetMapping("/api/clientes/{clienteId}/mascotas")
  public List<MascotaResponseDto> findByCliente(@PathVariable Long clienteId) {
    return mascotaService.findByClienteId(clienteId);
  }

  @PostMapping("/api/mascotas")
  @ResponseStatus(HttpStatus.CREATED)
  public MascotaResponseDto create(@Valid @RequestBody MascotaRequestDto dto) {
    return mascotaService.create(dto);
  }

  @PutMapping("/api/mascotas/{id}")
  public MascotaResponseDto update(@PathVariable Long id,
      @Valid @RequestBody MascotaRequestDto dto) {
    return mascotaService.update(id, dto);
  }

  @DeleteMapping("/api/mascotas/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    mascotaService.delete(id);
  }
}

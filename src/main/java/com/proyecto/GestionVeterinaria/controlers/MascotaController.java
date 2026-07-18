package com.proyecto.GestionVeterinaria.controlers;

import com.proyecto.GestionVeterinaria.dto.mascota.MascotaRequestDto;
import com.proyecto.GestionVeterinaria.dto.mascota.MascotaResponseDto;
import com.proyecto.GestionVeterinaria.security.AuthUtils;
import com.proyecto.GestionVeterinaria.service.MascotaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MascotaController {

  private final MascotaService mascotaService;

  @GetMapping("/api/clientes/{clienteId}/mascotas")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VETERINARIO') "
      + "or @clienteSecurityService.isOwner(#clienteId, authentication.name)")
  public List<MascotaResponseDto> findByCliente(@PathVariable Long clienteId) {
    return mascotaService.findByClienteId(clienteId);
  }

  @PostMapping("/api/mascotas")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENTE')")
  public MascotaResponseDto create(@Valid @RequestBody MascotaRequestDto dto,
      Authentication authentication) {
    boolean isAdmin = AuthUtils.hasRole(authentication, "ADMIN");
    return mascotaService.create(dto, authentication.getName(), isAdmin);
  }

  @PutMapping("/api/mascotas/{id}")
  @PreAuthorize("hasRole('ADMIN') "
      + "or (hasRole('CLIENTE') and @mascotaSecurityService.isOwner(#id, authentication.name))")
  public MascotaResponseDto update(@PathVariable Long id,
      @Valid @RequestBody MascotaRequestDto dto) {
    return mascotaService.update(id, dto);
  }

  @DeleteMapping("/api/mascotas/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ADMIN') "
      + "or (hasRole('CLIENTE') and @mascotaSecurityService.isOwner(#id, authentication.name))")
  public void delete(@PathVariable Long id) {
    mascotaService.delete(id);
  }
}

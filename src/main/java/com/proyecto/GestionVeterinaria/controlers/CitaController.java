package com.proyecto.GestionVeterinaria.controlers;

import com.proyecto.GestionVeterinaria.dto.cita.CitaRequestDto;
import com.proyecto.GestionVeterinaria.dto.cita.CitaResponseDto;
import com.proyecto.GestionVeterinaria.dto.cita.EstadoUpdateDto;
import com.proyecto.GestionVeterinaria.dto.cita.PagoRequestDto;
import com.proyecto.GestionVeterinaria.dto.cita.ReprogramarCitaDto;
import com.proyecto.GestionVeterinaria.persistence.enumerates.CanceladoPor;
import com.proyecto.GestionVeterinaria.security.AuthUtils;
import com.proyecto.GestionVeterinaria.service.CitaSecurityService;
import com.proyecto.GestionVeterinaria.service.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaController {

  private final CitaService citaService;
  private final CitaSecurityService citaSecurityService;

  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('VETERINARIO')")
  public List<CitaResponseDto> findAll(Authentication authentication) {
    if (AuthUtils.hasRole(authentication, "ADMIN")) {
      return citaService.findAll();
    }
    Long veterinarioId = citaSecurityService.resolveVeterinarioId(authentication.getName())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Perfil de veterinario no encontrado"));
    return citaService.findByVeterinarioId(veterinarioId);
  }

  @GetMapping("/mis-citas")
  @PreAuthorize("hasRole('CLIENTE')")
  public List<CitaResponseDto> misCitas(Authentication authentication) {
    return citaService.findMisCitas(authentication.getName());
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('ADMIN') or hasRole('VETERINARIO') "
      + "or (hasRole('CLIENTE') and @mascotaSecurityService.isOwner(#dto.mascotaId(), authentication.name))")
  public CitaResponseDto agendar(@Valid @RequestBody CitaRequestDto dto) {
    return citaService.agendar(dto);
  }

  @PatchMapping("/{id}/estado")
  @PreAuthorize("hasRole('ADMIN') "
      + "or (hasRole('VETERINARIO') and @citaSecurityService.isAssignedVet(#id, authentication.name))")
  public CitaResponseDto cambiarEstado(@PathVariable Long id,
      @Valid @RequestBody EstadoUpdateDto dto) {
    return citaService.cambiarEstado(id, dto);
  }

  @PatchMapping("/{id}/pago")
  @PreAuthorize("hasRole('ADMIN') "
      + "or (hasRole('VETERINARIO') and @citaSecurityService.isAssignedVet(#id, authentication.name))")
  public CitaResponseDto registrarPago(@PathVariable Long id,
      @Valid @RequestBody PagoRequestDto dto) {
    return citaService.registrarPago(id, dto);
  }

  @PatchMapping("/{id}/reprogramar")
  @PreAuthorize("hasRole('ADMIN') "
      + "or (hasRole('CLIENTE') and @citaSecurityService.isOwnerOfCita(#id, authentication.name))")
  public CitaResponseDto reprogramar(@PathVariable Long id,
      @Valid @RequestBody ReprogramarCitaDto dto) {
    return citaService.reprogramar(id, dto);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ADMIN') "
      + "or (hasRole('VETERINARIO') and @citaSecurityService.isAssignedVet(#id, authentication.name)) "
      + "or (hasRole('CLIENTE') and @citaSecurityService.isOwnerOfCita(#id, authentication.name))")
  public void cancelar(@PathVariable Long id, Authentication authentication) {
    CanceladoPor canceladoPor;
    if (AuthUtils.hasRole(authentication, "ADMIN")) {
      canceladoPor = CanceladoPor.ADMIN;
    } else if (AuthUtils.hasRole(authentication, "VETERINARIO")) {
      canceladoPor = CanceladoPor.VETERINARIO;
    } else {
      canceladoPor = CanceladoPor.CLIENTE;
    }
    citaService.cancelar(id, canceladoPor);
  }
}

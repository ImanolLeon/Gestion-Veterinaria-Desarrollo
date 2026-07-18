package com.proyecto.GestionVeterinaria.controlers;

import com.proyecto.GestionVeterinaria.dto.reporte.CitasPorVeterinarioDto;
import com.proyecto.GestionVeterinaria.dto.reporte.ServicioPopularDto;
import com.proyecto.GestionVeterinaria.security.AuthUtils;
import com.proyecto.GestionVeterinaria.service.CitaSecurityService;
import com.proyecto.GestionVeterinaria.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('VETERINARIO')")
public class ReporteController {

  private final ReporteService reporteService;
  private final CitaSecurityService citaSecurityService;

  @GetMapping("/citas-por-veterinario")
  public List<CitasPorVeterinarioDto> citasPorVeterinario(
      @RequestParam(required = false) Long veterinarioId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
      Authentication authentication) {
    if (AuthUtils.hasRole(authentication, "ADMIN")) {
      return reporteService.citasPorVeterinario(veterinarioId, desde, hasta);
    }

    Optional<Long> propio = veterinarioIdPropio(authentication, veterinarioId);
    if (propio.isEmpty()) {
      return List.of();
    }
    return reporteService.citasPorVeterinario(propio.get(), desde, hasta);
  }

  @GetMapping("/servicios-populares")
  public List<ServicioPopularDto> serviciosPopulares(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
      Authentication authentication) {
    if (AuthUtils.hasRole(authentication, "ADMIN")) {
      return reporteService.serviciosPopulares(null, desde, hasta);
    }

    Optional<Long> propio = veterinarioIdPropio(authentication, null);
    if (propio.isEmpty()) {
      return List.of();
    }
    return reporteService.serviciosPopulares(propio.get(), desde, hasta);
  }

  /**
   * Resuelve el veterinario del usuario autenticado a partir del username del JWT,
   * nunca del query param: un veterinario solo puede ver sus propios datos. Pedir
   * el id de otro veterinario es 403; no tener perfil de veterinario asociado
   * devuelve vacío para que el endpoint responda 200 con una lista vacía.
   */
  private Optional<Long> veterinarioIdPropio(Authentication authentication, Long veterinarioIdSolicitado) {
    Optional<Long> propio = citaSecurityService.resolveVeterinarioId(authentication.getName());
    if (veterinarioIdSolicitado != null && propio.filter(veterinarioIdSolicitado::equals).isEmpty()) {
      throw new AccessDeniedException("No puede consultar reportes de otro veterinario");
    }
    return propio;
  }
}

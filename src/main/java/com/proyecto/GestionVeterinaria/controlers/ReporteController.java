package com.proyecto.GestionVeterinaria.controlers;

import com.proyecto.GestionVeterinaria.dto.reporte.CitasPorVeterinarioDto;
import com.proyecto.GestionVeterinaria.dto.reporte.ServicioPopularDto;
import com.proyecto.GestionVeterinaria.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('VETERINARIO')")
public class ReporteController {

  private final ReporteService reporteService;

  @GetMapping("/citas-por-veterinario")
  public List<CitasPorVeterinarioDto> citasPorVeterinario(
      @RequestParam(required = false) Long veterinarioId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
    return reporteService.citasPorVeterinario(veterinarioId, desde, hasta);
  }

  @GetMapping("/servicios-populares")
  public List<ServicioPopularDto> serviciosPopulares() {
    return reporteService.serviciosPopulares();
  }
}

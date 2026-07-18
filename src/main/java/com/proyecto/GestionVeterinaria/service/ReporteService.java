package com.proyecto.GestionVeterinaria.service;

import com.proyecto.GestionVeterinaria.dto.reporte.CitasPorVeterinarioDto;
import com.proyecto.GestionVeterinaria.dto.reporte.ServicioPopularDto;
import com.proyecto.GestionVeterinaria.persistence.entity.Cita;
import com.proyecto.GestionVeterinaria.persistence.entity.Veterinario;
import com.proyecto.GestionVeterinaria.persistence.enumerates.Estado;
import com.proyecto.GestionVeterinaria.repository.CitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteService {

  private final CitaRepository citaRepository;

  public List<CitasPorVeterinarioDto> citasPorVeterinario(Long veterinarioId,
      LocalDateTime desde,
      LocalDateTime hasta) {
    List<Cita> citas = buscarCitas(veterinarioId, desde, hasta);

    Map<Long, List<Cita>> byVet = citas.stream()
        .filter(c -> c.getVeterinario() != null)
        .collect(Collectors.groupingBy(c -> c.getVeterinario().getId()));

    return byVet.entrySet().stream().map(entry -> {
      Long vetId = entry.getKey();
      List<Cita> citasVet = entry.getValue();
      Veterinario vet = citasVet.get(0).getVeterinario();
      long completadas = citasVet.stream().filter(c -> c.getEstado() == Estado.COMPLETADA).count();
      long canceladas = citasVet.stream().filter(c -> c.getEstado() == Estado.CANCELADA).count();
      return new CitasPorVeterinarioDto(
          vetId,
          vet.getNombres() + " " + vet.getApellidos(),
          citasVet.size(),
          completadas,
          canceladas);
    }).toList();
  }

  public List<ServicioPopularDto> serviciosPopulares(Long veterinarioId,
      LocalDateTime desde,
      LocalDateTime hasta) {
    List<Cita> citas = buscarCitas(veterinarioId, desde, hasta);

    Map<Long, List<Cita>> byServicio = citas.stream()
        .filter(c -> c.getServicio() != null && c.getEstado() != Estado.CANCELADA)
        .collect(Collectors.groupingBy(c -> c.getServicio().getId()));

    return byServicio.entrySet().stream()
        .map(entry -> {
          List<Cita> citasServicio = entry.getValue();
          String nombre = citasServicio.get(0).getServicio().getNombre();
          return new ServicioPopularDto(entry.getKey(), nombre, citasServicio.size());
        })
        .sorted((a, b) -> Long.compare(b.totalCitas(), a.totalCitas()))
        .toList();
  }

  /**
   * El rango es inclusivo en ambos extremos. Un veterinarioId nulo abarca a todos
   * los veterinarios, y un rango incompleto (desde o hasta nulos) abarca todas las
   * fechas.
   */
  private List<Cita> buscarCitas(Long veterinarioId, LocalDateTime desde, LocalDateTime hasta) {
    boolean conRango = desde != null && hasta != null;
    if (veterinarioId != null) {
      return conRango
          ? citaRepository.findByVeterinarioIdAndFechaHoraBetween(veterinarioId, desde, hasta)
          : citaRepository.findByVeterinarioId(veterinarioId);
    }
    return conRango
        ? citaRepository.findByFechaHoraBetween(desde, hasta)
        : citaRepository.findAll();
  }
}

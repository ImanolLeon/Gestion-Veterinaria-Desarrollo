package com.proyecto.GestionVeterinaria.service;

import com.proyecto.GestionVeterinaria.dto.historial.HistorialRequestDto;
import com.proyecto.GestionVeterinaria.dto.historial.HistorialResponseDto;
import com.proyecto.GestionVeterinaria.dto.historial.HistorialUpdateDto;
import com.proyecto.GestionVeterinaria.persistence.entity.Cita;
import com.proyecto.GestionVeterinaria.persistence.entity.HistorialClinico;
import com.proyecto.GestionVeterinaria.persistence.entity.Mascota;
import com.proyecto.GestionVeterinaria.persistence.enumerates.Estado;
import com.proyecto.GestionVeterinaria.repository.CitaRepository;
import com.proyecto.GestionVeterinaria.repository.HistorialClinicoRepository;
import com.proyecto.GestionVeterinaria.repository.MascotaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistorialClinicoService {

  private final HistorialClinicoRepository historialClinicoRepository;
  private final CitaRepository citaRepository;
  private final MascotaRepository mascotaRepository;

  @Value("${app.historial.edit-window-hours:48}")
  private long editWindowHours;

  public List<HistorialResponseDto> findByMascota(Long mascotaId) {
    mascotaRepository.findByIdAndActivoTrue(mascotaId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mascota no encontrada"));
    return historialClinicoRepository.findByMascotaIdOrderByFechaDesc(mascotaId)
        .stream().map(this::toDto).toList();
  }

  @Transactional
  public HistorialResponseDto create(HistorialRequestDto dto) {
    Cita cita = citaRepository.findById(dto.citaId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));

    if (cita.getEstado() != Estado.COMPLETADA) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Solo se puede registrar historial para citas COMPLETADAS");
    }

    if (historialClinicoRepository.findByCitaId(dto.citaId()).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,
          "Ya existe un historial para esta cita");
    }

    Mascota mascota = mascotaRepository.findById(dto.mascotaId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mascota no encontrada"));

    HistorialClinico historial = HistorialClinico.builder()
        .cita(cita)
        .mascota(mascota)
        .diagnostico(dto.diagnostico())
        .tratamiento(dto.tratamiento())
        .observaciones(dto.observaciones())
        .peso(dto.pesoKg())
        .fecha(LocalDate.now())
        .creadoEn(LocalDateTime.now())
        .build();

    return toDto(historialClinicoRepository.save(historial));
  }

  @Transactional
  public HistorialResponseDto update(Long id, HistorialUpdateDto dto) {
    HistorialClinico historial = historialClinicoRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Historial no encontrado"));

    if (historial.getCreadoEn() != null
        && Duration.between(historial.getCreadoEn(), LocalDateTime.now()).toHours() > editWindowHours) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "La ventana de edición de " + editWindowHours + "h para este historial ha expirado");
    }

    historial.setDiagnostico(dto.diagnostico());
    historial.setTratamiento(dto.tratamiento());
    historial.setObservaciones(dto.observaciones());
    historial.setPeso(dto.pesoKg());

    return toDto(historialClinicoRepository.save(historial));
  }

  public HistorialResponseDto toDto(HistorialClinico h) {
    String vetNombres = null;
    String servicioNombre = null;
    if (h.getCita() != null) {
      if (h.getCita().getVeterinario() != null) {
        vetNombres = h.getCita().getVeterinario().getNombres()
            + " " + h.getCita().getVeterinario().getApellidos();
      }
      if (h.getCita().getServicio() != null) {
        servicioNombre = h.getCita().getServicio().getNombre();
      }
    }
    return new HistorialResponseDto(
        h.getId(),
        h.getMascota() != null ? h.getMascota().getId() : null,
        h.getMascota() != null ? h.getMascota().getNombre() : null,
        h.getCita() != null ? h.getCita().getId() : null,
        h.getDiagnostico(),
        h.getTratamiento(),
        h.getObservaciones(),
        h.getFecha(),
        vetNombres,
        servicioNombre,
        h.getPeso());
  }
}

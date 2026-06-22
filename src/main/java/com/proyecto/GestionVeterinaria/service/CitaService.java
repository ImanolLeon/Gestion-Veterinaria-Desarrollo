package com.proyecto.GestionVeterinaria.service;

import com.proyecto.GestionVeterinaria.dto.cita.CitaRequestDto;
import com.proyecto.GestionVeterinaria.dto.cita.CitaResponseDto;
import com.proyecto.GestionVeterinaria.dto.cita.EstadoUpdateDto;
import com.proyecto.GestionVeterinaria.persistence.entity.Cita;
import com.proyecto.GestionVeterinaria.persistence.entity.HistorialClinico;
import com.proyecto.GestionVeterinaria.persistence.entity.Mascota;
import com.proyecto.GestionVeterinaria.persistence.entity.Servicio;
import com.proyecto.GestionVeterinaria.persistence.entity.Veterinario;
import com.proyecto.GestionVeterinaria.persistence.enumerates.Estado;
import com.proyecto.GestionVeterinaria.repository.CitaRepository;
import com.proyecto.GestionVeterinaria.repository.HistorialClinicoRepository;
import com.proyecto.GestionVeterinaria.repository.MascotaRepository;
import com.proyecto.GestionVeterinaria.repository.ServicioRepository;
import com.proyecto.GestionVeterinaria.repository.VeterinarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CitaService {

  private final CitaRepository citaRepository;
  private final MascotaRepository mascotaRepository;
  private final VeterinarioRepository veterinarioRepository;
  private final ServicioRepository servicioRepository;
  private final HistorialClinicoRepository historialClinicoRepository;

  public List<CitaResponseDto> findAll() {
    return citaRepository.findAll().stream().map(this::toDto).toList();
  }

  public List<CitaResponseDto> findByMascota(Long mascotaId) {
    return citaRepository.findByMascotaId(mascotaId).stream().map(this::toDto).toList();
  }

  public List<CitaResponseDto> findMisCitas(String username) {
    return citaRepository.findByCliente(username)
        .stream().map(this::toDto).toList();
  }

  @Transactional
  public CitaResponseDto agendar(CitaRequestDto dto) {
    // Business rule: no past dates (also enforced by @Future on DTO)
    if (!dto.fechaHora().isAfter(LocalDateTime.now())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "La fecha y hora de la cita deben ser futuras");
    }

    Mascota mascota = mascotaRepository.findByIdAndActivoTrue(dto.mascotaId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Mascota no encontrada o inactiva"));

    Veterinario veterinario = veterinarioRepository.findById(dto.veterinarioId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Veterinario no encontrado"));

    Servicio servicio = servicioRepository.findById(dto.servicioId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Servicio no encontrado"));

    if (!servicio.isActivo()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El servicio no está activo");
    }

    // Business rule: no overlapping vet schedule
    LocalDateTime fin = dto.fechaHora().plusMinutes(servicio.getDuracionMin());
    List<Cita> conflictos = citaRepository.findConflictingCitas(
        dto.veterinarioId(), dto.fechaHora(), fin);
    if (!conflictos.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,
          "El veterinario ya tiene una cita en ese horario");
    }

    Cita cita = Cita.builder()
        .fechaHora(dto.fechaHora())
        .estado(Estado.PENDIENTE)
        .notas(dto.notas())
        .mascota(mascota)
        .veterinario(veterinario)
        .servicio(servicio)
        .build();

    return toDto(citaRepository.save(cita));
  }

  @Transactional
  public CitaResponseDto cambiarEstado(Long id, EstadoUpdateDto dto) {
    Cita cita = citaRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));

    if (cita.getEstado() == Estado.CANCELADA || cita.getEstado() == Estado.COMPLETADA) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "No se puede cambiar el estado de una cita " + cita.getEstado());
    }

    cita.setEstado(dto.estado());

    // Business rule: auto-create historial when cita is completed
    if (dto.estado() == Estado.COMPLETADA) {
      boolean historialExists = historialClinicoRepository.findByCitaId(id).isPresent();
      if (!historialExists) {
        HistorialClinico historial = HistorialClinico.builder()
            .cita(cita)
            .mascota(cita.getMascota())
            .fecha(LocalDate.now())
            .diagnostico("")
            .tratamiento("")
            .observaciones("")
            .build();
        historialClinicoRepository.save(historial);
      }
    }

    return toDto(citaRepository.save(cita));
  }

  @Transactional
  public void cancelar(Long id) {
    Cita cita = citaRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));

    if (cita.getEstado() == Estado.COMPLETADA) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "No se puede cancelar una cita completada");
    }
    cita.setEstado(Estado.CANCELADA);
    citaRepository.save(cita);
  }

  // Business rule: auto-cancel citas PENDIENTE with no update after 24h
  @Scheduled(fixedRate = 3_600_000)
  @Transactional
  public void autoCancelarCitasPendientes() {
    LocalDateTime limite = LocalDateTime.now().minusHours(24);
    List<Cita> pendientes = citaRepository.findByEstadoAndFechaHoraBefore(Estado.PENDIENTE, limite);
    pendientes.forEach(c -> c.setEstado(Estado.CANCELADA));
    citaRepository.saveAll(pendientes);
  }

  public CitaResponseDto toDto(Cita c) {
    return new CitaResponseDto(
        c.getId(),
        c.getFechaHora(),
        c.getEstado(),
        c.getNotas(),
        c.getMascota() != null ? c.getMascota().getId() : null,
        c.getMascota() != null ? c.getMascota().getNombre() : null,
        c.getVeterinario() != null ? c.getVeterinario().getId() : null,
        c.getVeterinario() != null
            ? c.getVeterinario().getNombres() + " " + c.getVeterinario().getApellidos()
            : null,
        c.getServicio() != null ? c.getServicio().getId() : null,
        c.getServicio() != null ? c.getServicio().getNombre() : null,
        c.getServicio() != null ? c.getServicio().getPrecio() : 0.0);
  }
}

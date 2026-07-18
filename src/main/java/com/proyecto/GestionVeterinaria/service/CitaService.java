package com.proyecto.GestionVeterinaria.service;

import com.proyecto.GestionVeterinaria.dto.cita.CitaRequestDto;
import com.proyecto.GestionVeterinaria.dto.cita.CitaResponseDto;
import com.proyecto.GestionVeterinaria.dto.cita.EstadoUpdateDto;
import com.proyecto.GestionVeterinaria.dto.cita.PagoRequestDto;
import com.proyecto.GestionVeterinaria.dto.cita.ReprogramarCitaDto;
import com.proyecto.GestionVeterinaria.persistence.entity.Cita;
import com.proyecto.GestionVeterinaria.persistence.entity.HistorialClinico;
import com.proyecto.GestionVeterinaria.persistence.entity.Mascota;
import com.proyecto.GestionVeterinaria.persistence.entity.Servicio;
import com.proyecto.GestionVeterinaria.persistence.entity.Veterinario;
import com.proyecto.GestionVeterinaria.persistence.enumerates.CanceladoPor;
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
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CitaService {

  private static final Map<Estado, Set<Estado>> TRANSICIONES_VALIDAS = Map.of(
      Estado.PENDIENTE, Set.of(Estado.CONFIRMADA, Estado.CANCELADA),
      Estado.CONFIRMADA, Set.of(Estado.EN_PROGRESO, Estado.CANCELADA),
      Estado.EN_PROGRESO, Set.of(Estado.COMPLETADA));

  private final CitaRepository citaRepository;
  private final MascotaRepository mascotaRepository;
  private final VeterinarioRepository veterinarioRepository;
  private final ServicioRepository servicioRepository;
  private final HistorialClinicoRepository historialClinicoRepository;
  private final NotificacionService notificacionService;

  public List<CitaResponseDto> findAll() {
    return citaRepository.findAll().stream().map(this::toDto).toList();
  }

  public List<CitaResponseDto> findByMascota(Long mascotaId) {
    return citaRepository.findByMascotaId(mascotaId).stream().map(this::toDto).toList();
  }

  public List<CitaResponseDto> findByVeterinarioId(Long veterinarioId) {
    return citaRepository.findByVeterinarioId(veterinarioId).stream().map(this::toDto).toList();
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

    if (!veterinario.isActivo()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El veterinario no está activo");
    }

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

    Cita guardada = citaRepository.save(cita);
    notificacionService.notificarCitaCreada(guardada);
    return toDto(guardada);
  }

  @Transactional
  public CitaResponseDto cambiarEstado(Long id, EstadoUpdateDto dto) {
    Cita cita = citaRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));

    Set<Estado> permitidos = TRANSICIONES_VALIDAS.getOrDefault(cita.getEstado(), Set.of());
    if (!permitidos.contains(dto.estado())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Transición inválida de " + cita.getEstado() + " a " + dto.estado());
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
            .creadoEn(LocalDateTime.now())
            .diagnostico("")
            .tratamiento("")
            .observaciones("")
            .build();
        historialClinicoRepository.save(historial);
      }
    }

    Cita guardada = citaRepository.save(cita);
    if (dto.estado() == Estado.CONFIRMADA || dto.estado() == Estado.CANCELADA) {
      notificacionService.notificarCambioEstado(guardada, dto.estado());
    }
    return toDto(guardada);
  }

  @Transactional
  public void cancelar(Long id, CanceladoPor canceladoPor) {
    Cita cita = citaRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));

    if (cita.getEstado() == Estado.COMPLETADA) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "No se puede cancelar una cita completada");
    }
    cita.setEstado(Estado.CANCELADA);
    cita.setCanceladoPor(canceladoPor);
    cita.setFechaCancelacion(LocalDateTime.now());
    Cita guardada = citaRepository.save(cita);
    notificacionService.notificarCambioEstado(guardada, Estado.CANCELADA);
  }

  @Transactional
  public CitaResponseDto registrarPago(Long id, PagoRequestDto dto) {
    Cita cita = citaRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));

    cita.setPagado(true);
    cita.setMetodoPago(dto.metodo());
    cita.setMontoPago(dto.monto());
    cita.setFechaPago(LocalDateTime.now());

    return toDto(citaRepository.save(cita));
  }

  @Transactional
  public CitaResponseDto reprogramar(Long id, ReprogramarCitaDto dto) {
    Cita cita = citaRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));

    if (cita.getEstado() != Estado.PENDIENTE && cita.getEstado() != Estado.CONFIRMADA) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Solo se pueden reprogramar citas PENDIENTE o CONFIRMADA");
    }

    if (!dto.fechaHora().isAfter(LocalDateTime.now())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "La fecha y hora de la cita deben ser futuras");
    }

    LocalDateTime fin = dto.fechaHora().plusMinutes(cita.getServicio().getDuracionMin());
    List<Cita> conflictos = citaRepository.findConflictingCitas(
        cita.getVeterinario().getId(), dto.fechaHora(), fin);
    boolean chocaConOtraCita = conflictos.stream().anyMatch(c -> !c.getId().equals(id));
    if (chocaConOtraCita) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,
          "El veterinario ya tiene una cita en ese horario");
    }

    cita.setFechaHora(dto.fechaHora());
    return toDto(citaRepository.save(cita));
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

  // Business rule: recordatorio 24h antes de una cita CONFIRMADA
  @Scheduled(fixedRate = 3_600_000)
  @Transactional
  public void enviarRecordatorios() {
    LocalDateTime desde = LocalDateTime.now().plusHours(23);
    LocalDateTime hasta = LocalDateTime.now().plusHours(25);
    List<Cita> citas = citaRepository.findByFechaHoraBetween(desde, hasta).stream()
        .filter(c -> c.getEstado() == Estado.CONFIRMADA && !c.isRecordatorioEnviado())
        .toList();
    citas.forEach(c -> {
      notificacionService.notificarRecordatorio(c);
      c.setRecordatorioEnviado(true);
    });
    citaRepository.saveAll(citas);
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
        c.getServicio() != null ? c.getServicio().getPrecio() : 0.0,
        c.getCanceladoPor(),
        c.getFechaCancelacion(),
        c.isPagado(),
        c.getMetodoPago(),
        c.getMontoPago());
  }
}

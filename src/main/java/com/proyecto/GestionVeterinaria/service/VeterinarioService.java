package com.proyecto.GestionVeterinaria.service;

import com.proyecto.GestionVeterinaria.dto.cita.CitaResponseDto;
import com.proyecto.GestionVeterinaria.dto.veterinario.DisponibilidadResponseDto;
import com.proyecto.GestionVeterinaria.dto.veterinario.VeterinarioActivoResponseDto;
import com.proyecto.GestionVeterinaria.dto.veterinario.VeterinarioRequestDto;
import com.proyecto.GestionVeterinaria.dto.veterinario.VeterinarioResponseDto;
import com.proyecto.GestionVeterinaria.dto.veterinario.VeterinarioUpdateDto;
import com.proyecto.GestionVeterinaria.persistence.entity.Cita;
import com.proyecto.GestionVeterinaria.persistence.entity.RolesEntity;
import com.proyecto.GestionVeterinaria.persistence.entity.Servicio;
import com.proyecto.GestionVeterinaria.persistence.entity.Usuario;
import com.proyecto.GestionVeterinaria.persistence.entity.Veterinario;
import com.proyecto.GestionVeterinaria.persistence.entity.AusenciaVeterinario;
import com.proyecto.GestionVeterinaria.persistence.entity.HorarioVeterinario;
import com.proyecto.GestionVeterinaria.persistence.enumerates.Estado;
import com.proyecto.GestionVeterinaria.persistence.enumerates.Rol;
import com.proyecto.GestionVeterinaria.repository.AusenciaVeterinarioRepository;
import com.proyecto.GestionVeterinaria.repository.CitaRepository;
import com.proyecto.GestionVeterinaria.repository.HorarioVeterinarioRepository;
import com.proyecto.GestionVeterinaria.repository.RolesRepository;
import com.proyecto.GestionVeterinaria.repository.ServicioRepository;
import com.proyecto.GestionVeterinaria.repository.UsuarioRepository;
import com.proyecto.GestionVeterinaria.repository.VeterinarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class VeterinarioService {

  private final VeterinarioRepository veterinarioRepository;
  private final UsuarioRepository usuarioRepository;
  private final RolesRepository rolesRepository;
  private final PasswordEncoder passwordEncoder;
  private final CitaRepository citaRepository;
  private final ServicioRepository servicioRepository;
  private final CitaService citaService;
  private final HorarioVeterinarioRepository horarioVeterinarioRepository;
  private final AusenciaVeterinarioRepository ausenciaVeterinarioRepository;

  private static final LocalTime HORA_INICIO = LocalTime.of(8, 0);
  private static final LocalTime HORA_FIN = LocalTime.of(18, 0);
  private static final int SLOT_MINUTOS = 30;

  public List<VeterinarioResponseDto> findAll(boolean soloActivos) {
    return veterinarioRepository.findAll().stream()
        .filter(v -> !soloActivos || v.isActivo())
        .map(this::toDto)
        .toList();
  }

  public VeterinarioResponseDto findById(Long id) {
    return veterinarioRepository.findById(id)
        .map(this::toDto)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinario no encontrado"));
  }

  @Transactional
  public VeterinarioResponseDto create(VeterinarioRequestDto dto) {
    if (usuarioRepository.findByUsername(dto.username()).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "El username ya existe");
    }

    if (veterinarioRepository.existsByColegiatura(dto.colegiatura())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "La colegiatura ya está registrada");
    }

    RolesEntity veterinarioRole = rolesRepository.findByRol(Rol.VETERINARIO)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
            "Rol VETERINARIO no encontrado. Verifique los datos iniciales."));

    Usuario usuario = Usuario.builder()
        .username(dto.username())
        .email(dto.email())
        .password(passwordEncoder.encode(dto.password()))
        .activo(true)
        .dateRegister(LocalDate.now())
        .isEnabled(true)
        .isAccountNonExpired(true)
        .isAccountNonLocked(true)
        .isCredentialsNonExpired(true)
        .roles(Set.of(veterinarioRole))
        .build();

    usuarioRepository.save(usuario);

    Veterinario veterinario = Veterinario.builder()
        .nombres(dto.nombres())
        .apellidos(dto.apellidos())
        .especialidad(dto.especialidad())
        .colegiatura(dto.colegiatura())
        .usuario(usuario)
        .build();

    return toDto(veterinarioRepository.save(veterinario));
  }

  @Transactional
  public VeterinarioResponseDto update(Long id, VeterinarioUpdateDto dto) {
    Veterinario veterinario = veterinarioRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinario no encontrado"));

    veterinario.setNombres(dto.nombres());
    veterinario.setApellidos(dto.apellidos());
    veterinario.setEspecialidad(dto.especialidad());
    veterinario.setColegiatura(dto.colegiatura());

    return toDto(veterinarioRepository.save(veterinario));
  }

  @Transactional
  public VeterinarioActivoResponseDto setActivo(Long id, boolean activo) {
    Veterinario veterinario = veterinarioRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinario no encontrado"));

    veterinario.setActivo(activo);
    if (veterinario.getUsuario() != null) {
      veterinario.getUsuario().setEnabled(activo);
      veterinario.getUsuario().setActivo(activo);
      usuarioRepository.save(veterinario.getUsuario());
    }
    veterinarioRepository.save(veterinario);

    List<CitaResponseDto> citasAfectadas = List.of();
    if (!activo) {
      LocalDateTime ahora = LocalDateTime.now();
      citasAfectadas = citaRepository.findByVeterinarioId(id).stream()
          .filter(c -> (c.getEstado() == Estado.PENDIENTE || c.getEstado() == Estado.CONFIRMADA)
              && c.getFechaHora().isAfter(ahora))
          .map(citaService::toDto)
          .toList();
    }

    return new VeterinarioActivoResponseDto(toDto(veterinario), citasAfectadas);
  }

  public DisponibilidadResponseDto getDisponibilidad(Long id, LocalDate fecha, Long servicioId) {
    veterinarioRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinario no encontrado"));

    boolean enAusencia = ausenciaVeterinarioRepository.findByVeterinarioId(id).stream()
        .anyMatch(a -> !fecha.isBefore(a.getFechaInicio()) && !fecha.isAfter(a.getFechaFin()));
    if (enAusencia) {
      return new DisponibilidadResponseDto(id, fecha, List.of());
    }

    List<HorarioVeterinario> horarioDelDia = horarioVeterinarioRepository
        .findByVeterinarioIdAndDiaSemana(id, fecha.getDayOfWeek());
    LocalTime horaInicio = horarioDelDia.isEmpty() ? HORA_INICIO : horarioDelDia.get(0).getHoraInicio();
    LocalTime horaFin = horarioDelDia.isEmpty() ? HORA_FIN : horarioDelDia.get(0).getHoraFin();

    Integer duracionServicio = null;
    if (servicioId != null) {
      Servicio servicio = servicioRepository.findById(servicioId)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Servicio no encontrado"));
      duracionServicio = servicio.getDuracionMin();
    }

    LocalDateTime desde = fecha.atTime(horaInicio);
    LocalDateTime hasta = fecha.atTime(horaFin);

    List<Cita> citasDelDia = citaRepository.findByVeterinarioAndDateRange(id, desde, hasta);

    List<LocalTime> slotsDisponibles = new ArrayList<>();
    LocalTime slot = horaInicio;
    Integer duracionFinal = duracionServicio;

    while (slot.isBefore(horaFin)) {
      LocalDateTime slotDt = fecha.atTime(slot);
      boolean ocupado;
      if (duracionFinal != null) {
        LocalDateTime slotFin = slotDt.plusMinutes(duracionFinal);
        ocupado = citasDelDia.stream().anyMatch(c -> {
          LocalDateTime citaInicio = c.getFechaHora();
          LocalDateTime citaFin = citaInicio.plusMinutes(c.getServicio().getDuracionMin());
          return slotDt.isBefore(citaFin) && citaInicio.isBefore(slotFin);
        });
      } else {
        ocupado = citasDelDia.stream().anyMatch(c -> {
          LocalDateTime citaInicio = c.getFechaHora();
          LocalDateTime citaFin = citaInicio.plusMinutes(c.getServicio().getDuracionMin());
          return !slotDt.isBefore(citaInicio) && slotDt.isBefore(citaFin);
        });
      }
      if (!ocupado) {
        slotsDisponibles.add(slot);
      }
      slot = slot.plusMinutes(SLOT_MINUTOS);
    }

    return new DisponibilidadResponseDto(id, fecha, slotsDisponibles);
  }

  public VeterinarioResponseDto toDto(Veterinario v) {
    return new VeterinarioResponseDto(
        v.getId(),
        v.getNombres(),
        v.getApellidos(),
        v.getEspecialidad(),
        v.getColegiatura(),
        v.getUsuario() != null ? v.getUsuario().getEmail() : null,
        v.getUsuario() != null ? v.getUsuario().getUsername() : null,
        v.isActivo());
  }
}

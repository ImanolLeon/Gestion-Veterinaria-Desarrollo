package com.proyecto.GestionVeterinaria.service;

import com.proyecto.GestionVeterinaria.dto.veterinario.DisponibilidadResponseDto;
import com.proyecto.GestionVeterinaria.dto.veterinario.VeterinarioRequestDto;
import com.proyecto.GestionVeterinaria.dto.veterinario.VeterinarioResponseDto;
import com.proyecto.GestionVeterinaria.persistence.entity.Cita;
import com.proyecto.GestionVeterinaria.persistence.entity.RolesEntity;
import com.proyecto.GestionVeterinaria.persistence.entity.Usuario;
import com.proyecto.GestionVeterinaria.persistence.entity.Veterinario;
import com.proyecto.GestionVeterinaria.persistence.enumerates.Rol;
import com.proyecto.GestionVeterinaria.repository.CitaRepository;
import com.proyecto.GestionVeterinaria.repository.RolesRepository;
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

  private static final LocalTime HORA_INICIO = LocalTime.of(8, 0);
  private static final LocalTime HORA_FIN = LocalTime.of(18, 0);
  private static final int SLOT_MINUTOS = 30;

  public List<VeterinarioResponseDto> findAll() {
    return veterinarioRepository.findAll().stream()
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

  public DisponibilidadResponseDto getDisponibilidad(Long id, LocalDate fecha) {
    veterinarioRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinario no encontrado"));

    LocalDateTime desde = fecha.atTime(HORA_INICIO);
    LocalDateTime hasta = fecha.atTime(HORA_FIN);

    List<Cita> citasDelDia = citaRepository.findByVeterinarioAndDateRange(id, desde, hasta);

    List<LocalTime> slotsDisponibles = new ArrayList<>();
    LocalTime slot = HORA_INICIO;

    while (slot.isBefore(HORA_FIN)) {
      LocalDateTime slotDt = fecha.atTime(slot);
      boolean ocupado = citasDelDia.stream().anyMatch(c -> {
        LocalDateTime citaInicio = c.getFechaHora();
        LocalDateTime citaFin = citaInicio.plusMinutes(c.getServicio().getDuracionMin());
        return !slotDt.isBefore(citaInicio) && slotDt.isBefore(citaFin);
      });
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
        v.getUsuario() != null ? v.getUsuario().getUsername() : null);
  }
}

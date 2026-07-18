package com.proyecto.GestionVeterinaria.service;

import com.proyecto.GestionVeterinaria.dto.veterinario.HorarioVeterinarioRequestDto;
import com.proyecto.GestionVeterinaria.dto.veterinario.HorarioVeterinarioResponseDto;
import com.proyecto.GestionVeterinaria.persistence.entity.HorarioVeterinario;
import com.proyecto.GestionVeterinaria.persistence.entity.Veterinario;
import com.proyecto.GestionVeterinaria.repository.HorarioVeterinarioRepository;
import com.proyecto.GestionVeterinaria.repository.VeterinarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HorarioVeterinarioService {

  private final HorarioVeterinarioRepository horarioVeterinarioRepository;
  private final VeterinarioRepository veterinarioRepository;

  public List<HorarioVeterinarioResponseDto> findByVeterinario(Long veterinarioId) {
    return horarioVeterinarioRepository.findByVeterinarioId(veterinarioId).stream()
        .map(this::toDto)
        .toList();
  }

  public HorarioVeterinarioResponseDto create(Long veterinarioId, HorarioVeterinarioRequestDto dto) {
    Veterinario veterinario = veterinarioRepository.findById(veterinarioId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinario no encontrado"));

    HorarioVeterinario horario = HorarioVeterinario.builder()
        .veterinario(veterinario)
        .diaSemana(dto.diaSemana())
        .horaInicio(dto.horaInicio())
        .horaFin(dto.horaFin())
        .build();

    return toDto(horarioVeterinarioRepository.save(horario));
  }

  public HorarioVeterinarioResponseDto update(Long id, HorarioVeterinarioRequestDto dto) {
    HorarioVeterinario horario = horarioVeterinarioRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Horario no encontrado"));

    horario.setDiaSemana(dto.diaSemana());
    horario.setHoraInicio(dto.horaInicio());
    horario.setHoraFin(dto.horaFin());

    return toDto(horarioVeterinarioRepository.save(horario));
  }

  public void delete(Long id) {
    if (!horarioVeterinarioRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Horario no encontrado");
    }
    horarioVeterinarioRepository.deleteById(id);
  }

  private HorarioVeterinarioResponseDto toDto(HorarioVeterinario h) {
    return new HorarioVeterinarioResponseDto(
        h.getId(),
        h.getVeterinario() != null ? h.getVeterinario().getId() : null,
        h.getDiaSemana(),
        h.getHoraInicio(),
        h.getHoraFin());
  }
}

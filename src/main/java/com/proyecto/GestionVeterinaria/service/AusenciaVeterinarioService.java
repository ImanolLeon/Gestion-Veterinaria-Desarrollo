package com.proyecto.GestionVeterinaria.service;

import com.proyecto.GestionVeterinaria.dto.veterinario.AusenciaVeterinarioRequestDto;
import com.proyecto.GestionVeterinaria.dto.veterinario.AusenciaVeterinarioResponseDto;
import com.proyecto.GestionVeterinaria.persistence.entity.AusenciaVeterinario;
import com.proyecto.GestionVeterinaria.persistence.entity.Veterinario;
import com.proyecto.GestionVeterinaria.repository.AusenciaVeterinarioRepository;
import com.proyecto.GestionVeterinaria.repository.VeterinarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AusenciaVeterinarioService {

  private final AusenciaVeterinarioRepository ausenciaVeterinarioRepository;
  private final VeterinarioRepository veterinarioRepository;

  public List<AusenciaVeterinarioResponseDto> findByVeterinario(Long veterinarioId) {
    return ausenciaVeterinarioRepository.findByVeterinarioId(veterinarioId).stream()
        .map(this::toDto)
        .toList();
  }

  public AusenciaVeterinarioResponseDto create(Long veterinarioId, AusenciaVeterinarioRequestDto dto) {
    Veterinario veterinario = veterinarioRepository.findById(veterinarioId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinario no encontrado"));

    if (dto.fechaFin().isBefore(dto.fechaInicio())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "La fecha de fin no puede ser anterior a la fecha de inicio");
    }

    AusenciaVeterinario ausencia = AusenciaVeterinario.builder()
        .veterinario(veterinario)
        .fechaInicio(dto.fechaInicio())
        .fechaFin(dto.fechaFin())
        .motivo(dto.motivo())
        .build();

    return toDto(ausenciaVeterinarioRepository.save(ausencia));
  }

  public AusenciaVeterinarioResponseDto update(Long id, AusenciaVeterinarioRequestDto dto) {
    AusenciaVeterinario ausencia = ausenciaVeterinarioRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ausencia no encontrada"));

    if (dto.fechaFin().isBefore(dto.fechaInicio())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "La fecha de fin no puede ser anterior a la fecha de inicio");
    }

    ausencia.setFechaInicio(dto.fechaInicio());
    ausencia.setFechaFin(dto.fechaFin());
    ausencia.setMotivo(dto.motivo());

    return toDto(ausenciaVeterinarioRepository.save(ausencia));
  }

  public void delete(Long id) {
    if (!ausenciaVeterinarioRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ausencia no encontrada");
    }
    ausenciaVeterinarioRepository.deleteById(id);
  }

  private AusenciaVeterinarioResponseDto toDto(AusenciaVeterinario a) {
    return new AusenciaVeterinarioResponseDto(
        a.getId(),
        a.getVeterinario() != null ? a.getVeterinario().getId() : null,
        a.getFechaInicio(),
        a.getFechaFin(),
        a.getMotivo());
  }
}

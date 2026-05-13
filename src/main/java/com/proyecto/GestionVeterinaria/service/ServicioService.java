package com.proyecto.GestionVeterinaria.service;

import com.proyecto.GestionVeterinaria.dto.servicio.ServicioRequestDto;
import com.proyecto.GestionVeterinaria.dto.servicio.ServicioResponseDto;
import com.proyecto.GestionVeterinaria.persistence.entity.Servicio;
import com.proyecto.GestionVeterinaria.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicioService {

  private final ServicioRepository servicioRepository;

  public List<ServicioResponseDto> findActivos() {
    return servicioRepository.findByActivoTrue().stream()
        .map(this::toDto)
        .toList();
  }

  public List<ServicioResponseDto> findAll() {
    return servicioRepository.findAll().stream()
        .map(this::toDto)
        .toList();
  }

  public ServicioResponseDto create(ServicioRequestDto dto) {
    Servicio servicio = Servicio.builder()
        .nombre(dto.nombre())
        .descripcion(dto.descripcion())
        .duracionMin(dto.duracionMin())
        .precio(dto.precio())
        .activo(dto.activo())
        .build();
    return toDto(servicioRepository.save(servicio));
  }

  public ServicioResponseDto update(Long id, ServicioRequestDto dto) {
    Servicio servicio = servicioRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Servicio no encontrado"));
    servicio.setNombre(dto.nombre());
    servicio.setDescripcion(dto.descripcion());
    servicio.setDuracionMin(dto.duracionMin());
    servicio.setPrecio(dto.precio());
    servicio.setActivo(dto.activo());
    return toDto(servicioRepository.save(servicio));
  }

  public ServicioResponseDto toDto(Servicio s) {
    return new ServicioResponseDto(s.getId(), s.getNombre(), s.getDescripcion(),
        s.getDuracionMin(), s.getPrecio(), s.isActivo());
  }
}

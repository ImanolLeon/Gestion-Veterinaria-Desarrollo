package com.proyecto.GestionVeterinaria.service;

import com.proyecto.GestionVeterinaria.dto.cliente.ClienteResponseDto;
import com.proyecto.GestionVeterinaria.dto.cliente.ClienteUpdateDto;
import com.proyecto.GestionVeterinaria.persistence.entity.Cliente;
import com.proyecto.GestionVeterinaria.persistence.entity.Usuario;
import com.proyecto.GestionVeterinaria.repository.ClienteRepository;
import com.proyecto.GestionVeterinaria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

  private final ClienteRepository clienteRepository;
  private final UsuarioRepository usuarioRepository;

  public List<ClienteResponseDto> findAll() {
    return clienteRepository.findAll().stream()
        .map(this::toDto)
        .toList();
  }

  public ClienteResponseDto findById(Long id) {
    return clienteRepository.findById(id)
        .map(this::toDto)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
  }

  public ClienteResponseDto findByUsuarioId(Long usuarioId) {
    return clienteRepository.findByUsuarioId(usuarioId)
        .map(this::toDto)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil de cliente no encontrado"));
  }

  public ClienteResponseDto findByUsername(String username) {
    return usuarioRepository.findByUsername(username)
        .map(Usuario::getId)
        .flatMap(clienteRepository::findByUsuarioId)
        .map(this::toDto)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil de cliente no encontrado"));
  }

  public ClienteResponseDto update(Long id, ClienteUpdateDto dto) {
    Cliente cliente = clienteRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));

    // Check DNI uniqueness if it changed
    if (!cliente.getDni().equals(dto.dni()) && clienteRepository.existsByDni(dto.dni())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "El DNI ya está registrado");
    }

    cliente.setNombres(dto.nombres());
    cliente.setApellidos(dto.apellidos());
    cliente.setDni(dto.dni());
    cliente.setTelefono(dto.telefono());
    cliente.setDireccion(dto.direccion());

    return toDto(clienteRepository.save(cliente));
  }

  public ClienteResponseDto toDto(Cliente c) {
    return new ClienteResponseDto(
        c.getId(),
        c.getNombres(),
        c.getApellidos(),
        c.getDni(),
        c.getTelefono(),
        c.getDireccion(),
        c.getUsuario() != null ? c.getUsuario().getEmail() : null,
        c.getUsuario() != null ? c.getUsuario().getUsername() : null);
  }
}

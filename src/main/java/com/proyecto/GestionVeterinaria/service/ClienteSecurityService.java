package com.proyecto.GestionVeterinaria.service;

import com.proyecto.GestionVeterinaria.persistence.entity.Cliente;
import com.proyecto.GestionVeterinaria.persistence.entity.Usuario;
import com.proyecto.GestionVeterinaria.repository.ClienteRepository;
import com.proyecto.GestionVeterinaria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("clienteSecurityService")
@RequiredArgsConstructor
public class ClienteSecurityService {

  private final ClienteRepository clienteRepository;
  private final UsuarioRepository usuarioRepository;

  public boolean isOwner(Long clienteId, String username) {
    return usuarioRepository.findByUsername(username)
        .map(Usuario::getId)
        .flatMap(clienteRepository::findByUsuarioId)
        .map(Cliente::getId)
        .map(id -> id.equals(clienteId))
        .orElse(false);
  }
}

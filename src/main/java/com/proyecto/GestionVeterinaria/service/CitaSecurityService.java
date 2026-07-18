package com.proyecto.GestionVeterinaria.service;

import com.proyecto.GestionVeterinaria.persistence.entity.Cita;
import com.proyecto.GestionVeterinaria.persistence.entity.Usuario;
import com.proyecto.GestionVeterinaria.persistence.entity.Veterinario;
import com.proyecto.GestionVeterinaria.repository.CitaRepository;
import com.proyecto.GestionVeterinaria.repository.UsuarioRepository;
import com.proyecto.GestionVeterinaria.repository.VeterinarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("citaSecurityService")
@RequiredArgsConstructor
public class CitaSecurityService {

  private final CitaRepository citaRepository;
  private final VeterinarioRepository veterinarioRepository;
  private final UsuarioRepository usuarioRepository;

  public boolean isOwnerOfCita(Long citaId, String username) {
    return citaRepository.findById(citaId)
        .map(Cita::getMascota)
        .filter(mascota -> mascota != null && mascota.getCliente() != null
            && mascota.getCliente().getUsuario() != null)
        .map(mascota -> username.equals(mascota.getCliente().getUsuario().getUsername()))
        .orElse(false);
  }

  public boolean isAssignedVet(Long citaId, String username) {
    return citaRepository.findById(citaId)
        .map(Cita::getVeterinario)
        .filter(vet -> vet != null && vet.getUsuario() != null)
        .map(vet -> username.equals(vet.getUsuario().getUsername()))
        .orElse(false);
  }

  public Optional<Long> resolveVeterinarioId(String username) {
    return usuarioRepository.findByUsername(username)
        .map(Usuario::getId)
        .flatMap(veterinarioRepository::findByUsuarioId)
        .map(Veterinario::getId);
  }
}

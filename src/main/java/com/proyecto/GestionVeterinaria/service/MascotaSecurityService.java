package com.proyecto.GestionVeterinaria.service;

import com.proyecto.GestionVeterinaria.persistence.entity.Mascota;
import com.proyecto.GestionVeterinaria.repository.MascotaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("mascotaSecurityService")
@RequiredArgsConstructor
public class MascotaSecurityService {

  private final MascotaRepository mascotaRepository;

  public boolean isOwner(Long mascotaId, String username) {
    return mascotaRepository.findById(mascotaId)
        .map(Mascota::getCliente)
        .filter(cliente -> cliente.getUsuario() != null)
        .map(cliente -> username.equals(cliente.getUsuario().getUsername()))
        .orElse(false);
  }
}

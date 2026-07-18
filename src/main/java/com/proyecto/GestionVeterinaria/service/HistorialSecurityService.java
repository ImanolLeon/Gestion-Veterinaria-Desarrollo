package com.proyecto.GestionVeterinaria.service;

import com.proyecto.GestionVeterinaria.persistence.entity.HistorialClinico;
import com.proyecto.GestionVeterinaria.repository.HistorialClinicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("historialSecurityService")
@RequiredArgsConstructor
public class HistorialSecurityService {

  private final HistorialClinicoRepository historialClinicoRepository;

  public boolean isAssignedVet(Long historialId, String username) {
    return historialClinicoRepository.findById(historialId)
        .map(HistorialClinico::getCita)
        .filter(cita -> cita != null && cita.getVeterinario() != null
            && cita.getVeterinario().getUsuario() != null)
        .map(cita -> username.equals(cita.getVeterinario().getUsuario().getUsername()))
        .orElse(false);
  }
}

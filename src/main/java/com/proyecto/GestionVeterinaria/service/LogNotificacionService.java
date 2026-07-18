package com.proyecto.GestionVeterinaria.service;

import com.proyecto.GestionVeterinaria.persistence.entity.Cita;
import com.proyecto.GestionVeterinaria.persistence.enumerates.Estado;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementación provisional sin infraestructura de correo real: registra la
 * notificación en el log. Reemplazar por una implementación SMTP cuando exista
 * infraestructura de envío de correos.
 */
@Slf4j
@Service
public class LogNotificacionService implements NotificacionService {

  @Override
  public void notificarCitaCreada(Cita cita) {
    log.info("[Notificación] Cita #{} creada para el {} con el veterinario {}",
        cita.getId(), cita.getFechaHora(), nombreVeterinario(cita));
  }

  @Override
  public void notificarCambioEstado(Cita cita, Estado nuevoEstado) {
    log.info("[Notificación] Cita #{} cambió a estado {}", cita.getId(), nuevoEstado);
  }

  @Override
  public void notificarRecordatorio(Cita cita) {
    log.info("[Notificación] Recordatorio: cita #{} programada para el {} con el veterinario {}",
        cita.getId(), cita.getFechaHora(), nombreVeterinario(cita));
  }

  private String nombreVeterinario(Cita cita) {
    return cita.getVeterinario() != null
        ? cita.getVeterinario().getNombres() + " " + cita.getVeterinario().getApellidos()
        : "N/A";
  }
}

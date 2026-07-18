package com.proyecto.GestionVeterinaria.service;

import com.proyecto.GestionVeterinaria.persistence.entity.Cita;
import com.proyecto.GestionVeterinaria.persistence.enumerates.Estado;

public interface NotificacionService {

  void notificarCitaCreada(Cita cita);

  void notificarCambioEstado(Cita cita, Estado nuevoEstado);

  void notificarRecordatorio(Cita cita);
}

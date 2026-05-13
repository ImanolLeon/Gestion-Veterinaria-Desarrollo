package com.proyecto.GestionVeterinaria.repository;

import com.proyecto.GestionVeterinaria.persistence.entity.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {
  List<Servicio> findByActivoTrue();
}

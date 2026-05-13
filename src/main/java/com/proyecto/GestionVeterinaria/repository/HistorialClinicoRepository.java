package com.proyecto.GestionVeterinaria.repository;

import com.proyecto.GestionVeterinaria.persistence.entity.HistorialClinico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HistorialClinicoRepository extends JpaRepository<HistorialClinico, Long> {
  List<HistorialClinico> findByMascotaIdOrderByFechaDesc(Long mascotaId);

  Optional<HistorialClinico> findByCitaId(Long citaId);
}

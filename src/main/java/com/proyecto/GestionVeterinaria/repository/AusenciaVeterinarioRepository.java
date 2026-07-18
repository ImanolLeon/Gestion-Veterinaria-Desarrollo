package com.proyecto.GestionVeterinaria.repository;

import com.proyecto.GestionVeterinaria.persistence.entity.AusenciaVeterinario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AusenciaVeterinarioRepository extends JpaRepository<AusenciaVeterinario, Long> {
  List<AusenciaVeterinario> findByVeterinarioId(Long veterinarioId);
}

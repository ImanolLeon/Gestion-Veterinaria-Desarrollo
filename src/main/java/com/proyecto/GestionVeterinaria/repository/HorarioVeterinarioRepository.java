package com.proyecto.GestionVeterinaria.repository;

import com.proyecto.GestionVeterinaria.persistence.entity.HorarioVeterinario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface HorarioVeterinarioRepository extends JpaRepository<HorarioVeterinario, Long> {
  List<HorarioVeterinario> findByVeterinarioId(Long veterinarioId);

  List<HorarioVeterinario> findByVeterinarioIdAndDiaSemana(Long veterinarioId, DayOfWeek diaSemana);
}

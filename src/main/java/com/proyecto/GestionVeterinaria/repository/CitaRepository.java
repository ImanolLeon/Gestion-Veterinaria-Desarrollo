package com.proyecto.GestionVeterinaria.repository;

import com.proyecto.GestionVeterinaria.persistence.entity.Cita;
import com.proyecto.GestionVeterinaria.persistence.enumerates.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

  List<Cita> findByMascotaId(Long mascotaId);

  @Query("SELECT c FROM Cita c WHERE c.mascota.cliente.usuario.username = :username ORDER BY c.fechaHora DESC")
  List<Cita> findByCliente(@Param("username") String username);

  List<Cita> findByVeterinarioId(Long veterinarioId);

  List<Cita> findByVeterinarioIdAndEstado(Long veterinarioId, Estado estado);

  List<Cita> findByEstadoAndFechaHoraBefore(Estado estado, LocalDateTime fechaHora);

  @Query("""
      SELECT c FROM Cita c
      WHERE c.veterinario.id = :vetId
        AND c.estado NOT IN ('CANCELADA')
        AND c.fechaHora < :fin
        AND FUNCTION('TIMESTAMPADD', MINUTE, c.servicio.duracionMin, c.fechaHora) > :inicio
      """)
  List<Cita> findConflictingCitas(@Param("vetId") Long vetId,
      @Param("inicio") LocalDateTime inicio,
      @Param("fin") LocalDateTime fin);

  @Query("""
      SELECT c FROM Cita c
      WHERE c.veterinario.id = :vetId
        AND c.estado NOT IN ('CANCELADA')
        AND c.fechaHora >= :desde
        AND c.fechaHora < :hasta
      """)
  List<Cita> findByVeterinarioAndDateRange(@Param("vetId") Long vetId,
      @Param("desde") LocalDateTime desde,
      @Param("hasta") LocalDateTime hasta);

  @Query("""
      SELECT c FROM Cita c
      WHERE c.fechaHora >= :desde AND c.fechaHora <= :hasta
      """)
  List<Cita> findByFechaHoraBetween(@Param("desde") LocalDateTime desde,
      @Param("hasta") LocalDateTime hasta);
}

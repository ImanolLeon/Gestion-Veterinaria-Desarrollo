package com.proyecto.GestionVeterinaria.repository;

import com.proyecto.GestionVeterinaria.persistence.entity.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long> {
  List<Mascota> findByClienteIdAndActivoTrue(Long clienteId);

  Optional<Mascota> findByIdAndActivoTrue(Long id);
}

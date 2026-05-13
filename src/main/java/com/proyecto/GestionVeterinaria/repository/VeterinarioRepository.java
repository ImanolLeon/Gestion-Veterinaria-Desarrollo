package com.proyecto.GestionVeterinaria.repository;

import com.proyecto.GestionVeterinaria.persistence.entity.Veterinario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VeterinarioRepository extends JpaRepository<Veterinario, Long> {
  Optional<Veterinario> findByUsuarioId(Long usuarioId);

  boolean existsByColegiatura(String colegiatura);
}

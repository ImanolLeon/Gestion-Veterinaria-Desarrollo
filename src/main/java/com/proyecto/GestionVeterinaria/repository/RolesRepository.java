package com.proyecto.GestionVeterinaria.repository;

import com.proyecto.GestionVeterinaria.persistence.entity.RolesEntity;
import com.proyecto.GestionVeterinaria.persistence.enumerates.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolesRepository extends JpaRepository<RolesEntity, Long> {
  Optional<RolesEntity> findByRol(Rol rol);
}

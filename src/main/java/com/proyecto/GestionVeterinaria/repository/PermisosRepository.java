package com.proyecto.GestionVeterinaria.repository;

import com.proyecto.GestionVeterinaria.persistence.entity.PermisosEntity;
import com.proyecto.GestionVeterinaria.persistence.enumerates.Permisos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermisosRepository extends JpaRepository<PermisosEntity, Long> {
  Optional<PermisosEntity> findByPermisos(Permisos permisos);
}

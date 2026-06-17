package com.proyecto.GestionVeterinaria.config;

import com.proyecto.GestionVeterinaria.persistence.entity.PermisosEntity;
import com.proyecto.GestionVeterinaria.persistence.entity.RolesEntity;
import com.proyecto.GestionVeterinaria.persistence.enumerates.Permisos;
import com.proyecto.GestionVeterinaria.persistence.enumerates.Rol;
import com.proyecto.GestionVeterinaria.repository.PermisosRepository;
import com.proyecto.GestionVeterinaria.repository.RolesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Crea los roles base (y sus permisos) al iniciar el proyecto si aún no existen.
 * Idempotente: cada arranque solo inserta lo que falte.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

  private final RolesRepository rolesRepository;
  private final PermisosRepository permisosRepository;

  @Override
  @Transactional
  public void run(String... args) {
    seedRol(Rol.ADMIN, Permisos.VER, Permisos.CREAR, Permisos.EDITAR, Permisos.ACTUALIZAR);
    seedRol(Rol.VETERINARIO, Permisos.VER, Permisos.CREAR, Permisos.EDITAR, Permisos.ACTUALIZAR);
    seedRol(Rol.CLIENTE, Permisos.VER);
  }

  private void seedRol(Rol rol, Permisos... permisos) {
    if (rolesRepository.findByRol(rol).isPresent()) {
      return;
    }

    Set<PermisosEntity> permisosEntities = new HashSet<>();
    for (Permisos permiso : permisos) {
      permisosEntities.add(obtenerOCrearPermiso(permiso));
    }

    RolesEntity rolEntity = RolesEntity.builder()
        .rol(rol)
        .permisos(permisosEntities)
        .build();

    rolesRepository.save(rolEntity);
    log.info("Rol inicial creado: {}", rol);
  }

  private PermisosEntity obtenerOCrearPermiso(Permisos permiso) {
    return permisosRepository.findByPermisos(permiso)
        .orElseGet(() -> permisosRepository.save(
            PermisosEntity.builder().permisos(permiso).build()));
  }
}

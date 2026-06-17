package com.proyecto.GestionVeterinaria.config;

import com.proyecto.GestionVeterinaria.persistence.entity.PermisosEntity;
import com.proyecto.GestionVeterinaria.persistence.entity.RolesEntity;
import com.proyecto.GestionVeterinaria.persistence.entity.Usuario;
import com.proyecto.GestionVeterinaria.persistence.entity.Veterinario;
import com.proyecto.GestionVeterinaria.persistence.enumerates.Permisos;
import com.proyecto.GestionVeterinaria.persistence.enumerates.Rol;
import com.proyecto.GestionVeterinaria.repository.PermisosRepository;
import com.proyecto.GestionVeterinaria.repository.RolesRepository;
import com.proyecto.GestionVeterinaria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Crea los roles base (con sus permisos) y los usuarios iniciales (admin y
 * veterinario) al iniciar el proyecto si aún no existen.
 * Idempotente: cada arranque solo inserta lo que falte.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

  private final RolesRepository rolesRepository;
  private final PermisosRepository permisosRepository;
  private final UsuarioRepository usuarioRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${app.seed.admin.username}")
  private String adminUsername;
  @Value("${app.seed.admin.email}")
  private String adminEmail;
  @Value("${app.seed.admin.password}")
  private String adminPassword;

  @Value("${app.seed.veterinario.username}")
  private String vetUsername;
  @Value("${app.seed.veterinario.email}")
  private String vetEmail;
  @Value("${app.seed.veterinario.password}")
  private String vetPassword;
  @Value("${app.seed.veterinario.nombres}")
  private String vetNombres;
  @Value("${app.seed.veterinario.apellidos}")
  private String vetApellidos;
  @Value("${app.seed.veterinario.especialidad}")
  private String vetEspecialidad;
  @Value("${app.seed.veterinario.colegiatura}")
  private String vetColegiatura;

  @Override
  @Transactional
  public void run(String... args) {
    RolesEntity adminRole = seedRol(Rol.ADMIN, Permisos.VER, Permisos.CREAR, Permisos.EDITAR, Permisos.ACTUALIZAR);
    RolesEntity veterinarioRole = seedRol(Rol.VETERINARIO, Permisos.VER, Permisos.CREAR, Permisos.EDITAR, Permisos.ACTUALIZAR);
    seedRol(Rol.CLIENTE, Permisos.VER);

    seedAdmin(adminRole);
    seedVeterinario(veterinarioRole);
  }

  private void seedAdmin(RolesEntity adminRole) {
    if (usuarioRepository.findByUsername(adminUsername).isPresent()) {
      return;
    }

    Usuario admin = construirUsuario(adminUsername, adminEmail, adminPassword, adminRole);
    usuarioRepository.save(admin);
    log.info("Usuario inicial ADMIN creado: {}", adminUsername);
  }

  private void seedVeterinario(RolesEntity veterinarioRole) {
    if (usuarioRepository.findByUsername(vetUsername).isPresent()) {
      return;
    }

    Usuario usuario = construirUsuario(vetUsername, vetEmail, vetPassword, veterinarioRole);

    Veterinario veterinario = Veterinario.builder()
        .nombres(vetNombres)
        .apellidos(vetApellidos)
        .especialidad(vetEspecialidad)
        .colegiatura(vetColegiatura)
        .usuario(usuario)
        .build();

    usuario.setVeterinario(veterinario);
    usuarioRepository.save(usuario);
    log.info("Usuario inicial VETERINARIO creado: {}", vetUsername);
  }

  private Usuario construirUsuario(String username, String email, String password, RolesEntity rol) {
    return Usuario.builder()
        .username(username)
        .email(email)
        .password(passwordEncoder.encode(password))
        .activo(true)
        .dateRegister(LocalDate.now())
        .isEnabled(true)
        .isAccountNonExpired(true)
        .isAccountNonLocked(true)
        .isCredentialsNonExpired(true)
        .roles(Set.of(rol))
        .build();
  }

  private RolesEntity seedRol(Rol rol, Permisos... permisos) {
    return rolesRepository.findByRol(rol).orElseGet(() -> {
      Set<PermisosEntity> permisosEntities = new HashSet<>();
      for (Permisos permiso : permisos) {
        permisosEntities.add(obtenerOCrearPermiso(permiso));
      }

      RolesEntity rolEntity = RolesEntity.builder()
          .rol(rol)
          .permisos(permisosEntities)
          .build();

      RolesEntity guardado = rolesRepository.save(rolEntity);
      log.info("Rol inicial creado: {}", rol);
      return guardado;
    });
  }

  private PermisosEntity obtenerOCrearPermiso(Permisos permiso) {
    return permisosRepository.findByPermisos(permiso)
        .orElseGet(() -> permisosRepository.save(
            PermisosEntity.builder().permisos(permiso).build()));
  }
}

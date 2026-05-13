package com.proyecto.GestionVeterinaria.repository;

import com.proyecto.GestionVeterinaria.persistence.entity.RefreshTokenEntity;
import com.proyecto.GestionVeterinaria.persistence.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
  Optional<RefreshTokenEntity> findByToken(String token);

  void deleteByUsuario(Usuario usuario);
}

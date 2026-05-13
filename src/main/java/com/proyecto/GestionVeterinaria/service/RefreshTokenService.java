package com.proyecto.GestionVeterinaria.service;

import com.proyecto.GestionVeterinaria.persistence.entity.RefreshTokenEntity;
import com.proyecto.GestionVeterinaria.persistence.entity.Usuario;
import com.proyecto.GestionVeterinaria.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;

  @Value("${jwt.refreshExpiration}")
  private long refreshExpiration;

  @Transactional
  public RefreshTokenEntity createRefreshToken(Usuario usuario) {
    refreshTokenRepository.deleteByUsuario(usuario);
    RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
        .usuario(usuario)
        .token(UUID.randomUUID().toString())
        .expiresAt(Instant.now().plusMillis(refreshExpiration))
        .build();
    return refreshTokenRepository.save(refreshToken);
  }

  @Transactional(readOnly = true)
  public RefreshTokenEntity validateToken(String token) {
    return refreshTokenRepository.findByToken(token)
        .filter(t -> t.getExpiresAt().isAfter(Instant.now()))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
            "Refresh token inválido o expirado"));
  }

  @Transactional
  public void deleteByUsuario(Usuario usuario) {
    refreshTokenRepository.deleteByUsuario(usuario);
  }
}

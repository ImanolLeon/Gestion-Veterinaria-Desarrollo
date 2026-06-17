package com.proyecto.GestionVeterinaria.service;

import com.proyecto.GestionVeterinaria.dto.auth.LoginRequestDto;
import com.proyecto.GestionVeterinaria.dto.auth.LoginResponseDto;
import com.proyecto.GestionVeterinaria.dto.auth.RefreshRequestDto;
import com.proyecto.GestionVeterinaria.dto.auth.RegisterRequestDto;
import com.proyecto.GestionVeterinaria.persistence.entity.Cliente;
import com.proyecto.GestionVeterinaria.persistence.entity.RefreshTokenEntity;
import com.proyecto.GestionVeterinaria.persistence.entity.RolesEntity;
import com.proyecto.GestionVeterinaria.persistence.entity.Usuario;
import com.proyecto.GestionVeterinaria.persistence.enumerates.Rol;
import com.proyecto.GestionVeterinaria.repository.ClienteRepository;
import com.proyecto.GestionVeterinaria.repository.RolesRepository;
import com.proyecto.GestionVeterinaria.repository.UsuarioRepository;
import com.proyecto.GestionVeterinaria.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UsuarioRepository usuarioRepository;
  private final ClienteRepository clienteRepository;
  private final RolesRepository rolesRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtUtils jwtUtils;
  private final UserDetailImpl userDetailImpl;
  private final RefreshTokenService refreshTokenService;

  @Transactional
  public LoginResponseDto register(RegisterRequestDto dto) {
    if (usuarioRepository.findByUsername(dto.username()).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "El username ya existe");
    }

    if (clienteRepository.existsByDni(dto.dni())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "El DNI ya está registrado");
    }

    RolesEntity clienteRole = rolesRepository.findByRol(Rol.CLIENTE)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
            "Rol CLIENTE no encontrado. Verifique los datos iniciales."));

    Usuario usuario = Usuario.builder()
        .username(dto.username())
        .email(dto.email())
        .password(passwordEncoder.encode(dto.password()))
        .activo(true)
        .dateRegister(LocalDate.now())
        .isEnabled(true)
        .isAccountNonExpired(true)
        .isAccountNonLocked(true)
        .isCredentialsNonExpired(true)
        .roles(Set.of(clienteRole))
        .build();

    usuarioRepository.save(usuario);

    Cliente cliente = Cliente.builder()
        .nombres(dto.nombres())
        .apellidos(dto.apellidos())
        .dni(dto.dni())
        .telefono(dto.telefono())
        .direccion(dto.direccion())
        .usuario(usuario)
        .build();

    clienteRepository.save(cliente);

    UserDetails userDetails = userDetailImpl.loadUserByUsername(dto.username());
    String accessToken = jwtUtils.generateToken(userDetails);
    RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(usuario);
    List<String> roles = List.of(Rol.CLIENTE.name());

    return new LoginResponseDto(accessToken, refreshToken.getToken(), dto.username(), roles);
  }

  public LoginResponseDto login(LoginRequestDto dto) {
    Authentication auth = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(dto.username(), dto.password()));

    UserDetails userDetails = (UserDetails) auth.getPrincipal();
    String accessToken = jwtUtils.generateToken(userDetails);

    Usuario usuario = usuarioRepository.findByUsername(dto.username())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(usuario);

    List<String> roles = userDetails.getAuthorities().stream()
        .map(a -> a.getAuthority())
        .filter(a -> a.startsWith("ROLE_"))
        .map(a -> a.substring(5))
        .toList();

    return new LoginResponseDto(accessToken, refreshToken.getToken(), userDetails.getUsername(), roles);
  }

  public LoginResponseDto refreshToken(RefreshRequestDto dto) {
    RefreshTokenEntity storedToken = refreshTokenService.validateToken(dto.refreshToken());
    Usuario usuario = storedToken.getUsuario();

    UserDetails userDetails = userDetailImpl.loadUserByUsername(usuario.getUsername());
    String newAccessToken = jwtUtils.generateToken(userDetails);
    RefreshTokenEntity newRefreshToken = refreshTokenService.createRefreshToken(usuario);

    List<String> roles = userDetails.getAuthorities().stream()
        .map(a -> a.getAuthority())
        .filter(a -> a.startsWith("ROLE_"))
        .map(a -> a.substring(5))
        .toList();

    return new LoginResponseDto(newAccessToken, newRefreshToken.getToken(), userDetails.getUsername(), roles);
  }

  public void logout(RefreshRequestDto dto) {
    RefreshTokenEntity storedToken = refreshTokenService.validateToken(dto.refreshToken());
    refreshTokenService.deleteByUsuario(storedToken.getUsuario());
  }
}

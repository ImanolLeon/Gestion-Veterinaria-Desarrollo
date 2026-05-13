package com.proyecto.GestionVeterinaria.controlers;

import com.proyecto.GestionVeterinaria.dto.auth.LoginRequestDto;
import com.proyecto.GestionVeterinaria.dto.auth.LoginResponseDto;
import com.proyecto.GestionVeterinaria.dto.auth.RefreshRequestDto;
import com.proyecto.GestionVeterinaria.dto.auth.RegisterRequestDto;
import com.proyecto.GestionVeterinaria.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public LoginResponseDto register(@Valid @RequestBody RegisterRequestDto dto) {
    return authService.register(dto);
  }

  @PostMapping("/login")
  public LoginResponseDto login(@Valid @RequestBody LoginRequestDto dto) {
    return authService.login(dto);
  }

  @PostMapping("/refresh")
  public LoginResponseDto refresh(@Valid @RequestBody RefreshRequestDto dto) {
    return authService.refreshToken(dto);
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(@Valid @RequestBody RefreshRequestDto dto) {
    authService.logout(dto);
  }
}

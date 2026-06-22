package com.proyecto.GestionVeterinaria.controlers;

import com.proyecto.GestionVeterinaria.dto.cliente.ClienteResponseDto;
import com.proyecto.GestionVeterinaria.dto.cliente.ClienteUpdateDto;
import com.proyecto.GestionVeterinaria.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

  private final ClienteService clienteService;

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public List<ClienteResponseDto> findAll() {
    return clienteService.findAll();
  }

  @GetMapping("/me")
  @PreAuthorize("hasRole('CLIENTE')")
  public ClienteResponseDto getMyProfile(Authentication authentication) {
    return clienteService.findByUsername(authentication.getName());
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or @clienteSecurityService.isOwner(#id, authentication.name)")
  public ClienteResponseDto findById(@PathVariable Long id) {
    return clienteService.findById(id);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or @clienteSecurityService.isOwner(#id, authentication.name)")
  public ClienteResponseDto update(@PathVariable Long id,
      @Valid @RequestBody ClienteUpdateDto dto) {
    return clienteService.update(id, dto);
  }
}

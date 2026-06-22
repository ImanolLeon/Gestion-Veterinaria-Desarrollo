package com.proyecto.GestionVeterinaria.controlers;

import com.proyecto.GestionVeterinaria.dto.cliente.ClienteResponseDto;
import com.proyecto.GestionVeterinaria.dto.cliente.ClienteUpdateDto;
import com.proyecto.GestionVeterinaria.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Asegura que Angular pueda consumir el endpoint sin bloqueos de CORS
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ClienteResponseDto> findAll() {
        return clienteService.findAll();
    }

    // NUEVO ENDPOINT: Resuelve la ruta exacta /api/clientes/perfil/{username} que Angular necesita
    @GetMapping("/perfil/{username}")
    @PreAuthorize("hasRole('ADMIN') or #username == authentication.name")
    public ClienteResponseDto findByUsername(@PathVariable String username) {
        return clienteService.findByUsername(username);
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
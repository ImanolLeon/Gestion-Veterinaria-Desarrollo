package com.proyecto.GestionVeterinaria.service;

import com.proyecto.GestionVeterinaria.dto.mascota.MascotaRequestDto;
import com.proyecto.GestionVeterinaria.dto.mascota.MascotaResponseDto;
import com.proyecto.GestionVeterinaria.persistence.entity.Cliente;
import com.proyecto.GestionVeterinaria.persistence.entity.Mascota;
import com.proyecto.GestionVeterinaria.persistence.entity.Usuario;
import com.proyecto.GestionVeterinaria.persistence.enumerates.Sexo;
import com.proyecto.GestionVeterinaria.repository.ClienteRepository;
import com.proyecto.GestionVeterinaria.repository.MascotaRepository;
import com.proyecto.GestionVeterinaria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MascotaService {

  private final MascotaRepository mascotaRepository;
  private final ClienteRepository clienteRepository;
  private final UsuarioRepository usuarioRepository;

  public List<MascotaResponseDto> findByClienteId(Long clienteId) {
    clienteRepository.findById(clienteId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
    return mascotaRepository.findByClienteIdAndActivoTrue(clienteId).stream()
        .map(this::toDto)
        .toList();
  }

  public MascotaResponseDto findById(Long id) {
    return mascotaRepository.findByIdAndActivoTrue(id)
        .map(this::toDto)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mascota no encontrada"));
  }

  public MascotaResponseDto create(MascotaRequestDto dto, String username, boolean isAdmin) {
    Long clienteId = dto.clienteId();
    if (!isAdmin) {
      Usuario usuario = usuarioRepository.findByUsername(username)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
      clienteId = clienteRepository.findByUsuarioId(usuario.getId())
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil de cliente no encontrado"))
          .getId();
    }

    Cliente cliente = clienteRepository.findById(clienteId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));

    Mascota mascota = Mascota.builder()
        .nombre(dto.nombre())
        .especie(dto.especie())
        .raza(dto.raza())
        .fechaNacimiento(dto.fechaNacimiento())
        .peso_kg(dto.pesoKg())
        .activo(true)
        .sexo(dto.sexo() != null ? dto.sexo() : Sexo.DESCONOCIDO)
        .esterilizado(dto.esterilizado() != null && dto.esterilizado())
        .cliente(cliente)
        .build();

    return toDto(mascotaRepository.save(mascota));
  }

  public MascotaResponseDto update(Long id, MascotaRequestDto dto) {
    Mascota mascota = mascotaRepository.findByIdAndActivoTrue(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mascota no encontrada"));

    mascota.setNombre(dto.nombre());
    mascota.setEspecie(dto.especie());
    mascota.setRaza(dto.raza());
    mascota.setFechaNacimiento(dto.fechaNacimiento());
    mascota.setPeso_kg(dto.pesoKg());
    mascota.setSexo(dto.sexo() != null ? dto.sexo() : Sexo.DESCONOCIDO);
    mascota.setEsterilizado(dto.esterilizado() != null && dto.esterilizado());

    return toDto(mascotaRepository.save(mascota));
  }

  public void delete(Long id) {
    Mascota mascota = mascotaRepository.findByIdAndActivoTrue(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mascota no encontrada"));
    mascota.setActivo(false);
    mascotaRepository.save(mascota);
  }

  public MascotaResponseDto toDto(Mascota m) {
    return new MascotaResponseDto(
        m.getId(),
        m.getNombre(),
        m.getEspecie(),
        m.getRaza(),
        m.getFechaNacimiento(),
        m.getPeso_kg(),
        m.getCliente() != null ? m.getCliente().getId() : null,
        m.getCliente() != null ? m.getCliente().getNombres() + " " + m.getCliente().getApellidos() : null,
        m.getSexo(),
        m.isEsterilizado());
  }
}

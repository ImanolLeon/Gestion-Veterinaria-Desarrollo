package com.proyecto.GestionVeterinaria.repository;

import com.proyecto.GestionVeterinaria.persistence.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
  Optional<Cliente> findByUsuarioId(Long usuarioId);

  Optional<Cliente> findByDni(String dni);

  boolean existsByDni(String dni);
  Optional<Cliente> findByUsuarioUsername(String username);
}

package com.proyecto.GestionVeterinaria.persistence.entity;

import com.proyecto.GestionVeterinaria.persistence.enumerates.Permisos;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class PermisosEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private Permisos permisos;

    @ManyToMany(mappedBy = "permisos")
    Set<RolesEntity> roles = new HashSet<>();
}

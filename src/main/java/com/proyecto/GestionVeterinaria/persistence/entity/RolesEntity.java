package com.proyecto.GestionVeterinaria.persistence.entity;

import com.proyecto.GestionVeterinaria.persistence.enumerates.Rol;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class RolesEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    @ManyToMany(mappedBy = "roles")
    Set<Usuario> usuarios = new HashSet<>();

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
            @JoinTable(
                    name = "rol_permission",
                    joinColumns = @JoinColumn(name = "rol_id"),
                    inverseJoinColumns = @JoinColumn(name = "permiso_id")
            )

    Set<PermisosEntity> permisos = new HashSet<>();
}

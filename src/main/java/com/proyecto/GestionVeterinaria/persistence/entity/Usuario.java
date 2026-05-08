package com.proyecto.GestionVeterinaria.persistence.entity;

import com.proyecto.GestionVeterinaria.persistence.enumerates.Rol;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
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
@Table
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String password;

    @Column(nullable = false,unique = true)
    private String username;

    private LocalDate dateRegister;

    private boolean activo;

    private boolean  isAccountNonExpired;

    private boolean  isAccountNonLocked;

    private boolean  isCredentialsNonExpired;

    private boolean  isEnabled;

    @OneToOne(mappedBy = "usuario",cascade = CascadeType.ALL)
    private Cliente cliente;

    @OneToOne(mappedBy = "usuario",cascade = CascadeType.ALL)
    private Veterinario veterinario;

    @ManyToMany(cascade = CascadeType.ALL,fetch = FetchType.EAGER)
            @JoinTable(name = "usuario_rol",
                    joinColumns = @JoinColumn(name = "usuario_id"),
                    inverseJoinColumns = @JoinColumn(name = "rol_id")
            )
    Set<RolesEntity> roles = new HashSet<>();

}

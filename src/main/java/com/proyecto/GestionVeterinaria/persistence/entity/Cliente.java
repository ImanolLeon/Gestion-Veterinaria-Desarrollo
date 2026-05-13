package com.proyecto.GestionVeterinaria.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombres;

    private String apellidos;

    @Column(length = 8, nullable = false)
    private String dni;

    private String telefono;

    private String direccion;

    @OneToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<Mascota> mascotas;
}

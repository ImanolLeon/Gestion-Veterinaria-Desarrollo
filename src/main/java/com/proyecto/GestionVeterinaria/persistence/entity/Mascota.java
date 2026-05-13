package com.proyecto.GestionVeterinaria.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table
public class Mascota {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String especie;

    private String raza;

    private LocalDate fechaNacimiento;

    private double peso_kg;

    private boolean activo;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @OneToMany(mappedBy = "mascota", cascade = CascadeType.ALL)
    private List<HistorialClinico> historialClinicos;

    @OneToMany(mappedBy = "mascota", cascade = CascadeType.ALL)
    private List<Cita> citas;

}

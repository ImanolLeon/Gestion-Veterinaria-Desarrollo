package com.proyecto.GestionVeterinaria.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table
public class HistorialClinico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String diagnostico;

    private String tratamiento;

    private String observaciones;

    private LocalDate fecha;

    private LocalDateTime creadoEn;

    private Double peso;

    @ManyToOne
    private Mascota mascota;

    @OneToOne()
    @JoinColumn(name = "cita_id")
    private Cita cita;

}

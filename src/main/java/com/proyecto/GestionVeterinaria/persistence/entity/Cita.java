package com.proyecto.GestionVeterinaria.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table
public class Cita {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    private LocalDate fecha;

    private LocalTime hora;

    private String observaciones;

    @OneToOne(mappedBy = "cita", cascade = CascadeType.ALL)
    private HistorialClinico historialClinico;
    
    @ManyToOne()
    @JoinColumn(name="mascota_id")
    private Mascota mascota;
    
    @ManyToOne()
    @JoinColumn(name = "veterinario_id")
    private Veterinario veterinario;


    @ManyToOne()
    @JoinColumn(name = "servicio_id")
    private Servicio servicio;


}

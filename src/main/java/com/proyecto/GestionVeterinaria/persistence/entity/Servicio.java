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
public class Servicio {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private double duracionMin;

    private double precio;

    private boolean activo;
    
    @OneToMany(mappedBy = "servicio", cascade = CascadeType.ALL)
    List<Cita> citas = new ArrayList<>();

}

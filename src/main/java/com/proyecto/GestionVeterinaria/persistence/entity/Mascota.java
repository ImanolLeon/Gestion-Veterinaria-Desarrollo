package com.proyecto.GestionVeterinaria.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
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
    private Long id ;

    private String nombre;

    private String especie;

    private String raza;

    private Date fechaNacimiento;

    private double peso_kg;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @OneToMany(mappedBy = "mascota",cascade = CascadeType.ALL)
    List<HistorialClinico> historialClinicos = new ArrayList<>();
    
    @OneToMany(mappedBy = "mascota", cascade = CascadeType.ALL)
    List<Cita> citas= new ArrayList<>();
    
}

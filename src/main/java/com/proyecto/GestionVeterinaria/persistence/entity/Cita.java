package com.proyecto.GestionVeterinaria.persistence.entity;

import com.proyecto.GestionVeterinaria.persistence.enumerates.CanceladoPor;
import com.proyecto.GestionVeterinaria.persistence.enumerates.Estado;
import com.proyecto.GestionVeterinaria.persistence.enumerates.MetodoPago;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    private Estado estado;

    private String notas;

    @Enumerated(EnumType.STRING)
    private CanceladoPor canceladoPor;

    private LocalDateTime fechaCancelacion;

    @Builder.Default
    private boolean pagado = false;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;

    private Double montoPago;

    private LocalDateTime fechaPago;

    @Builder.Default
    private boolean recordatorioEnviado = false;

    @OneToOne(mappedBy = "cita", cascade = CascadeType.ALL)
    private HistorialClinico historialClinico;

    @ManyToOne
    @JoinColumn(name = "mascota_id")
    private Mascota mascota;

    @ManyToOne
    @JoinColumn(name = "veterinario_id")
    private Veterinario veterinario;

    @ManyToOne
    @JoinColumn(name = "servicio_id")
    private Servicio servicio;
}

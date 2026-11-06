package com.huertos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "plantas_medicinales")
@Getter
@Setter
@NoArgsConstructor
public class PlantaMedicinal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreComun;

    private String nombreKichwa;

    private String nombreCientifico;

    private String familiaBotanica;

    private String regionDistribucion;

    private String altitudMsnm;

    @Enumerated(EnumType.STRING)
    private CicloCultivo cicloCultivo;

    @Enumerated(EnumType.STRING)
    private CualidadTermica cualidadTermica;

    @Column(columnDefinition = "TEXT")
    private String usosMedicinales;

    @Column(columnDefinition = "TEXT")
    private String indicacionesDosificacion;

    @Column(columnDefinition = "TEXT")
    private String contraindicaciones;

    private String fotoUrl;

    private LocalDateTime creadoEn;
}

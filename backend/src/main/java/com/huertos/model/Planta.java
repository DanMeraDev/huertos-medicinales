package com.huertos.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "plantas")
@Getter
@Setter
@NoArgsConstructor
public class Planta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreComun;

    private String nombreKichwa;

    private String nombreCientifico;

    private String familiaBotanica;

    @Enumerated(EnumType.STRING)
    private CualidadTermica cualidadTermica;

    @Enumerated(EnumType.STRING)
    private CicloCultivo cicloCultivo;

    @Enumerated(EnumType.STRING)
    private OrigenPlanta origen;

    private Integer altitudMinMsnm;

    private Integer altitudMaxMsnm;

    private String regionesDistribucion;

    @Column(columnDefinition = "TEXT")
    private String usosMedicinales;

    @Column(columnDefinition = "TEXT")
    private String preparacionDosificacion;

    @Column(columnDefinition = "TEXT")
    private String contraindicaciones;

    /** Ruta relativa dentro de la carpeta de fotos (por ejemplo, plantas/12.jpg). */
    private String fotografiaUrl;

    @ElementCollection
    @BatchSize(size = 100)
    @CollectionTable(name = "planta_categorias", joinColumns = @JoinColumn(name = "planta_id"))
    @Column(name = "categoria")
    @Enumerated(EnumType.STRING)
    private Set<CategoriaUso> categorias = new HashSet<>();

    private boolean activa = true;

    private String motivoBaja;

    private LocalDateTime fechaBaja;

    @ManyToOne(fetch = FetchType.LAZY)
    private Usuario dadoDeBajaPor;

    @ManyToOne(fetch = FetchType.LAZY)
    private Usuario actualizadoPor;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    private LocalDateTime actualizadoEn;
}

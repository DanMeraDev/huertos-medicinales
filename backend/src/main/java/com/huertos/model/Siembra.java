package com.huertos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Siembra en un subhuerto a partir de un lote de bodega (HR-CU01 incluye BO-CU03).
 * La planta y el tipo de material se obtienen del lote; la señalética, de la cualidad térmica de la planta.
 */
@Entity
@Table(name = "siembras")
@Getter
@Setter
@NoArgsConstructor
public class Siembra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Subhuerto subhuerto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private BodegaMaterialVegetal bodegaMaterial;

    private BigDecimal cantidadSembrada;

    private LocalDate fechaSiembra;

    @Enumerated(EnumType.STRING)
    private FaseLunar faseLunarSiembra;

    private boolean tuvoAlertaAltitud;

    @Enumerated(EnumType.STRING)
    private EstadoCultivo estadoCultivo = EstadoCultivo.SEMBRADO;

    @Enumerated(EnumType.STRING)
    private CausaFracaso causaPerdida;

    private String observacionPerdida;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    private LocalDateTime actualizadoEn;
}

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
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/** Baja de una cantidad de un lote de bodega, con su causa (BO-CU09). */
@Entity
@Table(name = "bajas_material_vegetal")
@Getter
@Setter
@NoArgsConstructor
public class BajaMaterialVegetal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private BodegaMaterialVegetal bodegaMaterial;

    private BigDecimal cantidadBaja;

    @Enumerated(EnumType.STRING)
    private CausaFracaso causa;

    private String observacion;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime fechaBaja;
}

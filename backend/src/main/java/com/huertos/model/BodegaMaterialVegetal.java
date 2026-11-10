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

/** Lote de material vegetal de la bodega de una comunidad: cada ingreso (BO-CU01) es un lote. */
@Entity
@Table(name = "bodega_material_vegetal")
@Getter
@Setter
@NoArgsConstructor
public class BodegaMaterialVegetal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Comunidad comunidad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Planta planta;

    @Enumerated(EnumType.STRING)
    private TipoMaterialVegetal tipoMaterial;

    private BigDecimal cantidadDisponible;

    @Enumerated(EnumType.STRING)
    private UnidadMedida unidadMedida = UnidadMedida.UNIDADES;

    private String loteOrigen;

    private LocalDate fechaIngreso;

    private boolean activo = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    private LocalDateTime actualizadoEn;
}

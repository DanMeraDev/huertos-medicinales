package com.huertos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Huerto real de una comunidad. Se crea al aprobar la solicitud (US-CU06) con el nombre de la comunidad;
 * el área y los datos operativos los completa el Responsable en HR-CU03.
 */
@Entity
@Table(name = "huertos")
@Getter
@Setter
@NoArgsConstructor
public class Huerto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private Comunidad comunidad;

    private String nombreHuerto;

    private BigDecimal latitud;

    private BigDecimal longitud;

    private int altitudMsnm;

    private String orientacion;

    private BigDecimal areaTotalM2;

    @Enumerated(EnumType.STRING)
    private TipoHuerto tipoHuerto = TipoHuerto.BASICO;

    @Enumerated(EnumType.STRING)
    private DisenoPrincipal disenoPrincipal;

    @Enumerated(EnumType.STRING)
    private TipoSuelo tipoSuelo;

    @Enumerated(EnumType.STRING)
    private FuenteAgua fuenteAgua;

    @Enumerated(EnumType.STRING)
    private TipoAbono tipoAbono;

    @Enumerated(EnumType.STRING)
    private TipoCerca tipoCerca;

    private String detalleCerca;

    private int puntosHuerto;

    private boolean activo = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime creadoEn;
}

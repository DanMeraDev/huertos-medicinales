package com.huertos.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Actividad grande de un saber ancestral que el MSP revisa con su evidencia (SA-CU04 a SA-CU06).
 * Al aprobarse suma puntos a la comunidad, al huerto y a cada participante por sus horas (GM-CU01).
 */
@Entity
@Table(name = "actividades_ancestrales_msp")
@Getter
@Setter
@NoArgsConstructor
public class ActividadAncestralMsp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Comunidad comunidad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Huerto huerto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private SaberAncestral saberAncestral;

    private LocalDate fechaRealizacion;

    @Enumerated(EnumType.STRING)
    private FaseLunar faseLunarEjecucion;

    @Column(columnDefinition = "TEXT")
    private String descripcionBitacora;

    private String evidenciaFotoUrl;

    private String evidenciaVideoUrl;

    private int numeroIntento = 1;

    @Enumerated(EnumType.STRING)
    private EstadoVerificacion estadoVerificacion = EstadoVerificacion.PENDIENTE;

    private String observacionMsp;

    private int puntosAcreditarComunidad;

    @ManyToOne(fetch = FetchType.LAZY)
    private Usuario verificadoPor;

    private LocalDateTime fechaVerificacion;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime creadoEn;

    @OneToMany(mappedBy = "actividadMsp", cascade = CascadeType.PERSIST)
    private List<ActividadMspParticipante> participantes = new ArrayList<>();
}

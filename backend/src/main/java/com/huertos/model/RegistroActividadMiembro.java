package com.huertos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "registro_actividades_miembro")
@Getter
@Setter
@NoArgsConstructor
public class RegistroActividadMiembro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Comunidad comunidad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Miembro miembro;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ActividadComunidad actividadComunidad;

    private LocalDate fechaActividad;

    private int horasTrabajadas;

    private int puntosOtorgados;

    private String observacion;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime creadoEn;
}

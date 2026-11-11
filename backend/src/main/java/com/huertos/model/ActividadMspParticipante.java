package com.huertos.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "actividad_msp_participantes")
@Getter
@Setter
@NoArgsConstructor
public class ActividadMspParticipante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ActividadAncestralMsp actividadMsp;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Miembro miembro;

    private int horasTrabajadas;

    private int puntosAcreditados;
}

package com.huertos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "saberes_ancestrales")
@Getter
@Setter
@NoArgsConstructor
public class SaberAncestral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int ordenPaso;

    private String codigoHito;

    private String titulo;

    private String tituloKichwa;

    @Enumerated(EnumType.STRING)
    private CategoriaSaber categoria;

    @Enumerated(EnumType.STRING)
    private FaseLunar faseLunarIdeal;

    private String horarioEnergetico;

    @Column(columnDefinition = "TEXT")
    private String contenidoEducativo;

    @Column(columnDefinition = "TEXT")
    private String ingredientesHerramientas;

    private int puntosBaseComunidad = 20;

    private String nombreInsigniaOficial;
}

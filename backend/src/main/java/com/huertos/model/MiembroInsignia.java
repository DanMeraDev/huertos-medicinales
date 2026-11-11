package com.huertos.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Insignia ganada por un miembro: oficial del MSP (con saberAncestral) o talento de su comunidad (con talentoComunidad).
 */
@Entity
@Table(name = "miembro_insignias")
@Getter
@Setter
@NoArgsConstructor
public class MiembroInsignia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Miembro miembro;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Comunidad comunidad;

    @Enumerated(EnumType.STRING)
    private TipoOrigenInsignia tipoOrigen;

    private String nombreInsignia;

    private String descripcion;

    private String iconoCodigo = "medalla-kinti";

    @ManyToOne(fetch = FetchType.LAZY)
    private SaberAncestral saberAncestral;

    @ManyToOne(fetch = FetchType.LAZY)
    private TalentoComunidad talentoComunidad;

    private LocalDate fechaObtencion;
}

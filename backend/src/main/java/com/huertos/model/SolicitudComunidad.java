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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "solicitudes_comunidad")
@Getter
@Setter
@NoArgsConstructor
public class SolicitudComunidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreComunidad;

    @Enumerated(EnumType.STRING)
    private TipoOrganizacion tipoOrganizacion;

    private String provincia;

    private String parroquia;

    private String direccion;

    private BigDecimal latitud;

    private BigDecimal longitud;

    private int altitudMsnm;

    @Enumerated(EnumType.STRING)
    private DisenoPrincipal disenoPrincipal;

    private String subtiposSolicitados;

    private String cedulaSolicitante;

    private String nombreSolicitante;

    private String correoSolicitante;

    private String celularSolicitante;

    @Enumerated(EnumType.STRING)
    private Genero generoSolicitante;

    @Enumerated(EnumType.STRING)
    private EstadoSolicitud estado;

    private String motivoRechazo;

    @ManyToOne(fetch = FetchType.LAZY)
    private Usuario revisadoPor;

    private LocalDateTime fechaRevision;

    private LocalDateTime creadoEn;
}

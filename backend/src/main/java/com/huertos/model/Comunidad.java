package com.huertos.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "comunidades")
@Getter
@Setter
@NoArgsConstructor
public class Comunidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Enumerated(EnumType.STRING)
    private TipoOrganizacion tipoOrganizacion;

    private String provincia;

    private String parroquia;

    private String direccion;

    private String telefonoContacto;

    private String correoContacto;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private Usuario responsable;

    private int puntosComunidad;

    private boolean activa;

    private LocalDateTime creadoEn;
}

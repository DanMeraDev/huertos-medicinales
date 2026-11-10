package com.huertos.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreCompleto;

    private String identificacion;

    @Enumerated(EnumType.STRING)
    private Genero genero;

    private String email;

    private String telefono;

    private String contrasena;

    private boolean requiereCambioClave;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    private boolean activo;

    private LocalDateTime creadoEn;
}

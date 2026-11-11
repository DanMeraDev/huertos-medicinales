package com.huertos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "calendario_lunar")
@Getter
@Setter
@NoArgsConstructor
public class CalendarioLunar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private FaseLunar faseLunar;

    private String nombreKichwa;

    private String nombreEspanol;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    @Column(columnDefinition = "TEXT")
    private String practicasRecomendadas;

    private String consejoKinti;
}

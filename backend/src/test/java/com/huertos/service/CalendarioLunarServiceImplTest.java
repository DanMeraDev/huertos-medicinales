package com.huertos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huertos.dto.CalendarioLunarResponse;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.mapper.CalendarioLunarMapper;
import com.huertos.model.CalendarioLunar;
import com.huertos.model.FaseLunar;
import com.huertos.repository.CalendarioLunarRepository;
import com.huertos.service.impl.CalendarioLunarServiceImpl;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CalendarioLunarServiceImplTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 11, 11);

    @Mock
    private CalendarioLunarRepository calendarioLunarRepository;

    @Mock
    private CalendarioLunarMapper calendarioLunarMapper;

    @InjectMocks
    private CalendarioLunarServiceImpl calendarioLunarService;

    private CalendarioLunar fase;

    @BeforeEach
    void prepararFase() {
        fase = new CalendarioLunar();
        fase.setId(7L);
        fase.setFaseLunar(FaseLunar.WANUK_KILLA_MENGUANTE);
        fase.setNombreKichwa("Wañuk Killa");
        fase.setNombreEspanol("Luna Menguante");
        fase.setFechaInicio(LocalDate.of(2026, 11, 9));
        fase.setFechaFin(LocalDate.of(2026, 11, 15));
        fase.setPracticasRecomendadas("La savia desciende a la raíz.");
        fase.setConsejoKinti("Kinti habla en la noche.");
    }

    @Test
    void devuelveLaFaseQueVigenciaEnLaFechaConsultada() {
        when(calendarioLunarRepository.findByFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(FECHA, FECHA))
                .thenReturn(Optional.of(fase));
        when(calendarioLunarMapper.toCalendarioLunarResponse(fase)).thenReturn(respuesta());

        CalendarioLunarResponse resultado = calendarioLunarService.obtenerFaseActual(FECHA);

        assertThat(resultado.id()).isEqualTo(7L);
        assertThat(resultado.faseLunar()).isEqualTo(FaseLunar.WANUK_KILLA_MENGUANTE);
        assertThat(resultado.nombreKichwa()).isEqualTo("Wañuk Killa");
        assertThat(resultado.nombreEspanol()).isEqualTo("Luna Menguante");
        assertThat(resultado.fechaInicio()).isEqualTo(LocalDate.of(2026, 11, 9));
        assertThat(resultado.fechaFin()).isEqualTo(LocalDate.of(2026, 11, 15));
        assertThat(resultado.practicasRecomendadas()).isEqualTo("La savia desciende a la raíz.");
        assertThat(resultado.consejoKinti()).isEqualTo("Kinti habla en la noche.");
    }

    @Test
    void buscaElPeriodoQueContieneLaFechaConsultada() {
        when(calendarioLunarRepository.findByFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(FECHA, FECHA))
                .thenReturn(Optional.of(fase));
        when(calendarioLunarMapper.toCalendarioLunarResponse(fase)).thenReturn(respuesta());

        calendarioLunarService.obtenerFaseActual(FECHA);

        verify(calendarioLunarRepository).findByFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(FECHA, FECHA);
    }

    @Test
    void fallaCuandoNoHayFaseConfiguradaParaLaFecha() {
        when(calendarioLunarRepository.findByFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(FECHA, FECHA))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> calendarioLunarService.obtenerFaseActual(FECHA))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("No hay datos lunares disponibles para la fecha seleccionada");
    }

    private CalendarioLunarResponse respuesta() {
        return new CalendarioLunarResponse(
                7L, FaseLunar.WANUK_KILLA_MENGUANTE, "Wañuk Killa", "Luna Menguante",
                LocalDate.of(2026, 11, 9), LocalDate.of(2026, 11, 15),
                "La savia desciende a la raíz.", "Kinti habla en la noche.");
    }
}

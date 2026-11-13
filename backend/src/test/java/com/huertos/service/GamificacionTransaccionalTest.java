package com.huertos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huertos.model.ActividadAncestralMsp;
import com.huertos.model.ActividadMspParticipante;
import com.huertos.model.Comunidad;
import com.huertos.model.EstadoVerificacion;
import com.huertos.model.Huerto;
import com.huertos.model.Miembro;
import com.huertos.model.NivelMiembro;
import com.huertos.model.SaberAncestral;
import com.huertos.repository.ActividadAncestralMspRepository;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.HuertoRepository;
import com.huertos.repository.MiembroInsigniaRepository;
import com.huertos.repository.MiembroRepository;
import com.huertos.repository.RegistroActividadMiembroRepository;
import com.huertos.repository.TalentoComunidadRepository;
import com.huertos.service.impl.GamificacionServiceImpl;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = GamificacionTransaccionalTest.Config.class)
class GamificacionTransaccionalTest {

    @Configuration
    @EnableTransactionManagement
    static class Config {

        @Bean
        public PlatformTransactionManager transactionManager() {
            return mock(PlatformTransactionManager.class);
        }

        @Bean
        public ComunidadRepository comunidadRepository() {
            return mock(ComunidadRepository.class);
        }

        @Bean
        public HuertoRepository huertoRepository() {
            return mock(HuertoRepository.class);
        }

        @Bean
        public MiembroRepository miembroRepository() {
            return mock(MiembroRepository.class);
        }

        @Bean
        public ActividadAncestralMspRepository actividadMspRepository() {
            return mock(ActividadAncestralMspRepository.class);
        }

        @Bean
        public MiembroInsigniaRepository miembroInsigniaRepository() {
            return mock(MiembroInsigniaRepository.class);
        }

        @Bean
        public RegistroActividadMiembroRepository registroActividadMiembroRepository() {
            return mock(RegistroActividadMiembroRepository.class);
        }

        @Bean
        public TalentoComunidadRepository talentoComunidadRepository() {
            return mock(TalentoComunidadRepository.class);
        }

        @Bean
        public GamificacionService gamificacionService(
                ComunidadRepository comunidadRepository,
                HuertoRepository huertoRepository,
                MiembroRepository miembroRepository,
                ActividadAncestralMspRepository actividadMspRepository,
                MiembroInsigniaRepository miembroInsigniaRepository,
                RegistroActividadMiembroRepository registroActividadMiembroRepository,
                TalentoComunidadRepository talentoComunidadRepository) {
            return new GamificacionServiceImpl(
                    comunidadRepository,
                    huertoRepository,
                    miembroRepository,
                    actividadMspRepository,
                    miembroInsigniaRepository,
                    registroActividadMiembroRepository,
                    talentoComunidadRepository);
        }
    }

    @Autowired
    private GamificacionService gamificacionService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ComunidadRepository comunidadRepository;

    @Autowired
    private HuertoRepository huertoRepository;

    @Autowired
    private MiembroRepository miembroRepository;

    @Autowired
    private ActividadAncestralMspRepository actividadMspRepository;

    @Autowired
    private MiembroInsigniaRepository miembroInsigniaRepository;

    @BeforeEach
    void reiniciarMocks() {
        reset(transactionManager, comunidadRepository, huertoRepository, miembroRepository, actividadMspRepository, miembroInsigniaRepository);
        TransactionStatus status = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any(TransactionDefinition.class))).thenReturn(status);
    }

    @Test
    @DisplayName("El método procesarPuntosActividadMsp posee la anotación @Transactional")
    void metodoPoseeAnotacionTransactional() throws NoSuchMethodException {
        Method metodo = GamificacionServiceImpl.class.getMethod("procesarPuntosActividadMsp", Long.class);
        assertThat(metodo.isAnnotationPresent(Transactional.class)).isTrue();
    }

    @Test
    @DisplayName("Si falla la actualización de un miembro, el interceptor transaccional ejecuta rollback y no altera la comunidad")
    void siFallaActualizacionDeMiembroEjecutaRollback() {
        Comunidad comunidad = new Comunidad();
        comunidad.setId(10L);
        comunidad.setPuntosComunidad(100);

        Huerto huerto = new Huerto();
        huerto.setId(20L);
        huerto.setPuntosHuerto(50);
        huerto.setComunidad(comunidad);

        SaberAncestral saber = new SaberAncestral();
        saber.setId(30L);
        saber.setPuntosBaseComunidad(20);
        saber.setNombreInsigniaOficial("Sabio de la Tierra");

        Miembro miembro = new Miembro();
        miembro.setId(40L);
        miembro.setNivel(NivelMiembro.APRENDIZ);
        miembro.setPuntosTotales(10);
        miembro.setActivo(true);

        ActividadAncestralMsp actividad = new ActividadAncestralMsp();
        actividad.setId(1L);
        actividad.setComunidad(comunidad);
        actividad.setHuerto(huerto);
        actividad.setSaberAncestral(saber);
        actividad.setNumeroIntento(1);
        actividad.setEstadoVerificacion(EstadoVerificacion.APROBADA);

        ActividadMspParticipante participante = new ActividadMspParticipante();
        participante.setId(50L);
        participante.setActividadMsp(actividad);
        participante.setMiembro(miembro);
        participante.setHorasTrabajadas(4);
        actividad.setParticipantes(List.of(participante));

        when(actividadMspRepository.findById(1L)).thenReturn(Optional.of(actividad));
        when(miembroRepository.findById(40L)).thenReturn(Optional.of(miembro));

        // Simulamos un fallo a nivel de base de datos al guardar el miembro
        when(miembroRepository.save(any(Miembro.class)))
                .thenThrow(new DataIntegrityViolationException("Error de integridad de datos en miembros"));

        assertThatThrownBy(() -> gamificacionService.procesarPuntosActividadMsp(1L))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("Error de integridad de datos");

        // Verificamos que el TransactionManager haya recibido la orden de ROLLBACK
        verify(transactionManager).rollback(any(TransactionStatus.class));
        // Y que NUNCA se haya hecho commit de la transacción
        verify(transactionManager, never()).commit(any(TransactionStatus.class));
    }

    @Test
    @DisplayName("Cuando no hay errores, el interceptor transaccional ejecuta commit exitoso")
    void cuandoNoHayErroresEjecutaCommit() {
        Comunidad comunidad = new Comunidad();
        comunidad.setId(10L);
        comunidad.setPuntosComunidad(100);

        Huerto huerto = new Huerto();
        huerto.setId(20L);
        huerto.setPuntosHuerto(50);
        huerto.setComunidad(comunidad);

        SaberAncestral saber = new SaberAncestral();
        saber.setId(30L);
        saber.setPuntosBaseComunidad(20);

        ActividadAncestralMsp actividad = new ActividadAncestralMsp();
        actividad.setId(2L);
        actividad.setComunidad(comunidad);
        actividad.setHuerto(huerto);
        actividad.setSaberAncestral(saber);
        actividad.setNumeroIntento(1);
        actividad.setEstadoVerificacion(EstadoVerificacion.APROBADA);
        actividad.setParticipantes(List.of());

        when(actividadMspRepository.findById(2L)).thenReturn(Optional.of(actividad));

        gamificacionService.procesarPuntosActividadMsp(2L);

        // Verificamos que se ejecutó commit
        verify(transactionManager).commit(any(TransactionStatus.class));
        verify(transactionManager, never()).rollback(any(TransactionStatus.class));
    }
}

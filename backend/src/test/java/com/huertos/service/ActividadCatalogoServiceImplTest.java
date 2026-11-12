package com.huertos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huertos.dto.ActividadCatalogoResponse;
import com.huertos.dto.CrearActividadCatalogoRequest;
import com.huertos.exception.ConflictoException;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.model.ActividadComunidad;
import com.huertos.model.CategoriaActividad;
import com.huertos.model.Comunidad;
import com.huertos.repository.ActividadComunidadRepository;
import com.huertos.repository.ComunidadRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActividadCatalogoServiceImplTest {

    @Mock
    private ActividadComunidadRepository actividadRepository;

    @Mock
    private ComunidadRepository comunidadRepository;

    @InjectMocks
    private ActividadCatalogoServiceImpl service;

    private Comunidad comunidadActiva;

    @BeforeEach
    void setUp() {
        comunidadActiva = new Comunidad();
        comunidadActiva.setId(10L);
        comunidadActiva.setNombre("Comunidad Inti");
        comunidadActiva.setActiva(true);
    }

    @Test
    void crearActividad_asignaUnPuntoPorHoraYActivaTrue() {
        var request = new CrearActividadCatalogoRequest("  Riego matutino  ", CategoriaActividad.RIEGO);

        when(comunidadRepository.findByResponsableCorreo("resp@comunidad.ec"))
                .thenReturn(Optional.of(comunidadActiva));
        when(actividadRepository.existsByComunidadIdAndNombreIgnoreCase(10L, "Riego matutino"))
                .thenReturn(false);
        when(actividadRepository.save(any(ActividadComunidad.class))).thenAnswer(inv -> {
            ActividadComunidad entidad = inv.getArgument(0);
            entidad.setId(55L);
            return entidad;
        });

        ActividadCatalogoResponse respuesta = service.crear("resp@comunidad.ec", request);

        assertNotNull(respuesta);
        assertEquals(55L, respuesta.id());
        assertEquals("Riego matutino", respuesta.nombre());
        assertEquals(CategoriaActividad.RIEGO, respuesta.categoria());
        assertEquals(1, respuesta.puntosPorHora());
        assertTrue(respuesta.activa());
        verify(actividadRepository).save(any(ActividadComunidad.class));
    }

    @Test
    void crearActividad_lanzaConflictoSiNombreYaExisteEnComunidad() {
        var request = new CrearActividadCatalogoRequest("Deshierbe general", CategoriaActividad.LIMPIEZA_DESHIERBE);

        when(comunidadRepository.findByResponsableCorreo("resp@comunidad.ec"))
                .thenReturn(Optional.of(comunidadActiva));
        when(actividadRepository.existsByComunidadIdAndNombreIgnoreCase(10L, "Deshierbe general"))
                .thenReturn(true);

        ConflictoException ex = assertThrows(
                ConflictoException.class,
                () -> service.crear("resp@comunidad.ec", request));

        assertEquals("Ya existe una actividad con este nombre en tu comunidad", ex.getMessage());
        verify(actividadRepository, never()).save(any());
    }

    @Test
    void listarPorResponsable_devuelveActividadesDeSuComunidad() {
        ActividadComunidad act = new ActividadComunidad();
        act.setId(1L);
        act.setComunidad(comunidadActiva);
        act.setNombre("Compostaje orgánico");
        act.setCategoria(CategoriaActividad.ABONO_COMPOST);
        act.setPuntosPorHora(1);
        act.setActiva(true);

        when(comunidadRepository.findByResponsableCorreo("resp@comunidad.ec"))
                .thenReturn(Optional.of(comunidadActiva));
        when(actividadRepository.findByComunidadIdOrderByNombreAsc(10L))
                .thenReturn(List.of(act));

        List<ActividadCatalogoResponse> lista = service.listarPorResponsable("resp@comunidad.ec");

        assertEquals(1, lista.size());
        assertEquals("Compostaje orgánico", lista.get(0).nombre());
        assertEquals(1, lista.get(0).puntosPorHora());
    }

    @Test
    void crearActividad_lanzaNoEncontradoSiUsuarioNoTieneComunidad() {
        var request = new CrearActividadCatalogoRequest("Poda", CategoriaActividad.CUIDADO_GENERAL);

        when(comunidadRepository.findByResponsableCorreo("sin@comunidad.ec"))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> service.crear("sin@comunidad.ec", request));
    }
}
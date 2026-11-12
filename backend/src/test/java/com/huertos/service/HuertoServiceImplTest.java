package com.huertos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huertos.dto.ActualizarHuertoRequest;
import com.huertos.dto.HuertoDetalleResponse;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.mapper.HuertoMapper;
import com.huertos.model.DisenoPrincipal;
import com.huertos.model.FuenteAgua;
import com.huertos.model.Huerto;
import com.huertos.model.TipoAbono;
import com.huertos.model.TipoCerca;
import com.huertos.model.TipoHuerto;
import com.huertos.model.TipoSuelo;
import com.huertos.repository.HuertoRepository;
import com.huertos.repository.SubhuertoRepository;
import com.huertos.repository.UsuarioRepository;
import com.huertos.service.impl.HuertoServiceImpl;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HuertoServiceImplTest {

    @Mock
    private HuertoRepository huertoRepository;

    @Mock
    private SubhuertoRepository subhuertoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private HuertoMapper huertoMapper;

    private HuertoService servicio;

    @BeforeEach
    void setUp() {
        servicio = new HuertoServiceImpl(huertoRepository, subhuertoRepository, usuarioRepository, huertoMapper);
    }

    @Test
    void obtenerPorCorreoRetornaDetalleDelHuerto() {
        String correo = "responsable@huertos.gob.ec";
        Huerto huerto = huerto();
        HuertoDetalleResponse respuestaEsperada = detalleResponse();

        when(usuarioRepository.findByCorreo(correo))
                .thenReturn(Optional.of(usuario(10L)));
        when(huertoRepository.findByComunidadResponsableId(10L))
                .thenReturn(Optional.of(huerto));
        when(subhuertoRepository.sumAreaActivaByHuertoId(huerto.getId()))
                .thenReturn(BigDecimal.ZERO);
        doReturn(respuestaEsperada).when(huertoMapper).toDetalleResponse(huerto, BigDecimal.ZERO);

        HuertoDetalleResponse resultado = servicio.obtenerPorCorreo(correo);

        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.areaTotalM2()).isEqualByComparingTo("150.00");
        assertThat(resultado.areaOcupadaSubhuertosM2()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void obtenerPorCorreoLanzaExcepcionCuandoNoExisteUsuario() {
        when(usuarioRepository.findByCorreo("inexistente@huertos.gob.ec"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.obtenerPorCorreo("inexistente@huertos.gob.ec"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void obtenerPorCorreoLanzaExcepcionCuandoNoExisteHuerto() {
        when(usuarioRepository.findByCorreo("responsable@huertos.gob.ec"))
                .thenReturn(Optional.of(usuario(10L)));
        when(huertoRepository.findByComunidadResponsableId(10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.obtenerPorCorreo("responsable@huertos.gob.ec"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void actualizarPorCorreoGuardaCambiosOperativos() {
        String correo = "responsable@huertos.gob.ec";
        Huerto huerto = huerto();
        ActualizarHuertoRequest request = requestValido();
        HuertoDetalleResponse respuestaEsperada = detalleResponse();

        when(usuarioRepository.findByCorreo(correo))
                .thenReturn(Optional.of(usuario(10L)));
        when(huertoRepository.findByComunidadResponsableId(10L))
                .thenReturn(Optional.of(huerto));
        when(subhuertoRepository.sumAreaActivaByHuertoId(huerto.getId()))
                .thenReturn(BigDecimal.ZERO);
        when(huertoRepository.save(any(Huerto.class))).thenAnswer(inv -> inv.getArgument(0));
        doReturn(respuestaEsperada).when(huertoMapper).toDetalleResponse(huerto, BigDecimal.ZERO);

        HuertoDetalleResponse resultado = servicio.actualizarPorCorreo(correo, request);

        assertThat(resultado.areaTotalM2()).isEqualByComparingTo("150.00");
        verify(huertoRepository).save(any(Huerto.class));
    }

    @Test
    void actualizarPorCorreoLanzaExcepcionCuandoAreaMenorQueSubhuertos() {
        String correo = "responsable@huertos.gob.ec";
        Huerto huerto = huerto();

        ActualizarHuertoRequest request = new ActualizarHuertoRequest(
                new BigDecimal("-0.1234567"),
                new BigDecimal("-78.5678901"),
                2500,
                "NORTE_SUR",
                new BigDecimal("50.00"),
                DisenoPrincipal.EN_TIERRA,
                TipoSuelo.ARCILLOSO,
                FuenteAgua.GOTEO,
                TipoAbono.COMPOST,
                TipoCerca.CERCA_VIVA,
                null
        );

        when(usuarioRepository.findByCorreo(correo))
                .thenReturn(Optional.of(usuario(10L)));
        when(huertoRepository.findByComunidadResponsableId(10L))
                .thenReturn(Optional.of(huerto));
        when(subhuertoRepository.sumAreaActivaByHuertoId(huerto.getId()))
                .thenReturn(new BigDecimal("80.00"));

        assertThatThrownBy(() -> servicio.actualizarPorCorreo(correo, request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("50")
                .hasMessageContaining("80");

        verify(huertoRepository, never()).save(any());
    }

    @Test
    void actualizarPorCorreoLanzaExcepcionCuandoNoExisteUsuario() {
        when(usuarioRepository.findByCorreo("inexistente@huertos.gob.ec"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.actualizarPorCorreo("inexistente@huertos.gob.ec", requestValido()))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    private Huerto huerto() {
        Huerto huerto = new Huerto();
        huerto.setId(1L);
        huerto.setNombreHuerto("Huerto Allpa Viva");
        huerto.setLatitud(new BigDecimal("-0.1234567"));
        huerto.setLongitud(new BigDecimal("-78.5678901"));
        huerto.setAltitudMsnm(2500);
        huerto.setAreaTotalM2(new BigDecimal("150.00"));
        huerto.setDisenoPrincipal(DisenoPrincipal.EN_TIERRA);
        huerto.setTipoHuerto(TipoHuerto.BASICO);
        huerto.setPuntosHuerto(0);
        return huerto;
    }

    private HuertoDetalleResponse detalleResponse() {
        return new HuertoDetalleResponse(
                1L,
                3L,
                "Huerto Allpa Viva",
                new BigDecimal("-0.1234567"),
                new BigDecimal("-78.5678901"),
                2500,
                "NORTE_SUR",
                new BigDecimal("150.00"),
                DisenoPrincipal.EN_TIERRA,
                TipoSuelo.ARCILLOSO,
                FuenteAgua.GOTEO,
                TipoAbono.COMPOST,
                TipoCerca.CERCA_VIVA,
                "Muro de cabuya",
                TipoHuerto.BASICO,
                0,
                BigDecimal.ZERO
        );
    }

    private ActualizarHuertoRequest requestValido() {
        return new ActualizarHuertoRequest(
                new BigDecimal("-0.1234567"),
                new BigDecimal("-78.5678901"),
                2500,
                "NORTE_SUR",
                new BigDecimal("150.00"),
                DisenoPrincipal.EN_TIERRA,
                TipoSuelo.ARCILLOSO,
                FuenteAgua.GOTEO,
                TipoAbono.COMPOST,
                TipoCerca.CERCA_VIVA,
                "Muro de cabuya y madera"
        );
    }

    private com.huertos.model.Usuario usuario(Long id) {
        com.huertos.model.Usuario usuario = new com.huertos.model.Usuario();
        usuario.setId(id);
        return usuario;
    }
}

package com.huertos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huertos.dto.MiembroDetalleResponse;
import com.huertos.dto.RegistrarMiembroRequest;
import com.huertos.exception.ConflictoException;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.mapper.MiembroMapper;
import com.huertos.model.Comunidad;
import com.huertos.model.ComunidadMiembro;
import com.huertos.model.Genero;
import com.huertos.model.Miembro;
import com.huertos.model.NivelMiembro;
import com.huertos.model.Usuario;
import com.huertos.repository.ComunidadMiembroRepository;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.MiembroRepository;
import com.huertos.repository.UsuarioRepository;
import com.huertos.service.impl.MiembroServiceImpl;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MiembroServiceImplTest {

    private static final Instant AHORA = Instant.parse("2026-11-12T10:00:00Z");
    private static final ZoneId ZONA = ZoneId.of("America/Guayaquil");

    @Mock
    private MiembroRepository miembroRepository;

    @Mock
    private ComunidadMiembroRepository comunidadMiembroRepository;

    @Mock
    private ComunidadRepository comunidadRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Spy
    private MiembroMapper miembroMapper = Mappers.getMapper(MiembroMapper.class);

    private Clock clock;
    private MiembroServiceImpl miembroService;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(AHORA, ZONA);
        miembroService = new MiembroServiceImpl(
                miembroRepository,
                comunidadMiembroRepository,
                comunidadRepository,
                usuarioRepository,
                miembroMapper,
                clock
        );
    }

    @Test
    void registraNuevoVoluntarioExitosamenteConConsentimientoLOPDP() {
        Long responsableId = 1L;
        Comunidad comunidad = new Comunidad();
        comunidad.setId(3L);
        comunidad.setNombre("Huerto El Rosal");

        RegistrarMiembroRequest request = new RegistrarMiembroRequest(
                "1710034065",
                "María Tránsito",
                "Amaguaña Alba",
                LocalDate.of(2010, 3, 25),
                Genero.FEMENINO,
                "0981234567",
                NivelMiembro.APRENDIZ,
                true
        );

        when(comunidadRepository.findByResponsableId(responsableId)).thenReturn(Optional.of(comunidad));
        when(miembroRepository.findByCedula("1710034065")).thenReturn(Optional.empty());
        when(miembroRepository.save(any(Miembro.class))).thenAnswer(invocation -> {
            Miembro m = invocation.getArgument(0);
            m.setId(15L);
            return m;
        });
        when(comunidadMiembroRepository.save(any(ComunidadMiembro.class))).thenAnswer(invocation -> {
            ComunidadMiembro cm = invocation.getArgument(0);
            cm.setId(50L);
            return cm;
        });

        MiembroDetalleResponse response = miembroService.registrarMiembroEnComunidad(responsableId, request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(15L);
        assertThat(response.comunidadId()).isEqualTo(3L);
        assertThat(response.cedula()).isEqualTo("1710034065");
        assertThat(response.nombreCompleto()).isEqualTo("María Tránsito Amaguaña Alba");
        assertThat(response.edad()).isEqualTo(16);
        assertThat(response.puntosTotales()).isZero();
        assertThat(response.nivel()).isEqualTo(NivelMiembro.APRENDIZ);
        assertThat(response.activoEnComunidad()).isTrue();
        assertThat(response.fechaIngreso()).isEqualTo(LocalDate.now(clock));

        ArgumentCaptor<Miembro> miembroCaptor = ArgumentCaptor.forClass(Miembro.class);
        verify(miembroRepository).save(miembroCaptor.capture());
        Miembro miembroGuardado = miembroCaptor.getValue();
        assertThat(miembroGuardado.isConsentimientoDatos()).isTrue();
        assertThat(miembroGuardado.getConsentimientoEn()).isEqualTo(LocalDateTime.now(clock));
        assertThat(miembroGuardado.isActivo()).isTrue();
        assertThat(miembroGuardado.getPuntosTotales()).isZero();

        ArgumentCaptor<ComunidadMiembro> cmCaptor = ArgumentCaptor.forClass(ComunidadMiembro.class);
        verify(comunidadMiembroRepository).save(cmCaptor.capture());
        ComunidadMiembro cmGuardado = cmCaptor.getValue();
        assertThat(cmGuardado.getComunidad()).isEqualTo(comunidad);
        assertThat(cmGuardado.getMiembro()).isEqualTo(miembroGuardado);
        assertThat(cmGuardado.isActivoEnComunidad()).isTrue();
        assertThat(cmGuardado.getFechaIngreso()).isEqualTo(LocalDate.now(clock));
    }

    @Test
    void reutilizaVoluntarioExistenteDeOtraComunidadSinDuplicarMiembro() {
        Long responsableId = 1L;
        Comunidad comunidad = new Comunidad();
        comunidad.setId(7L);

        Miembro miembroExistente = new Miembro();
        miembroExistente.setId(20L);
        miembroExistente.setCedula("1710034065");
        miembroExistente.setNombres("María Tránsito");
        miembroExistente.setApellidos("Amaguaña Alba");
        miembroExistente.setFechaNacimiento(LocalDate.of(2010, 3, 25));
        miembroExistente.setGenero(Genero.FEMENINO);
        miembroExistente.setTelefono("0981234567");
        miembroExistente.setNivel(NivelMiembro.OFICIAL);
        miembroExistente.setPuntosTotales(150);
        miembroExistente.setActivo(true);
        miembroExistente.setConsentimientoDatos(true);

        RegistrarMiembroRequest request = new RegistrarMiembroRequest(
                "1710034065",
                "María Tránsito",
                "Amaguaña Alba",
                LocalDate.of(2010, 3, 25),
                Genero.FEMENINO,
                "0981234567",
                NivelMiembro.APRENDIZ,
                true
        );

        when(comunidadRepository.findByResponsableId(responsableId)).thenReturn(Optional.of(comunidad));
        when(miembroRepository.findByCedula("1710034065")).thenReturn(Optional.of(miembroExistente));
        when(comunidadMiembroRepository.existsByComunidadIdAndMiembroIdAndActivoEnComunidadTrue(7L, 20L))
                .thenReturn(false);
        when(comunidadMiembroRepository.findByComunidadIdAndMiembroId(7L, 20L)).thenReturn(Optional.empty());
        when(comunidadMiembroRepository.save(any(ComunidadMiembro.class))).thenAnswer(invocation -> {
            ComunidadMiembro cm = invocation.getArgument(0);
            cm.setId(88L);
            return cm;
        });

        MiembroDetalleResponse response = miembroService.registrarMiembroEnComunidad(responsableId, request);

        assertThat(response.id()).isEqualTo(20L);
        assertThat(response.comunidadId()).isEqualTo(7L);
        assertThat(response.nivel()).isEqualTo(NivelMiembro.OFICIAL);
        assertThat(response.puntosTotales()).isEqualTo(150);
        assertThat(response.activoEnComunidad()).isTrue();

        verify(miembroRepository, never()).save(any());
        verify(comunidadMiembroRepository).save(any(ComunidadMiembro.class));
    }

    @Test
    void rechazaRegistroCuandoVoluntarioYaEstaActivoEnLaMismaComunidad() {
        Long responsableId = 1L;
        Comunidad comunidad = new Comunidad();
        comunidad.setId(3L);

        Miembro miembroExistente = new Miembro();
        miembroExistente.setId(15L);
        miembroExistente.setCedula("1710034065");

        RegistrarMiembroRequest request = new RegistrarMiembroRequest(
                "1710034065",
                "María Tránsito",
                "Amaguaña Alba",
                LocalDate.of(2010, 3, 25),
                Genero.FEMENINO,
                "0981234567",
                NivelMiembro.APRENDIZ,
                true
        );

        when(comunidadRepository.findByResponsableId(responsableId)).thenReturn(Optional.of(comunidad));
        when(miembroRepository.findByCedula("1710034065")).thenReturn(Optional.of(miembroExistente));
        when(comunidadMiembroRepository.existsByComunidadIdAndMiembroIdAndActivoEnComunidadTrue(3L, 15L))
                .thenReturn(true);

        assertThatThrownBy(() -> miembroService.registrarMiembroEnComunidad(responsableId, request))
                .isInstanceOf(ConflictoException.class)
                .hasMessage("El voluntario con la cédula ingresada ya se encuentra registrado y activo en esta comunidad");

        verify(comunidadMiembroRepository, never()).save(any());
    }

    @Test
    void rechazaRegistroCuandoEdadEsMenorADiezAnos() {
        Long responsableId = 1L;
        LocalDate fechaMenorADiez = LocalDate.of(2018, 5, 10);

        RegistrarMiembroRequest request = new RegistrarMiembroRequest(
                "1710034065",
                "Pedro",
                "Pérez",
                fechaMenorADiez,
                Genero.MASCULINO,
                "0981234567",
                NivelMiembro.APRENDIZ,
                true
        );

        assertThatThrownBy(() -> miembroService.registrarMiembroEnComunidad(responsableId, request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("La persona voluntaria debe tener al menos 10 años de edad cumplidos");

        verify(miembroRepository, never()).save(any());
        verify(comunidadMiembroRepository, never()).save(any());
    }

    @Test
    void rechazaRegistroCuandoResponsableNoTieneComunidadAsignada() {
        Long responsableId = 99L;
        RegistrarMiembroRequest request = new RegistrarMiembroRequest(
                "1710034065",
                "María Tránsito",
                "Amaguaña Alba",
                LocalDate.of(2010, 3, 25),
                Genero.FEMENINO,
                "0981234567",
                NivelMiembro.APRENDIZ,
                true
        );

        when(comunidadRepository.findByResponsableId(responsableId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> miembroService.registrarMiembroEnComunidad(responsableId, request))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("No se encontró ninguna comunidad asociada al responsable autenticado");
    }

    @Test
    void rechazaRegistroCuandoCedulaEsInvalida() {
        Long responsableId = 1L;
        RegistrarMiembroRequest request = new RegistrarMiembroRequest(
                "1710034066",
                "María Tránsito",
                "Amaguaña Alba",
                LocalDate.of(2010, 3, 25),
                Genero.FEMENINO,
                "0981234567",
                NivelMiembro.APRENDIZ,
                true
        );

        assertThatThrownBy(() -> miembroService.registrarMiembroEnComunidad(responsableId, request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("La cédula ingresada no es válida");
    }

    @Test
    void rechazaRegistroCuandoFaltaConsentimientoLOPDP() {
        Long responsableId = 1L;
        RegistrarMiembroRequest request = new RegistrarMiembroRequest(
                "1710034065",
                "María Tránsito",
                "Amaguaña Alba",
                LocalDate.of(2010, 3, 25),
                Genero.FEMENINO,
                "0981234567",
                NivelMiembro.APRENDIZ,
                false
        );

        assertThatThrownBy(() -> miembroService.registrarMiembroEnComunidad(responsableId, request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("Debe certificar la autorización del voluntario o su representante legal bajo la LOPDP");
    }

    @Test
    void registraMiembroPorCorreoResuelveResponsableYRegistra() {
        String correo = "responsable@comunidad.ec";
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setCorreo(correo);

        Comunidad comunidad = new Comunidad();
        comunidad.setId(3L);

        RegistrarMiembroRequest request = new RegistrarMiembroRequest(
                "1710034065",
                "María Tránsito",
                "Amaguaña Alba",
                LocalDate.of(2010, 3, 25),
                Genero.FEMENINO,
                "0981234567",
                NivelMiembro.APRENDIZ,
                true
        );

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.of(usuario));
        when(comunidadRepository.findByResponsableId(1L)).thenReturn(Optional.of(comunidad));
        when(miembroRepository.findByCedula("1710034065")).thenReturn(Optional.empty());
        when(miembroRepository.save(any(Miembro.class))).thenAnswer(invocation -> {
            Miembro m = invocation.getArgument(0);
            m.setId(15L);
            return m;
        });
        when(comunidadMiembroRepository.save(any(ComunidadMiembro.class))).thenAnswer(invocation -> {
            ComunidadMiembro cm = invocation.getArgument(0);
            cm.setId(50L);
            return cm;
        });

        MiembroDetalleResponse response = miembroService.registrarMiembroPorCorreo(correo, request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(15L);
    }

    @Test
    void rechazaRegistroPorCorreoCuandoUsuarioNoExiste() {
        String correo = "desconocido@comunidad.ec";
        RegistrarMiembroRequest request = new RegistrarMiembroRequest(
                "1710034065",
                "María Tránsito",
                "Amaguaña Alba",
                LocalDate.of(2010, 3, 25),
                Genero.FEMENINO,
                "0981234567",
                NivelMiembro.APRENDIZ,
                true
        );

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> miembroService.registrarMiembroPorCorreo(correo, request))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("No se encontró el usuario");
    }
}

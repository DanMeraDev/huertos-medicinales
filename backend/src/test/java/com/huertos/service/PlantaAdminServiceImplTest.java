package com.huertos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.huertos.dto.ActualizarPlantaRequest;
import com.huertos.dto.CrearPlantaRequest;
import com.huertos.dto.DarBajaPlantaRequest;
import com.huertos.dto.PlantaDetalleResponse;
import com.huertos.exception.ConflictoException;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.mapper.PlantaMapper;
import com.huertos.model.CategoriaUso;
import com.huertos.model.CicloCultivo;
import com.huertos.model.CualidadTermica;
import com.huertos.model.OrigenPlanta;
import com.huertos.model.Planta;
import com.huertos.model.Usuario;
import com.huertos.repository.PlantaRepository;
import com.huertos.repository.UsuarioRepository;
import com.huertos.service.impl.PlantaAdminServiceImpl;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlantaAdminServiceImplTest {

    private static final Instant AHORA = Instant.parse("2026-11-12T15:30:00Z");
    private static final LocalDateTime FECHA_DEL_RELOJ = LocalDateTime.ofInstant(AHORA, ZoneOffset.UTC);
    private static final LocalDateTime FECHA_DE_CREACION = LocalDateTime.of(2026, 1, 10, 8, 0);
    private static final String CORREO_TECNICO = "tecnico.msp@huertos.gob.ec";

    @Mock
    private PlantaRepository plantaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Captor
    private ArgumentCaptor<Planta> plantaGuardada;

    private PlantaAdminService servicio;

    @BeforeEach
    void crearServicio() {
        servicio = new PlantaAdminServiceImpl(plantaRepository, usuarioRepository, Mappers.getMapper(PlantaMapper.class),
                Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    @Test
    void registraLaPlantaActivaConLasFechasDelReloj() {
        when(plantaRepository.existsByNombreCientificoIgnoreCase("Herba ficticia")).thenReturn(false);
        when(plantaRepository.save(any(Planta.class))).thenAnswer(invocacion -> conId(invocacion.getArgument(0)));

        servicio.registrarPlanta(solicitudCompleta());

        verify(plantaRepository).save(plantaGuardada.capture());
        Planta planta = plantaGuardada.getValue();
        assertThat(planta.isActiva()).isTrue();
        assertThat(planta.getCreadoEn()).isEqualTo(FECHA_DEL_RELOJ);
        assertThat(planta.getActualizadoEn()).isEqualTo(FECHA_DEL_RELOJ);
        assertThat(planta.getId()).isEqualTo(50L);
    }

    @Test
    void devuelveLaFichaDeLaPlantaRegistrada() {
        when(plantaRepository.existsByNombreCientificoIgnoreCase("Herba ficticia")).thenReturn(false);
        when(plantaRepository.save(any(Planta.class))).thenAnswer(invocacion -> conId(invocacion.getArgument(0)));

        PlantaDetalleResponse ficha = servicio.registrarPlanta(solicitudCompleta());

        assertThat(ficha).isEqualTo(new PlantaDetalleResponse(50L, "Hierba de prueba", "Yura kiwa",
                "Herba ficticia", "Ficticiaceae", CualidadTermica.FRIA, CicloCultivo.CORTO, OrigenPlanta.NATIVA,
                2000, 3200, "Costa, Sierra", "Alivia el dolor de estómago", "Infusión de 5 g en una taza",
                "No usar en embarazo", Set.of(CategoriaUso.SEDANTE, CategoriaUso.DIURETICA),
                "plantas/0b7c6e8a-1d2f-4a3b-9c4d-5e6f7a8b9c0d.jpg"));
    }

    @Test
    void guardaTodasLasCategoriasDeLaPlanta() {
        when(plantaRepository.save(any(Planta.class))).thenAnswer(invocacion -> conId(invocacion.getArgument(0)));

        servicio.registrarPlanta(solicitudCompleta());

        verify(plantaRepository).save(plantaGuardada.capture());
        assertThat(plantaGuardada.getValue().getCategorias())
                .containsExactlyInAnyOrder(CategoriaUso.SEDANTE, CategoriaUso.DIURETICA);
    }

    @Test
    void recortaLosEspaciosDeLosTextos() {
        when(plantaRepository.save(any(Planta.class))).thenAnswer(invocacion -> conId(invocacion.getArgument(0)));
        CrearPlantaRequest solicitud = new CrearPlantaRequest("  Hierba de prueba ", " Yura kiwa ",
                "  Herba ficticia  ", " Ficticiaceae ", CualidadTermica.FRIA, null, null, null, null,
                " Costa, Sierra ", "  Alivia el dolor de estómago ", " Infusión ", " No usar en embarazo ",
                Set.of(CategoriaUso.SEDANTE), " plantas/foto.jpg ");

        servicio.registrarPlanta(solicitud);

        verify(plantaRepository).existsByNombreCientificoIgnoreCase("Herba ficticia");
        verify(plantaRepository).save(plantaGuardada.capture());
        Planta planta = plantaGuardada.getValue();
        assertThat(planta.getNombreComun()).isEqualTo("Hierba de prueba");
        assertThat(planta.getNombreKichwa()).isEqualTo("Yura kiwa");
        assertThat(planta.getNombreCientifico()).isEqualTo("Herba ficticia");
        assertThat(planta.getFamiliaBotanica()).isEqualTo("Ficticiaceae");
        assertThat(planta.getRegionesDistribucion()).isEqualTo("Costa, Sierra");
        assertThat(planta.getUsosMedicinales()).isEqualTo("Alivia el dolor de estómago");
        assertThat(planta.getPreparacionDosificacion()).isEqualTo("Infusión");
        assertThat(planta.getContraindicaciones()).isEqualTo("No usar en embarazo");
        assertThat(planta.getFotografiaUrl()).isEqualTo("plantas/foto.jpg");
    }

    @Test
    void guardaComoNuloLosTextosOpcionalesVacios() {
        when(plantaRepository.save(any(Planta.class))).thenAnswer(invocacion -> conId(invocacion.getArgument(0)));
        CrearPlantaRequest solicitud = new CrearPlantaRequest("Hierba de prueba", "   ", "Herba ficticia",
                "Ficticiaceae", CualidadTermica.CALIENTE, null, null, null, null, "", "Alivia el dolor", " ",
                "", Set.of(CategoriaUso.SEDANTE), "  ");

        servicio.registrarPlanta(solicitud);

        verify(plantaRepository).save(plantaGuardada.capture());
        Planta planta = plantaGuardada.getValue();
        assertThat(planta.getNombreKichwa()).isNull();
        assertThat(planta.getRegionesDistribucion()).isNull();
        assertThat(planta.getPreparacionDosificacion()).isNull();
        assertThat(planta.getContraindicaciones()).isNull();
        assertThat(planta.getFotografiaUrl()).isNull();
    }

    @Test
    void rechazaUnNombreCientificoQueYaExisteSinGuardar() {
        when(plantaRepository.existsByNombreCientificoIgnoreCase("Herba ficticia")).thenReturn(true);

        assertThatThrownBy(() -> servicio.registrarPlanta(solicitudCompleta()))
                .isInstanceOf(ConflictoException.class)
                .hasMessage("El nombre científico ya existe en el catálogo");
        verify(plantaRepository, never()).save(any());
    }

    @Test
    void rechazaUnNombreCientificoDuplicadoEscritoConOtrasMayusculas() {
        CrearPlantaRequest solicitud = new CrearPlantaRequest("Hierba de prueba", null, "HERBA FICTICIA",
                "Ficticiaceae", CualidadTermica.FRIA, null, null, null, null, null, "Alivia el dolor", null, null,
                Set.of(CategoriaUso.SEDANTE), null);
        when(plantaRepository.existsByNombreCientificoIgnoreCase("HERBA FICTICIA")).thenReturn(true);

        assertThatThrownBy(() -> servicio.registrarPlanta(solicitud))
                .isInstanceOf(ConflictoException.class)
                .hasMessage("El nombre científico ya existe en el catálogo");
        verify(plantaRepository, never()).save(any());
    }

    @Test
    void rechazaUnaAltitudMinimaMayorQueLaMaximaSinConsultar() {
        CrearPlantaRequest solicitud = new CrearPlantaRequest("Hierba de prueba", null, "Herba ficticia",
                "Ficticiaceae", CualidadTermica.FRIA, null, null, 3500, 2000, null, "Alivia el dolor", null, null,
                Set.of(CategoriaUso.SEDANTE), null);

        assertThatThrownBy(() -> servicio.registrarPlanta(solicitud))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("La altitud mínima no puede ser mayor que la altitud máxima");
        verifyNoInteractions(plantaRepository);
    }

    @Test
    void aceptaAltitudesIgualesYUnaSolaAltitud() {
        when(plantaRepository.save(any(Planta.class))).thenAnswer(invocacion -> conId(invocacion.getArgument(0)));

        servicio.registrarPlanta(new CrearPlantaRequest("Hierba de prueba", null, "Herba ficticia",
                "Ficticiaceae", CualidadTermica.FRIA, null, null, 2500, 2500, null, "Alivia el dolor", null, null,
                Set.of(CategoriaUso.SEDANTE), null));
        servicio.registrarPlanta(new CrearPlantaRequest("Hierba de prueba", null, "Herba ficticia dos",
                "Ficticiaceae", CualidadTermica.FRIA, null, null, 3500, null, null, "Alivia el dolor", null, null,
                Set.of(CategoriaUso.SEDANTE), null));

        verify(plantaRepository, times(2)).save(any(Planta.class));
    }

    @Test
    void actualizaTodosLosCamposEditablesDeLaPlanta() {
        prepararActualizacion();

        servicio.actualizarPlanta(7L, solicitudDeActualizacion(), CORREO_TECNICO);

        verify(plantaRepository).save(plantaGuardada.capture());
        Planta planta = plantaGuardada.getValue();
        assertThat(planta.getNombreComun()).isEqualTo("Hierba editada");
        assertThat(planta.getNombreKichwa()).isEqualTo("Yura musuk");
        assertThat(planta.getNombreCientifico()).isEqualTo("Herba editata");
        assertThat(planta.getFamiliaBotanica()).isEqualTo("Editaceae");
        assertThat(planta.getCualidadTermica()).isEqualTo(CualidadTermica.CALIENTE);
        assertThat(planta.getCicloCultivo()).isEqualTo(CicloCultivo.LARGO);
        assertThat(planta.getOrigen()).isEqualTo(OrigenPlanta.ENDEMICA);
        assertThat(planta.getAltitudMinMsnm()).isEqualTo(1000);
        assertThat(planta.getAltitudMaxMsnm()).isEqualTo(1800);
        assertThat(planta.getRegionesDistribucion()).isEqualTo("Amazonía");
        assertThat(planta.getUsosMedicinales()).isEqualTo("Calma la tos");
        assertThat(planta.getPreparacionDosificacion()).isEqualTo("Jarabe de 10 ml");
        assertThat(planta.getContraindicaciones()).isEqualTo("No usar en niños");
        assertThat(planta.getFotografiaUrl()).isEqualTo("plantas/nueva.jpg");
    }

    @Test
    void reemplazaLasCategoriasDeLaPlanta() {
        prepararActualizacion();

        servicio.actualizarPlanta(7L, solicitudDeActualizacion(), CORREO_TECNICO);

        verify(plantaRepository).save(plantaGuardada.capture());
        assertThat(plantaGuardada.getValue().getCategorias())
                .containsExactlyInAnyOrder(CategoriaUso.EXPECTORANTE, CategoriaUso.ANTIINFLAMATORIA);
    }

    @Test
    void guardaElTecnicoQueActualizaYLaFechaDelReloj() {
        Usuario tecnico = prepararActualizacion();

        servicio.actualizarPlanta(7L, solicitudDeActualizacion(), CORREO_TECNICO);

        verify(plantaRepository).save(plantaGuardada.capture());
        assertThat(plantaGuardada.getValue().getActualizadoPor()).isSameAs(tecnico);
        assertThat(plantaGuardada.getValue().getActualizadoEn()).isEqualTo(FECHA_DEL_RELOJ);
    }

    @Test
    void noCambiaElIdLaFechaDeCreacionNiElEstadoActivo() {
        prepararActualizacion();

        servicio.actualizarPlanta(7L, solicitudDeActualizacion(), CORREO_TECNICO);

        verify(plantaRepository).save(plantaGuardada.capture());
        Planta planta = plantaGuardada.getValue();
        assertThat(planta.getId()).isEqualTo(7L);
        assertThat(planta.getCreadoEn()).isEqualTo(FECHA_DE_CREACION);
        assertThat(planta.isActiva()).isTrue();
        assertThat(planta.getMotivoBaja()).isNull();
        assertThat(planta.getFechaBaja()).isNull();
        assertThat(planta.getDadoDeBajaPor()).isNull();
    }

    @Test
    void devuelveLaFichaActualizada() {
        prepararActualizacion();

        PlantaDetalleResponse ficha = servicio.actualizarPlanta(7L, solicitudDeActualizacion(), CORREO_TECNICO);

        assertThat(ficha).isEqualTo(new PlantaDetalleResponse(7L, "Hierba editada", "Yura musuk", "Herba editata",
                "Editaceae", CualidadTermica.CALIENTE, CicloCultivo.LARGO, OrigenPlanta.ENDEMICA, 1000, 1800,
                "Amazonía", "Calma la tos", "Jarabe de 10 ml", "No usar en niños",
                Set.of(CategoriaUso.EXPECTORANTE, CategoriaUso.ANTIINFLAMATORIA), "plantas/nueva.jpg"));
    }

    @Test
    void recortaLosEspaciosYGuardaComoNuloLosOpcionalesVaciosAlActualizar() {
        prepararActualizacion();
        ActualizarPlantaRequest solicitud = new ActualizarPlantaRequest("  Hierba editada ", "   ",
                " Herba editata  ", " Editaceae ", CualidadTermica.FRIA, null, null, null, null, "",
                "  Calma la tos ", " ", "", Set.of(CategoriaUso.SEDANTE), "  ");

        servicio.actualizarPlanta(7L, solicitud, CORREO_TECNICO);

        verify(plantaRepository).existsByNombreCientificoIgnoreCaseAndIdNot("Herba editata", 7L);
        verify(plantaRepository).save(plantaGuardada.capture());
        Planta planta = plantaGuardada.getValue();
        assertThat(planta.getNombreComun()).isEqualTo("Hierba editada");
        assertThat(planta.getNombreCientifico()).isEqualTo("Herba editata");
        assertThat(planta.getFamiliaBotanica()).isEqualTo("Editaceae");
        assertThat(planta.getUsosMedicinales()).isEqualTo("Calma la tos");
        assertThat(planta.getNombreKichwa()).isNull();
        assertThat(planta.getRegionesDistribucion()).isNull();
        assertThat(planta.getPreparacionDosificacion()).isNull();
        assertThat(planta.getContraindicaciones()).isNull();
        assertThat(planta.getFotografiaUrl()).isNull();
    }

    @Test
    void rechazaActualizarUnaPlantaInexistenteODadaDeBaja() {
        when(plantaRepository.findByIdAndActivaTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.actualizarPlanta(99L, solicitudDeActualizacion(), CORREO_TECNICO))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("La planta no existe o fue dada de baja");
        verify(plantaRepository, never()).save(any());
    }

    @Test
    void rechazaUnNombreCientificoQuePerteneceAOtraPlanta() {
        when(plantaRepository.findByIdAndActivaTrue(7L)).thenReturn(Optional.of(plantaExistente()));
        when(plantaRepository.existsByNombreCientificoIgnoreCaseAndIdNot("Herba editata", 7L)).thenReturn(true);

        assertThatThrownBy(() -> servicio.actualizarPlanta(7L, solicitudDeActualizacion(), CORREO_TECNICO))
                .isInstanceOf(ConflictoException.class)
                .hasMessage("Nombre científico ya registrado en otra planta");
        verify(plantaRepository, never()).save(any());
    }

    @Test
    void rechazaElNombreCientificoDeOtraPlantaEscritoConOtrasMayusculas() {
        when(plantaRepository.findByIdAndActivaTrue(7L)).thenReturn(Optional.of(plantaExistente()));
        when(plantaRepository.existsByNombreCientificoIgnoreCaseAndIdNot("HERBA EDITATA", 7L)).thenReturn(true);
        ActualizarPlantaRequest solicitud = new ActualizarPlantaRequest("Hierba editada", null, "HERBA EDITATA",
                "Editaceae", CualidadTermica.FRIA, null, null, null, null, null, "Calma la tos", null, null,
                Set.of(CategoriaUso.SEDANTE), null);

        assertThatThrownBy(() -> servicio.actualizarPlanta(7L, solicitud, CORREO_TECNICO))
                .isInstanceOf(ConflictoException.class)
                .hasMessage("Nombre científico ya registrado en otra planta");
        verify(plantaRepository, never()).save(any());
    }

    @Test
    void permiteCambiarSoloLasMayusculasDelNombreCientificoPropio() {
        prepararActualizacion();
        ActualizarPlantaRequest solicitud = new ActualizarPlantaRequest("Hierba de prueba", null, "HERBA FICTICIA",
                "Ficticiaceae", CualidadTermica.FRIA, null, null, null, null, null, "Alivia el dolor", null, null,
                Set.of(CategoriaUso.SEDANTE), null);

        servicio.actualizarPlanta(7L, solicitud, CORREO_TECNICO);

        verify(plantaRepository).existsByNombreCientificoIgnoreCaseAndIdNot("HERBA FICTICIA", 7L);
        verify(plantaRepository).save(plantaGuardada.capture());
        assertThat(plantaGuardada.getValue().getNombreCientifico()).isEqualTo("HERBA FICTICIA");
    }

    @Test
    void rechazaActualizarConUnaAltitudMinimaMayorQueLaMaxima() {
        ActualizarPlantaRequest solicitud = new ActualizarPlantaRequest("Hierba editada", null, "Herba editata",
                "Editaceae", CualidadTermica.FRIA, null, null, 3500, 2000, null, "Calma la tos", null, null,
                Set.of(CategoriaUso.SEDANTE), null);

        assertThatThrownBy(() -> servicio.actualizarPlanta(7L, solicitud, CORREO_TECNICO))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("La altitud mínima no puede ser mayor que la altitud máxima");
        verifyNoInteractions(plantaRepository, usuarioRepository);
    }

    @Test
    void daDeBajaLaPlantaConElMotivoRecortadoLaFechaDelRelojYElTecnico() {
        Usuario tecnico = prepararBaja(plantaExistente());

        servicio.darDeBajaPlanta(7L, new DarBajaPlantaRequest("  Registro duplicado  "), CORREO_TECNICO);

        verify(plantaRepository).save(plantaGuardada.capture());
        Planta planta = plantaGuardada.getValue();
        assertThat(planta.isActiva()).isFalse();
        assertThat(planta.getMotivoBaja()).isEqualTo("Registro duplicado");
        assertThat(planta.getFechaBaja()).isEqualTo(FECHA_DEL_RELOJ);
        assertThat(planta.getDadoDeBajaPor()).isSameAs(tecnico);
    }

    @Test
    void noCambiaLosDemasDatosDeLaPlantaAlDarlaDeBaja() {
        prepararBaja(plantaExistente());

        servicio.darDeBajaPlanta(7L, new DarBajaPlantaRequest("Registro duplicado"), CORREO_TECNICO);

        verify(plantaRepository).save(plantaGuardada.capture());
        Planta planta = plantaGuardada.getValue();
        assertThat(planta.getId()).isEqualTo(7L);
        assertThat(planta.getNombreComun()).isEqualTo("Hierba de prueba");
        assertThat(planta.getNombreCientifico()).isEqualTo("Herba ficticia");
        assertThat(planta.getUsosMedicinales()).isEqualTo("Alivia el dolor de estómago");
        assertThat(planta.getCategorias()).containsExactlyInAnyOrder(CategoriaUso.SEDANTE, CategoriaUso.DIURETICA);
        assertThat(planta.getFotografiaUrl()).isEqualTo("plantas/anterior.jpg");
        assertThat(planta.getCreadoEn()).isEqualTo(FECHA_DE_CREACION);
        assertThat(planta.getActualizadoEn()).isEqualTo(FECHA_DE_CREACION);
        assertThat(planta.getActualizadoPor()).isNull();
    }

    @Test
    void rechazaDarDeBajaUnaPlantaInexistente() {
        when(plantaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.darDeBajaPlanta(99L, new DarBajaPlantaRequest("Registro duplicado"),
                CORREO_TECNICO))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("La planta no existe");
        verify(plantaRepository, never()).save(any());
    }

    @Test
    void rechazaDarDeBajaUnaPlantaQueYaEstaDadaDeBajaSinGuardar() {
        Planta inactiva = plantaExistente();
        inactiva.setActiva(false);
        when(plantaRepository.findById(7L)).thenReturn(Optional.of(inactiva));

        assertThatThrownBy(() -> servicio.darDeBajaPlanta(7L, new DarBajaPlantaRequest("Registro duplicado"),
                CORREO_TECNICO))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("La planta ya está dada de baja");
        verify(plantaRepository, never()).save(any());
        verifyNoInteractions(usuarioRepository);
    }

    private Usuario prepararBaja(Planta planta) {
        Usuario tecnico = new Usuario();
        tecnico.setId(3L);
        tecnico.setCorreo(CORREO_TECNICO);
        when(plantaRepository.findById(7L)).thenReturn(Optional.of(planta));
        when(usuarioRepository.findByCorreo(CORREO_TECNICO)).thenReturn(Optional.of(tecnico));
        when(plantaRepository.save(any(Planta.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        return tecnico;
    }

    private Usuario prepararActualizacion() {
        Usuario tecnico = new Usuario();
        tecnico.setId(3L);
        tecnico.setCorreo(CORREO_TECNICO);
        when(plantaRepository.findByIdAndActivaTrue(7L)).thenReturn(Optional.of(plantaExistente()));
        when(usuarioRepository.findByCorreo(CORREO_TECNICO)).thenReturn(Optional.of(tecnico));
        when(plantaRepository.save(any(Planta.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        return tecnico;
    }

    private static Planta plantaExistente() {
        Planta planta = new Planta();
        planta.setId(7L);
        planta.setNombreComun("Hierba de prueba");
        planta.setNombreKichwa("Yura kiwa");
        planta.setNombreCientifico("Herba ficticia");
        planta.setFamiliaBotanica("Ficticiaceae");
        planta.setCualidadTermica(CualidadTermica.FRIA);
        planta.setCicloCultivo(CicloCultivo.CORTO);
        planta.setOrigen(OrigenPlanta.NATIVA);
        planta.setAltitudMinMsnm(2000);
        planta.setAltitudMaxMsnm(3200);
        planta.setRegionesDistribucion("Costa, Sierra");
        planta.setUsosMedicinales("Alivia el dolor de estómago");
        planta.setCategorias(new HashSet<>(Set.of(CategoriaUso.SEDANTE, CategoriaUso.DIURETICA)));
        planta.setFotografiaUrl("plantas/anterior.jpg");
        planta.setActiva(true);
        planta.setCreadoEn(FECHA_DE_CREACION);
        planta.setActualizadoEn(FECHA_DE_CREACION);
        return planta;
    }

    private static ActualizarPlantaRequest solicitudDeActualizacion() {
        return new ActualizarPlantaRequest("Hierba editada", "Yura musuk", "Herba editata", "Editaceae",
                CualidadTermica.CALIENTE, CicloCultivo.LARGO, OrigenPlanta.ENDEMICA, 1000, 1800, "Amazonía",
                "Calma la tos", "Jarabe de 10 ml", "No usar en niños",
                Set.of(CategoriaUso.EXPECTORANTE, CategoriaUso.ANTIINFLAMATORIA), "plantas/nueva.jpg");
    }

    private static CrearPlantaRequest solicitudCompleta() {
        return new CrearPlantaRequest("Hierba de prueba", "Yura kiwa", "Herba ficticia", "Ficticiaceae",
                CualidadTermica.FRIA, CicloCultivo.CORTO, OrigenPlanta.NATIVA, 2000, 3200, "Costa, Sierra",
                "Alivia el dolor de estómago", "Infusión de 5 g en una taza", "No usar en embarazo",
                Set.of(CategoriaUso.SEDANTE, CategoriaUso.DIURETICA),
                "plantas/0b7c6e8a-1d2f-4a3b-9c4d-5e6f7a8b9c0d.jpg");
    }

    private static Planta conId(Planta planta) {
        planta.setId(50L);
        return planta;
    }
}

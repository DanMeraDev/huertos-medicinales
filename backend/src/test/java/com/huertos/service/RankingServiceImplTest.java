package com.huertos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huertos.dto.RankingComunidadResponse;
import com.huertos.dto.RankingMiembroResponse;
import com.huertos.mapper.RankingMapper;
import com.huertos.model.Comunidad;
import com.huertos.model.Miembro;
import com.huertos.model.NivelMiembro;
import com.huertos.model.TipoOrganizacion;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.MiembroInsigniaRepository;
import com.huertos.repository.MiembroRepository;
import com.huertos.service.impl.RankingServiceImpl;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RankingServiceImplTest {

    @Mock
    private ComunidadRepository comunidadRepository;

    @Mock
    private MiembroRepository miembroRepository;

    @Mock
    private MiembroInsigniaRepository miembroInsigniaRepository;

    @Spy
    private RankingMapper rankingMapper = Mappers.getMapper(RankingMapper.class);

    private RankingService servicio;

    @BeforeEach
    void setUp() {
        servicio = new RankingServiceImpl(comunidadRepository, miembroRepository, miembroInsigniaRepository, rankingMapper);
    }

    @Test
    void asignaPosicionesCorrelativasYOrdenaPorPuntosDescendente() {
        Comunidad com1 = crearComunidad(1L, "Comunidad Allpa", TipoOrganizacion.BARRIO, "Pichincha", "Calderón", 300, LocalDateTime.now());
        Comunidad com2 = crearComunidad(2L, "Comunidad Kawsay", TipoOrganizacion.FAMILIA, "Imbabura", "Otavalo", 200, LocalDateTime.now());
        Comunidad com3 = crearComunidad(3L, "Comunidad Sumak", TipoOrganizacion.COMITE_LOCAL, "Azuay", "Cuenca", 100, LocalDateTime.now());

        when(comunidadRepository.findByActivaTrueOrderByPuntosComunidadDescCreadoEnAsc())
                .thenReturn(List.of(com1, com2, com3));

        List<RankingComunidadResponse> ranking = servicio.obtenerRankingComunidades();

        assertThat(ranking).hasSize(3);

        assertThat(ranking.get(0).posicion()).isEqualTo(1);
        assertThat(ranking.get(0).comunidadId()).isEqualTo(1L);
        assertThat(ranking.get(0).nombreComunidad()).isEqualTo("Comunidad Allpa");
        assertThat(ranking.get(0).tipoOrganizacion()).isEqualTo(TipoOrganizacion.BARRIO);
        assertThat(ranking.get(0).provincia()).isEqualTo("Pichincha");
        assertThat(ranking.get(0).parroquia()).isEqualTo("Calderón");
        assertThat(ranking.get(0).puntosComunidad()).isEqualTo(300);

        assertThat(ranking.get(1).posicion()).isEqualTo(2);
        assertThat(ranking.get(1).comunidadId()).isEqualTo(2L);
        assertThat(ranking.get(1).puntosComunidad()).isEqualTo(200);

        assertThat(ranking.get(2).posicion()).isEqualTo(3);
        assertThat(ranking.get(2).comunidadId()).isEqualTo(3L);
        assertThat(ranking.get(2).puntosComunidad()).isEqualTo(100);

        verify(comunidadRepository).findByActivaTrueOrderByPuntosComunidadDescCreadoEnAsc();
    }

    @Test
    void retornaListaVaciaCuandoNoExistenComunidadesActivas() {
        when(comunidadRepository.findByActivaTrueOrderByPuntosComunidadDescCreadoEnAsc())
                .thenReturn(List.of());

        List<RankingComunidadResponse> ranking = servicio.obtenerRankingComunidades();

        assertThat(ranking).isEmpty();
        verify(comunidadRepository).findByActivaTrueOrderByPuntosComunidadDescCreadoEnAsc();
    }

    @Test
    void asignaPosicionesCorrelativasCorrectasAnteEmpateDePuntos() {
        Comunidad com1 = crearComunidad(10L, "Huerto Norte", TipoOrganizacion.BARRIO, "Pichincha", "Quito", 150, LocalDateTime.of(2026, 1, 1, 10, 0));
        Comunidad com2 = crearComunidad(20L, "Huerto Sur", TipoOrganizacion.BARRIO, "Pichincha", "Quito", 150, LocalDateTime.of(2026, 2, 1, 10, 0));

        when(comunidadRepository.findByActivaTrueOrderByPuntosComunidadDescCreadoEnAsc())
                .thenReturn(List.of(com1, com2));

        List<RankingComunidadResponse> ranking = servicio.obtenerRankingComunidades();

        assertThat(ranking).hasSize(2);
        assertThat(ranking.get(0).posicion()).isEqualTo(1);
        assertThat(ranking.get(0).comunidadId()).isEqualTo(10L);
        assertThat(ranking.get(1).posicion()).isEqualTo(2);
        assertThat(ranking.get(1).comunidadId()).isEqualTo(20L);
    }

    @Test
    @DisplayName("GM-CU05: Asigna posiciones ordinales correlativas y mapea datos de voluntarios con medallas")
    void asignaPosicionesCorrelativasYOrdenaPorPuntosDescendenteVoluntarios() {
        Miembro m1 = crearMiembro(1L, "Carmen", "Chuquimarca", NivelMiembro.MAESTRO, 250, LocalDateTime.now());
        Miembro m2 = crearMiembro(2L, "Luis", "Guamán", NivelMiembro.OFICIAL, 120, LocalDateTime.now());
        Miembro m3 = crearMiembro(3L, "Ana", "Tipantuña", NivelMiembro.APRENDIZ, 40, LocalDateTime.now());

        when(miembroRepository.findByActivoTrueOrderByPuntosTotalesDescCreadoEnAsc())
                .thenReturn(List.of(m1, m2, m3));
        when(miembroInsigniaRepository.countByMiembroId(1L)).thenReturn(5L);
        when(miembroInsigniaRepository.countByMiembroId(2L)).thenReturn(2L);
        when(miembroInsigniaRepository.countByMiembroId(3L)).thenReturn(0L);

        List<RankingMiembroResponse> ranking = servicio.obtenerRankingMiembros();

        assertThat(ranking).hasSize(3);

        assertThat(ranking.get(0).posicion()).isEqualTo(1);
        assertThat(ranking.get(0).miembroId()).isEqualTo(1L);
        assertThat(ranking.get(0).nombres()).isEqualTo("Carmen");
        assertThat(ranking.get(0).apellidos()).isEqualTo("Chuquimarca");
        assertThat(ranking.get(0).nivel()).isEqualTo(NivelMiembro.MAESTRO);
        assertThat(ranking.get(0).puntosTotales()).isEqualTo(250);
        assertThat(ranking.get(0).totalInsignias()).isEqualTo(5L);

        assertThat(ranking.get(1).posicion()).isEqualTo(2);
        assertThat(ranking.get(1).miembroId()).isEqualTo(2L);
        assertThat(ranking.get(1).nombres()).isEqualTo("Luis");
        assertThat(ranking.get(1).apellidos()).isEqualTo("Guamán");
        assertThat(ranking.get(1).nivel()).isEqualTo(NivelMiembro.OFICIAL);
        assertThat(ranking.get(1).puntosTotales()).isEqualTo(120);
        assertThat(ranking.get(1).totalInsignias()).isEqualTo(2L);

        assertThat(ranking.get(2).posicion()).isEqualTo(3);
        assertThat(ranking.get(2).miembroId()).isEqualTo(3L);
        assertThat(ranking.get(2).nombres()).isEqualTo("Ana");
        assertThat(ranking.get(2).apellidos()).isEqualTo("Tipantuña");
        assertThat(ranking.get(2).nivel()).isEqualTo(NivelMiembro.APRENDIZ);
        assertThat(ranking.get(2).puntosTotales()).isEqualTo(40);
        assertThat(ranking.get(2).totalInsignias()).isEqualTo(0L);

        verify(miembroRepository).findByActivoTrueOrderByPuntosTotalesDescCreadoEnAsc();
        verify(miembroInsigniaRepository).countByMiembroId(1L);
        verify(miembroInsigniaRepository).countByMiembroId(2L);
        verify(miembroInsigniaRepository).countByMiembroId(3L);
    }

    @Test
    @DisplayName("GM-CU05: Retorna lista vacía cuando no existen miembros activos")
    void retornaListaVaciaCuandoNoExistenMiembrosActivos() {
        when(miembroRepository.findByActivoTrueOrderByPuntosTotalesDescCreadoEnAsc())
                .thenReturn(List.of());

        List<RankingMiembroResponse> ranking = servicio.obtenerRankingMiembros();

        assertThat(ranking).isEmpty();
        verify(miembroRepository).findByActivoTrueOrderByPuntosTotalesDescCreadoEnAsc();
    }

    @Test
    @DisplayName("GM-CU05: Asigna posiciones ordinales correlativas ante empate de puntos en voluntarios")
    void asignaPosicionesCorrelativasCorrectasAnteEmpateDePuntosVoluntarios() {
        Miembro m1 = crearMiembro(10L, "Carlos", "Paredes", NivelMiembro.OFICIAL, 100, LocalDateTime.of(2026, 1, 1, 10, 0));
        Miembro m2 = crearMiembro(20L, "Elena", "Ramos", NivelMiembro.OFICIAL, 100, LocalDateTime.of(2026, 2, 1, 10, 0));

        when(miembroRepository.findByActivoTrueOrderByPuntosTotalesDescCreadoEnAsc())
                .thenReturn(List.of(m1, m2));
        when(miembroInsigniaRepository.countByMiembroId(10L)).thenReturn(1L);
        when(miembroInsigniaRepository.countByMiembroId(20L)).thenReturn(1L);

        List<RankingMiembroResponse> ranking = servicio.obtenerRankingMiembros();

        assertThat(ranking).hasSize(2);
        assertThat(ranking.get(0).posicion()).isEqualTo(1);
        assertThat(ranking.get(0).miembroId()).isEqualTo(10L);
        assertThat(ranking.get(1).posicion()).isEqualTo(2);
        assertThat(ranking.get(1).miembroId()).isEqualTo(20L);
    }

    private Comunidad crearComunidad(Long id, String nombre, TipoOrganizacion tipo, String provincia, String parroquia, int puntos, LocalDateTime creadoEn) {
        Comunidad comunidad = new Comunidad();
        comunidad.setId(id);
        comunidad.setNombre(nombre);
        comunidad.setTipoOrganizacion(tipo);
        comunidad.setProvincia(provincia);
        comunidad.setParroquia(parroquia);
        comunidad.setPuntosComunidad(puntos);
        comunidad.setCreadoEn(creadoEn);
        comunidad.setActiva(true);
        return comunidad;
    }

    private Miembro crearMiembro(Long id, String nombres, String apellidos, NivelMiembro nivel, int puntosTotales, LocalDateTime creadoEn) {
        Miembro miembro = new Miembro();
        miembro.setId(id);
        miembro.setNombres(nombres);
        miembro.setApellidos(apellidos);
        miembro.setNivel(nivel);
        miembro.setPuntosTotales(puntosTotales);
        miembro.setCreadoEn(creadoEn);
        miembro.setActivo(true);
        return miembro;
    }
}

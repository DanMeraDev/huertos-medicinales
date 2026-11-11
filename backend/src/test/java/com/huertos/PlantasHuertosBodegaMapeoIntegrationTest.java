package com.huertos;

import static org.assertj.core.api.Assertions.assertThat;

import com.huertos.model.BajaMaterialVegetal;
import com.huertos.model.BodegaMaterialVegetal;
import com.huertos.model.CategoriaUso;
import com.huertos.model.CausaFracaso;
import com.huertos.model.CicloCultivo;
import com.huertos.model.Comunidad;
import com.huertos.model.CualidadTermica;
import com.huertos.model.DisenoPrincipal;
import com.huertos.model.EstadoCultivo;
import com.huertos.model.FaseLunar;
import com.huertos.model.FuenteAgua;
import com.huertos.model.Genero;
import com.huertos.model.Huerto;
import com.huertos.model.OrigenPlanta;
import com.huertos.model.Planta;
import com.huertos.model.Rol;
import com.huertos.model.Siembra;
import com.huertos.model.Subhuerto;
import com.huertos.model.SubtipoSubhuerto;
import com.huertos.model.TipoAbono;
import com.huertos.model.TipoCerca;
import com.huertos.model.TipoHuerto;
import com.huertos.model.TipoMaterialVegetal;
import com.huertos.model.TipoOrganizacion;
import com.huertos.model.TipoSuelo;
import com.huertos.model.UnidadMedida;
import com.huertos.model.Usuario;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class PlantasHuertosBodegaMapeoIntegrationTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 10, 10, 0);

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void guardaLaPlantaConSusCategoriasYQuienLaDioDeBaja() {
        Usuario tecnico = entityManager.persist(usuario("tecnico@msp.gob.ec", "1700000001", Rol.TECNICO_MSP));
        Planta planta = planta("Planta de prueba", "Probatoria ficticia");
        planta.setNombreKichwa("Kiwa de prueba");
        planta.setCicloCultivo(CicloCultivo.CORTO);
        planta.setOrigen(OrigenPlanta.INTRODUCIDA);
        planta.setAltitudMinMsnm(1500);
        planta.setAltitudMaxMsnm(3200);
        planta.setRegionesDistribucion("SIERRA");
        planta.setPreparacionDosificacion("Infusión de las flores, una taza tres veces al día");
        planta.setContraindicaciones("No usar en el embarazo");
        planta.setFotografiaUrl("plantas/manzanilla.jpg");
        planta.setCategorias(new HashSet<>(Set.of(CategoriaUso.ANTIESPASMODICA, CategoriaUso.ANTIINFLAMATORIA)));
        planta.setActiva(false);
        planta.setMotivoBaja("Registro duplicado");
        planta.setFechaBaja(AHORA);
        planta.setDadoDeBajaPor(tecnico);
        planta.setActualizadoPor(tecnico);
        Long id = entityManager.persistAndGetId(planta, Long.class);
        entityManager.flush();
        entityManager.clear();

        Planta leida = entityManager.find(Planta.class, id);

        assertThat(leida.getCualidadTermica()).isEqualTo(CualidadTermica.FRIA);
        assertThat(leida.getCicloCultivo()).isEqualTo(CicloCultivo.CORTO);
        assertThat(leida.getOrigen()).isEqualTo(OrigenPlanta.INTRODUCIDA);
        assertThat(leida.getAltitudMaxMsnm()).isEqualTo(3200);
        assertThat(leida.getCategorias())
                .containsExactlyInAnyOrder(CategoriaUso.ANTIESPASMODICA, CategoriaUso.ANTIINFLAMATORIA);
        assertThat(leida.isActiva()).isFalse();
        assertThat(leida.getMotivoBaja()).isEqualTo("Registro duplicado");
        assertThat(leida.getDadoDeBajaPor().getCorreo()).isEqualTo("tecnico@msp.gob.ec");
        assertThat(leida.getActualizadoPor().getCorreo()).isEqualTo("tecnico@msp.gob.ec");
        assertThat(leida.getCreadoEn()).isNotNull();
        assertThat(leida.getActualizadoEn()).isNotNull();
    }

    @Test
    void guardaLaPlantaDelMspSinLosDatosQueElAnexoNoTrae() {
        Long id = entityManager.persistAndGetId(planta("Otra planta de prueba", "Probatoria secunda"), Long.class);
        entityManager.flush();
        entityManager.clear();

        Planta leida = entityManager.find(Planta.class, id);

        assertThat(leida.isActiva()).isTrue();
        assertThat(leida.getCicloCultivo()).isNull();
        assertThat(leida.getAltitudMinMsnm()).isNull();
        assertThat(leida.getFotografiaUrl()).isNull();
        assertThat(leida.getCategorias()).isEmpty();
    }

    @Test
    void guardaElHuertoSinAreaYSuSubhuerto() {
        Comunidad comunidad = entityManager.persist(comunidad());
        Huerto huerto = huerto(comunidad);
        huerto.setTipoSuelo(TipoSuelo.ARCILLOSO);
        huerto.setFuenteAgua(FuenteAgua.CANAL);
        huerto.setTipoAbono(TipoAbono.COMPOST);
        huerto.setTipoCerca(TipoCerca.CERCA_VIVA);
        huerto.setDetalleCerca("Higuerilla y agave");
        entityManager.persist(huerto);

        Subhuerto subhuerto = subhuerto(huerto);
        Long id = entityManager.persistAndGetId(subhuerto, Long.class);
        entityManager.flush();
        entityManager.clear();

        Subhuerto leido = entityManager.find(Subhuerto.class, id);

        assertThat(leido.getSubtipo()).isEqualTo(SubtipoSubhuerto.SURCOS_WACHU);
        assertThat(leido.getAreaM2()).isEqualByComparingTo("10.00");
        Huerto huertoLeido = leido.getHuerto();
        assertThat(huertoLeido.getComunidad().getNombre()).isEqualTo("Hampi Chakra");
        assertThat(huertoLeido.getNombreHuerto()).isEqualTo("Hampi Chakra");
        assertThat(huertoLeido.getAreaTotalM2()).isNull();
        assertThat(huertoLeido.getTipoHuerto()).isEqualTo(TipoHuerto.BASICO);
        assertThat(huertoLeido.getDisenoPrincipal()).isEqualTo(DisenoPrincipal.EN_TIERRA);
        assertThat(huertoLeido.getTipoSuelo()).isEqualTo(TipoSuelo.ARCILLOSO);
        assertThat(huertoLeido.getFuenteAgua()).isEqualTo(FuenteAgua.CANAL);
        assertThat(huertoLeido.getTipoAbono()).isEqualTo(TipoAbono.COMPOST);
        assertThat(huertoLeido.getTipoCerca()).isEqualTo(TipoCerca.CERCA_VIVA);
        assertThat(huertoLeido.getPuntosHuerto()).isZero();
    }

    @Test
    void guardaElLoteDeBodegaConSuBajaYUnaSiembra() {
        Comunidad comunidad = entityManager.persist(comunidad());
        Planta planta = entityManager.persist(planta("Planta de prueba", "Probatoria ficticia"));
        Subhuerto subhuerto = entityManager.persist(subhuerto(entityManager.persist(huerto(comunidad))));

        BodegaMaterialVegetal lote = new BodegaMaterialVegetal();
        lote.setComunidad(comunidad);
        lote.setPlanta(planta);
        lote.setTipoMaterial(TipoMaterialVegetal.SEMILLAS);
        lote.setCantidadDisponible(new BigDecimal("12.50"));
        lote.setUnidadMedida(UnidadMedida.GRAMOS);
        lote.setLoteOrigen("Donación MSP");
        lote.setFechaIngreso(LocalDate.of(2026, 11, 10));
        entityManager.persist(lote);

        BajaMaterialVegetal baja = new BajaMaterialVegetal();
        baja.setBodegaMaterial(lote);
        baja.setCantidadBaja(new BigDecimal("1.25"));
        baja.setCausa(CausaFracaso.CADUCIDAD_PUDRICION);
        baja.setObservacion("Semillas húmedas");
        Long bajaId = entityManager.persistAndGetId(baja, Long.class);

        Siembra siembra = new Siembra();
        siembra.setSubhuerto(subhuerto);
        siembra.setBodegaMaterial(lote);
        siembra.setCantidadSembrada(new BigDecimal("2.50"));
        siembra.setFechaSiembra(LocalDate.of(2026, 11, 10));
        siembra.setFaseLunarSiembra(FaseLunar.WINAK_KILLA_CRECIENTE);
        siembra.setTuvoAlertaAltitud(true);
        siembra.setEstadoCultivo(EstadoCultivo.PERDIDO);
        siembra.setCausaPerdida(CausaFracaso.HELADA_CLIMA_EXTREMO);
        siembra.setObservacionPerdida("Helada de noviembre");
        Long siembraId = entityManager.persistAndGetId(siembra, Long.class);
        entityManager.flush();
        entityManager.clear();

        BajaMaterialVegetal bajaLeida = entityManager.find(BajaMaterialVegetal.class, bajaId);
        assertThat(bajaLeida.getCantidadBaja()).isEqualByComparingTo("1.25");
        assertThat(bajaLeida.getCausa()).isEqualTo(CausaFracaso.CADUCIDAD_PUDRICION);
        assertThat(bajaLeida.getFechaBaja()).isNotNull();

        Siembra leida = entityManager.find(Siembra.class, siembraId);
        assertThat(leida.getCantidadSembrada()).isEqualByComparingTo("2.50");
        assertThat(leida.getFaseLunarSiembra()).isEqualTo(FaseLunar.WINAK_KILLA_CRECIENTE);
        assertThat(leida.isTuvoAlertaAltitud()).isTrue();
        assertThat(leida.getEstadoCultivo()).isEqualTo(EstadoCultivo.PERDIDO);
        assertThat(leida.getCausaPerdida()).isEqualTo(CausaFracaso.HELADA_CLIMA_EXTREMO);
        assertThat(leida.getSubhuerto().getNombreZona()).isEqualTo("Wachu Norte");

        BodegaMaterialVegetal loteLeido = leida.getBodegaMaterial();
        assertThat(loteLeido.getTipoMaterial()).isEqualTo(TipoMaterialVegetal.SEMILLAS);
        assertThat(loteLeido.getUnidadMedida()).isEqualTo(UnidadMedida.GRAMOS);
        assertThat(loteLeido.getCantidadDisponible()).isEqualByComparingTo("12.50");
        assertThat(loteLeido.getPlanta().getNombreComun()).isEqualTo("Planta de prueba");
        assertThat(loteLeido.getComunidad().getNombre()).isEqualTo("Hampi Chakra");
    }

    private Planta planta(String nombreComun, String nombreCientifico) {
        Planta planta = new Planta();
        planta.setNombreComun(nombreComun);
        planta.setNombreCientifico(nombreCientifico);
        planta.setFamiliaBotanica("Asteraceae");
        planta.setCualidadTermica(CualidadTermica.FRIA);
        planta.setUsosMedicinales("Dolor de estómago y cólicos");
        return planta;
    }

    private Comunidad comunidad() {
        Usuario responsable = entityManager.persist(
                usuario("rosa@correo.com", "1712345678", Rol.RESPONSABLE_COMUNIDAD));
        Comunidad comunidad = new Comunidad();
        comunidad.setNombre("Hampi Chakra");
        comunidad.setTipoOrganizacion(TipoOrganizacion.BARRIO);
        comunidad.setProvincia("Pichincha");
        comunidad.setParroquia("Calderón");
        comunidad.setDireccion("Calle 1");
        comunidad.setTelefonoContacto("0991234567");
        comunidad.setCorreoContacto("contacto@correo.com");
        comunidad.setResponsable(responsable);
        comunidad.setActiva(true);
        comunidad.setCreadoEn(AHORA);
        return comunidad;
    }

    private Huerto huerto(Comunidad comunidad) {
        Huerto huerto = new Huerto();
        huerto.setComunidad(comunidad);
        huerto.setNombreHuerto(comunidad.getNombre());
        huerto.setLatitud(new BigDecimal("-0.0950000"));
        huerto.setLongitud(new BigDecimal("-78.4220000"));
        huerto.setAltitudMsnm(2800);
        huerto.setDisenoPrincipal(DisenoPrincipal.EN_TIERRA);
        return huerto;
    }

    private Subhuerto subhuerto(Huerto huerto) {
        Subhuerto subhuerto = new Subhuerto();
        subhuerto.setHuerto(huerto);
        subhuerto.setNombreZona("Wachu Norte");
        subhuerto.setSubtipo(SubtipoSubhuerto.SURCOS_WACHU);
        subhuerto.setAreaM2(new BigDecimal("10.00"));
        return subhuerto;
    }

    private Usuario usuario(String correo, String identificacion, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setNombreCompleto("Persona de prueba");
        usuario.setIdentificacion(identificacion);
        usuario.setFechaNacimiento(LocalDate.of(1990, 5, 14));
        usuario.setGenero(Genero.OTRO);
        usuario.setCorreo(correo);
        usuario.setPasswordHash("hash");
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuario.setCreadoEn(AHORA);
        return usuario;
    }
}

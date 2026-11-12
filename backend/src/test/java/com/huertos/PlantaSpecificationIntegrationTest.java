package com.huertos;

import static org.assertj.core.api.Assertions.assertThat;

import com.huertos.dto.FiltrosPlantaRequest;
import com.huertos.model.CategoriaUso;
import com.huertos.model.CicloCultivo;
import com.huertos.model.CualidadTermica;
import com.huertos.model.OrigenPlanta;
import com.huertos.model.Planta;
import com.huertos.repository.PlantaRepository;
import com.huertos.repository.PlantaSpecification;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class PlantaSpecificationIntegrationTest {

    private static final Sort POR_NOMBRE_COMUN = Sort.by(Sort.Direction.ASC, "nombreComun", "id");

    @Autowired
    private PlantaRepository plantaRepository;

    @Test
    void sinFiltrosDevuelveLasVeintePlantasActivas() {
        assertThat(filtrar(sinFiltros())).hasSize(20);
    }

    @Test
    void filtraPorCualidadTermica() {
        assertThat(filtrar(new FiltrosPlantaRequest(CualidadTermica.FRIA, null, null, null, null, null, null)))
                .containsExactly("Cola de caballo", "Diente de león", "Llantén", "Sábila", "Valeriana", "Violeta");
    }

    @Test
    void filtraPorRegionIgnorandoTildesYMayusculas() {
        assertThat(filtrar(new FiltrosPlantaRequest(null, null, "amazonia", null, null, null, null)))
                .containsExactly("Cola de caballo");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void ignoraUnaRegionVaciaOEnBlanco(String region) {
        assertThat(filtrar(new FiltrosPlantaRequest(null, null, region, null, null, null, null))).hasSize(20);
    }

    @ParameterizedTest
    @ValueSource(strings = {"%", "_"})
    void tomaLosComodinesDeLaRegionComoTextoLiteral(String region) {
        assertThat(filtrar(new FiltrosPlantaRequest(null, null, region, null, null, null, null))).isEmpty();
    }

    @Test
    void filtraPorCicloDeCultivo() {
        assertThat(filtrar(new FiltrosPlantaRequest(null, null, null, null, CicloCultivo.LARGO, null, null)))
                .containsExactly("Cedrón", "Chilca", "Cola de caballo", "Marco", "Matico", "Romero", "Ruda",
                        "Sábila", "Valeriana", "Violeta");
    }

    @Test
    void filtraPorOrigen() {
        assertThat(filtrar(new FiltrosPlantaRequest(null, null, null, null, null, OrigenPlanta.NATIVA, null)))
                .containsExactly("Chilca", "Cola de caballo", "Marco", "Paico");
    }

    @Test
    void filtraPorCategoriaDeUsoSinDuplicarPlantas() {
        assertThat(filtrar(new FiltrosPlantaRequest(null, null, null, CategoriaUso.DIURETICA, null, null, null)))
                .containsExactly("Cola de caballo", "Diente de león", "Llantén", "Ortiga");
    }

    @Test
    void filtraPorAltitudIncluyendoLasPlantasCuyoRangoTerminaEnElValor() {
        assertThat(filtrar(new FiltrosPlantaRequest(null, 2800, null, null, null, null, null)))
                .hasSize(20)
                .contains("Albahaca", "Sábila");
    }

    @Test
    void filtraPorAltitudExcluyendoLasPlantasCuyoRangoNoLaAbarca() {
        assertThat(filtrar(new FiltrosPlantaRequest(null, 2900, null, null, null, null, null)))
                .hasSize(18)
                .doesNotContain("Albahaca", "Sábila");
        assertThat(filtrar(new FiltrosPlantaRequest(null, 3600, null, null, null, null, null)))
                .containsExactly("Chilca", "Diente de león", "Llantén", "Marco", "Ortiga");
        assertThat(filtrar(new FiltrosPlantaRequest(null, 100, null, null, null, null, null)))
                .containsExactly("Albahaca", "Cola de caballo", "Sábila");
    }

    @Test
    void combinaVariosFiltros() {
        assertThat(filtrar(new FiltrosPlantaRequest(CualidadTermica.CALIENTE, 3600, "sierra",
                CategoriaUso.ENERGETICA, null, null, null)))
                .containsExactly("Chilca", "Marco", "Ortiga");
        assertThat(filtrar(new FiltrosPlantaRequest(CualidadTermica.FRIA, null, null, CategoriaUso.CICATRIZANTE,
                CicloCultivo.LARGO, OrigenPlanta.INTRODUCIDA, null)))
                .containsExactly("Sábila");
    }

    @Test
    void combinaElCriterioConLosFiltros() {
        assertThat(filtrar(new FiltrosPlantaRequest(CualidadTermica.FRIA, null, null, null, null, null, " ma ")))
                .containsExactly("Llantén");
    }

    @Test
    void buscaElCriterioEnElNombreKichwaYCientifico() {
        assertThat(filtrar(new FiltrosPlantaRequest(null, null, null, null, null, null, "chupa")))
                .containsExactly("Cola de caballo");
        assertThat(filtrar(new FiltrosPlantaRequest(null, null, null, null, null, null, "matricaria")))
                .containsExactly("Manzanilla");
    }

    @Test
    void noIncluyePlantasInactivas() {
        Planta inactiva = plantaFicticia("Hierba fría ficticia");
        inactiva.setCualidadTermica(CualidadTermica.FRIA);
        inactiva.setActiva(false);
        plantaRepository.saveAndFlush(inactiva);

        assertThat(filtrar(new FiltrosPlantaRequest(CualidadTermica.FRIA, null, null, null, null, null, null)))
                .hasSize(6)
                .doesNotContain("Hierba fría ficticia");
        assertThat(filtrar(sinFiltros())).hasSize(20);
    }

    @Test
    void noExcluyeUnaPlantaSinRangoDeAltitudAlFiltrarPorAltitud() {
        plantaRepository.saveAndFlush(plantaFicticia("Hierba sin rango"));

        assertThat(filtrar(new FiltrosPlantaRequest(null, 100, null, null, null, null, null)))
                .containsExactly("Albahaca", "Cola de caballo", "Hierba sin rango", "Sábila");
    }

    private List<String> filtrar(FiltrosPlantaRequest filtros) {
        return plantaRepository.findAll(PlantaSpecification.filtrar(filtros), POR_NOMBRE_COMUN).stream()
                .map(Planta::getNombreComun)
                .toList();
    }

    private FiltrosPlantaRequest sinFiltros() {
        return new FiltrosPlantaRequest(null, null, null, null, null, null, null);
    }

    private Planta plantaFicticia(String nombreComun) {
        Planta planta = new Planta();
        planta.setNombreComun(nombreComun);
        planta.setNombreCientifico("Planta inventada");
        planta.setFamiliaBotanica("Ficticiaceae");
        planta.setCualidadTermica(CualidadTermica.CALIENTE);
        planta.setUsosMedicinales("Ninguno");
        return planta;
    }
}

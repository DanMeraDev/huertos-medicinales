package com.huertos;

import static org.assertj.core.api.Assertions.assertThat;

import com.huertos.model.CualidadTermica;
import com.huertos.model.Planta;
import com.huertos.repository.PlantaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class PlantaRepositoryIntegrationTest {

    @Autowired
    private PlantaRepository plantaRepository;

    @Test
    void encuentraPorNombreComunIgnorandoTildes() {
        assertThat(plantaRepository.buscarActivasPorNombre("oregano"))
                .extracting(Planta::getNombreComun)
                .containsExactly("Orégano");
    }

    @Test
    void encuentraPorNombreComunIgnorandoMayusculas() {
        assertThat(plantaRepository.buscarActivasPorNombre("MATICO"))
                .extracting(Planta::getNombreComun)
                .containsExactly("Matico");
    }

    @Test
    void encuentraPorNombreKichwa() {
        assertThat(plantaRepository.buscarActivasPorNombre("chupa"))
                .extracting(Planta::getNombreComun)
                .containsExactly("Cola de caballo");
    }

    @Test
    void encuentraPorNombreCientifico() {
        assertThat(plantaRepository.buscarActivasPorNombre("matricaria"))
                .extracting(Planta::getNombreComun)
                .containsExactly("Manzanilla");
    }

    @Test
    void ordenaLosResultadosPorNombreComun() {
        assertThat(plantaRepository.buscarActivasPorNombre("ma"))
                .extracting(Planta::getNombreComun)
                .containsExactly("Llantén", "Manzanilla", "Marco", "Matico", "Romero");
    }

    @Test
    void noIncluyePlantasInactivas() {
        Planta inactiva = new Planta();
        inactiva.setNombreComun("Hierba ficticia");
        inactiva.setNombreCientifico("Planta inventada");
        inactiva.setFamiliaBotanica("Ficticiaceae");
        inactiva.setCualidadTermica(CualidadTermica.FRIA);
        inactiva.setUsosMedicinales("Ninguno");
        inactiva.setActiva(false);
        plantaRepository.saveAndFlush(inactiva);

        assertThat(plantaRepository.buscarActivasPorNombre("ficticia")).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"%", "_", "%%", "__"})
    void tomaLosComodinesComoTextoLiteral(String criterio) {
        assertThat(plantaRepository.buscarActivasPorNombre(criterio)).isEmpty();
    }
}

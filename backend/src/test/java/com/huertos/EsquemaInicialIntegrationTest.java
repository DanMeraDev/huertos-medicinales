package com.huertos;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * Comprueba contra una MariaDB real que la migración V1 deja el esquema que usan los casos de uso.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class EsquemaInicialIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void creaLasSeisTablasDelNegocio() {
        List<String> tablas = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()",
                String.class);

        assertThat(tablas).contains(
                "usuarios", "comunidades", "miembros", "huertos", "plantas_medicinales", "lotes_material_vegetal");
    }

    @Test
    void permiteRegistrarUnHuertoSinDisenoNiCerca() {
        jdbcTemplate.update("""
                INSERT INTO comunidades
                    (nombre, tipo_comunidad, nombre_responsable_contacto, telefono_contacto, email_contacto)
                VALUES ('Hampi Chakra', 'BARRIO', 'Rosa Quishpe', '0991234567', 'rosa@correo.com')
                """);
        Long comunidadId = jdbcTemplate.queryForObject(
                "SELECT id FROM comunidades WHERE nombre = 'Hampi Chakra'", Long.class);

        jdbcTemplate.update("""
                INSERT INTO huertos
                    (comunidad_id, nombre, provincia, parroquia, direccion, area_m2,
                     tipo_suelo, tipo_agua_riego, tipo_abono)
                VALUES (?, 'Huerto San Juan', 'Pichincha', 'Calderón', 'Calle 1', 120.50,
                        'ARCILLOSO', 'POZO', 'COMPOST_ORGANICO')
                """, comunidadId);

        Integer sinDefinir = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM huertos WHERE comunidad_id = ? AND tipo_diseno IS NULL AND tipo_cerca IS NULL",
                Integer.class, comunidadId);
        assertThat(sinDefinir).isEqualTo(1);
    }
}

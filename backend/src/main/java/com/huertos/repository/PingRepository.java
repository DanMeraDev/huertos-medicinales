package com.huertos.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Consulta mínima a la base de datos para comprobar la conexión. Los repositorios del negocio
 * serán interfaces de Spring Data JPA.
 */
@Repository
@RequiredArgsConstructor
public class PingRepository {

    private final JdbcTemplate jdbcTemplate;

    public String versionBaseDeDatos() {
        return jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
    }
}

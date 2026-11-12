package com.huertos.service.impl;

import com.huertos.dto.CalendarioLunarResponse;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.mapper.CalendarioLunarMapper;
import com.huertos.repository.CalendarioLunarRepository;
import com.huertos.service.CalendarioLunarService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CalendarioLunarServiceImpl implements CalendarioLunarService {

    private final CalendarioLunarRepository calendarioLunarRepository;
    private final CalendarioLunarMapper calendarioLunarMapper;

    @Override
    @Transactional(readOnly = true)
    public CalendarioLunarResponse obtenerFaseActual(LocalDate fecha) {
        return calendarioLunarRepository
                .findByFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(fecha, fecha)
                .map(calendarioLunarMapper::toCalendarioLunarResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No hay datos lunares disponibles para la fecha seleccionada"));
    }
}

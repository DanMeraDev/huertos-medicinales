package com.huertos.service.impl;

import com.huertos.dto.SaberAncestralDetalleResponse;
import com.huertos.dto.SaberAncestralResumenResponse;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.mapper.SaberAncestralMapper;
import com.huertos.repository.SaberAncestralRepository;
import com.huertos.service.SaberAncestralService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SaberAncestralServiceImpl implements SaberAncestralService {

    private final SaberAncestralRepository saberAncestralRepository;
    private final SaberAncestralMapper saberAncestralMapper;

    @Override
    @Transactional(readOnly = true)
    public List<SaberAncestralResumenResponse> listarRutaSaberes() {
        return saberAncestralRepository.findAllByOrderByOrdenPasoAsc().stream()
                .map(saberAncestralMapper::toSaberAncestralResumenResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SaberAncestralDetalleResponse obtenerPorId(Long id) {
        return saberAncestralRepository.findById(id)
                .map(saberAncestralMapper::toSaberAncestralDetalleResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El saber ancestral solicitado no fue encontrado"));
    }
}

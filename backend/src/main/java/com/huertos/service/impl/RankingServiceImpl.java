package com.huertos.service.impl;

import com.huertos.dto.RankingComunidadResponse;
import com.huertos.dto.RankingMiembroResponse;
import com.huertos.mapper.RankingMapper;
import com.huertos.model.Comunidad;
import com.huertos.model.Miembro;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.MiembroInsigniaRepository;
import com.huertos.repository.MiembroRepository;
import com.huertos.service.RankingService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private final ComunidadRepository comunidadRepository;
    private final MiembroRepository miembroRepository;
    private final MiembroInsigniaRepository miembroInsigniaRepository;
    private final RankingMapper rankingMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RankingComunidadResponse> obtenerRankingComunidades() {
        List<Comunidad> comunidades = comunidadRepository.findByActivaTrueOrderByPuntosComunidadDescCreadoEnAsc();
        List<RankingComunidadResponse> resultado = new ArrayList<>(comunidades.size());
        for (int i = 0; i < comunidades.size(); i++) {
            resultado.add(rankingMapper.toResponse(comunidades.get(i), i + 1));
        }
        return resultado;
    }

    /**
     * Obtiene el ranking de miembros activos ordenados por puntos descendente,
     * calculando el total de insignias obtenidas y asignando la posición correlativa.
     */
    @Override
    @Transactional(readOnly = true)
    public List<RankingMiembroResponse> obtenerRankingMiembros() {
        List<Miembro> miembros = miembroRepository.findByActivoTrueOrderByPuntosTotalesDescCreadoEnAsc();
        List<RankingMiembroResponse> resultado = new ArrayList<>(miembros.size());
        for (int i = 0; i < miembros.size(); i++) {
            Miembro miembro = miembros.get(i);
            long totalInsignias = miembroInsigniaRepository.countByMiembroId(miembro.getId());
            resultado.add(rankingMapper.toResponse(miembro, i + 1, totalInsignias));
        }
        return resultado;
    }
}

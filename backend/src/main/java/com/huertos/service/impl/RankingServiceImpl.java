package com.huertos.service.impl;

import com.huertos.dto.RankingComunidadResponse;
import com.huertos.mapper.RankingMapper;
import com.huertos.model.Comunidad;
import com.huertos.repository.ComunidadRepository;
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
}

package com.huertos.service.impl;

import com.huertos.dto.RankingComunidadResponse;
import com.huertos.mapper.RankingMapper;
import com.huertos.repository.ComunidadRepository;
import com.huertos.service.RankingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private final ComunidadRepository comunidadRepository;
    private final RankingMapper rankingMapper;

    @Override
    public List<RankingComunidadResponse> obtenerRankingComunidades() {
        throw new UnsupportedOperationException("Pendiente de implementacion");
    }
}

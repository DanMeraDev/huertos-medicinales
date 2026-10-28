package com.noctua.huertos.service.impl;

import com.noctua.huertos.dto.PingResponse;
import com.noctua.huertos.repository.PingRepository;
import com.noctua.huertos.service.PingService;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PingServiceImpl implements PingService {

    private final PingRepository pingRepository;
    private final Clock clock;

    @Override
    public PingResponse ping() {
        return new PingResponse("ok", pingRepository.versionBaseDeDatos(), Instant.now(clock));
    }
}

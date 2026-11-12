package com.huertos.repository;

import com.huertos.dto.FiltrosPlantaRequest;
import com.huertos.model.Planta;
import org.springframework.data.jpa.domain.Specification;

public final class PlantaSpecification {

    private PlantaSpecification() {
    }

    public static Specification<Planta> filtrar(FiltrosPlantaRequest filtros) {
        return Specification.unrestricted();
    }
}

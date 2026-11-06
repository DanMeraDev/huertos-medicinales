package com.huertos.mapper;

import com.huertos.dto.CrearPlantaRequest;
import com.huertos.dto.PlantaResponse;
import com.huertos.model.PlantaMedicinal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PlantaMedicinalMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fotoUrl", ignore = true)
    @Mapping(target = "creadoEn", ignore = true)
    PlantaMedicinal aEntidad(CrearPlantaRequest datos);

    PlantaResponse aResponse(PlantaMedicinal planta);
}

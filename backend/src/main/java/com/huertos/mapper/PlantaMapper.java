package com.huertos.mapper;

import com.huertos.dto.PlantaDetalleResponse;
import com.huertos.dto.PlantaResumenResponse;
import com.huertos.model.Planta;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PlantaMapper {

    PlantaResumenResponse aResumen(Planta planta);

    PlantaDetalleResponse aDetalle(Planta planta);
}

package com.huertos.mapper;

import com.huertos.dto.CrearPlantaRequest;
import com.huertos.dto.PlantaDetalleResponse;
import com.huertos.dto.PlantaResumenResponse;
import com.huertos.model.Planta;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PlantaMapper {

    PlantaResumenResponse aResumen(Planta planta);

    PlantaDetalleResponse aDetalle(Planta planta);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    Planta aEntidad(CrearPlantaRequest request);
}

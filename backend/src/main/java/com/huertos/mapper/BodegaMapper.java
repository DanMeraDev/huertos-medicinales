package com.huertos.mapper;

import com.huertos.dto.MaterialVegetalResponse;
import com.huertos.model.BodegaMaterialVegetal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BodegaMapper {

    @Mapping(source = "comunidad.id", target = "comunidadId")
    @Mapping(source = "planta.id", target = "plantaId")
    @Mapping(source = "planta.nombreComun", target = "nombreComunPlanta")
    @Mapping(source = "planta.nombreCientifico", target = "nombreCientificoPlanta")
    MaterialVegetalResponse toMaterialVegetalResponse(BodegaMaterialVegetal entidad);
}


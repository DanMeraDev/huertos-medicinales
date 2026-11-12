package com.huertos.mapper;

import com.huertos.dto.ComunidadResumenResponse;
import com.huertos.dto.MaterialVegetalResponse;
import com.huertos.model.BodegaMaterialVegetal;
import com.huertos.model.Comunidad;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BodegaMapper {

    @Mapping(source = "comunidad.id", target = "comunidadId")
    @Mapping(source = "planta.id", target = "plantaId")
    @Mapping(source = "planta.nombreComun", target = "nombreComunPlanta")
    @Mapping(source = "planta.nombreCientifico", target = "nombreCientificoPlanta")
    MaterialVegetalResponse toMaterialVegetalResponse(BodegaMaterialVegetal entidad);

    List<MaterialVegetalResponse> toListMaterialVegetalResponse(List<BodegaMaterialVegetal> lista);

    List<ComunidadResumenResponse> toListComunidadResumenResponse(List<Comunidad> comunidades);
}


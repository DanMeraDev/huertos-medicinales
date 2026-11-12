package com.huertos.mapper;

import com.huertos.dto.SiembraDetalleResponse;
import com.huertos.model.Siembra;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SiembraMapper {

    @Mapping(source = "subhuerto.id", target = "subhuertoId")
    @Mapping(source = "subhuerto.nombreZona", target = "nombreSubhuerto")
    @Mapping(source = "bodegaMaterial.id", target = "bodegaMaterialId")
    @Mapping(source = "bodegaMaterial.planta.id", target = "plantaId")
    @Mapping(source = "bodegaMaterial.planta.nombreComun", target = "nombreComunPlanta")
    @Mapping(source = "bodegaMaterial.planta.cualidadTermica", target = "cualidadTermica")
    SiembraDetalleResponse toSiembraDetalleResponse(Siembra siembra);
}


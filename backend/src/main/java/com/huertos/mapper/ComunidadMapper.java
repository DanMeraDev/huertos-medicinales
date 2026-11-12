package com.huertos.mapper;

import com.huertos.dto.ComunidadDetalleResponse;
import com.huertos.model.Comunidad;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ComunidadMapper {

    @Mapping(target = "responsableId", source = "responsable.id")
    @Mapping(target = "nombreResponsable", source = "responsable.nombreCompleto")
    ComunidadDetalleResponse toDetalleResponse(Comunidad comunidad);
}

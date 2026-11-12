package com.huertos.mapper;

import com.huertos.dto.SaberAncestralDetalleResponse;
import com.huertos.dto.SaberAncestralResumenResponse;
import com.huertos.model.SaberAncestral;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SaberAncestralMapper {

    SaberAncestralResumenResponse toSaberAncestralResumenResponse(SaberAncestral entidad);

    SaberAncestralDetalleResponse toSaberAncestralDetalleResponse(SaberAncestral entidad);
}

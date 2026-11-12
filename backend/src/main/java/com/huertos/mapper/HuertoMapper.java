package com.huertos.mapper;

import com.huertos.dto.ActualizarHuertoRequest;
import com.huertos.dto.HuertoDetalleResponse;
import com.huertos.model.Huerto;
import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface HuertoMapper {

    @Mapping(target = "comunidadId", source = "comunidad.id")
    @Mapping(target = "areaOcupadaSubhuertosM2", ignore = true)
    HuertoDetalleResponse toDetalleResponseBase(Huerto huerto);

    default HuertoDetalleResponse toDetalleResponse(Huerto huerto, BigDecimal areaOcupada) {
        HuertoDetalleResponse base = toDetalleResponseBase(huerto);
        return new HuertoDetalleResponse(
                base.id(),
                base.comunidadId(),
                base.nombreHuerto(),
                base.latitud(),
                base.longitud(),
                base.altitudMsnm(),
                base.orientacion(),
                base.areaTotalM2(),
                base.disenoPrincipal(),
                base.tipoSuelo(),
                base.fuenteAgua(),
                base.tipoAbono(),
                base.tipoCerca(),
                base.detalleCerca(),
                base.tipoHuerto(),
                base.puntosHuerto(),
                areaOcupada);
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "comunidad", ignore = true)
    @Mapping(target = "nombreHuerto", ignore = true)
    @Mapping(target = "tipoHuerto", ignore = true)
    @Mapping(target = "puntosHuerto", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "creadoEn", ignore = true)
    void actualizarDesdeRequest(ActualizarHuertoRequest request, @MappingTarget Huerto huerto);
}

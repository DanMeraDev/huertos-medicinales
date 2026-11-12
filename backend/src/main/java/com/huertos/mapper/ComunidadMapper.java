package com.huertos.mapper;

import com.huertos.dto.ComunidadDetalleResponse;
import com.huertos.dto.ComunidadResponse;
import com.huertos.dto.DirectorioComunidadResponse;
import com.huertos.model.Comunidad;
import com.huertos.model.Huerto;
import com.huertos.model.SolicitudComunidad;
import com.huertos.model.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ComunidadMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nombre", source = "solicitud.nombreComunidad")
    @Mapping(target = "tipoOrganizacion", source = "solicitud.tipoOrganizacion")
    @Mapping(target = "provincia", source = "solicitud.provincia")
    @Mapping(target = "parroquia", source = "solicitud.parroquia")
    @Mapping(target = "direccion", source = "solicitud.direccion")
    @Mapping(target = "telefonoContacto", source = "solicitud.celularSolicitante")
    @Mapping(target = "correoContacto", source = "solicitud.correoSolicitante")
    @Mapping(target = "responsable", source = "responsable")
    @Mapping(target = "puntosComunidad", ignore = true)
    @Mapping(target = "activa", constant = "true")
    @Mapping(target = "creadoEn", ignore = true)
    Comunidad toComunidad(SolicitudComunidad solicitud, Usuario responsable);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "comunidad", source = "comunidad")
    @Mapping(target = "nombreHuerto", source = "comunidad.nombre")
    @Mapping(target = "latitud", source = "solicitud.latitud")
    @Mapping(target = "longitud", source = "solicitud.longitud")
    @Mapping(target = "altitudMsnm", source = "solicitud.altitudMsnm")
    @Mapping(target = "disenoPrincipal", source = "solicitud.disenoPrincipal")
    @Mapping(target = "orientacion", ignore = true)
    @Mapping(target = "areaTotalM2", ignore = true)
    @Mapping(target = "tipoHuerto", ignore = true)
    @Mapping(target = "tipoSuelo", ignore = true)
    @Mapping(target = "fuenteAgua", ignore = true)
    @Mapping(target = "tipoAbono", ignore = true)
    @Mapping(target = "tipoCerca", ignore = true)
    @Mapping(target = "detalleCerca", ignore = true)
    @Mapping(target = "puntosHuerto", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "creadoEn", ignore = true)
    Huerto toHuerto(SolicitudComunidad solicitud, Comunidad comunidad);

    @Mapping(target = "responsableId", source = "responsable.id")
    ComunidadResponse toComunidadResponse(Comunidad comunidad);

    @Mapping(target = "responsableId", source = "responsable.id")
    @Mapping(target = "nombreResponsable", source = "responsable.nombreCompleto")
    ComunidadDetalleResponse toDetalleResponse(Comunidad comunidad);

    @Mapping(target = "id", source = "comunidad.id")
    @Mapping(target = "nombre", source = "comunidad.nombre")
    @Mapping(target = "nombreResponsable", source = "nombreResponsable")
    @Mapping(target = "telefonoContacto", source = "comunidad.telefonoContacto")
    @Mapping(target = "correoContacto", source = "comunidad.correoContacto")
    @Mapping(target = "totalMiembros", source = "totalMiembros")
    @Mapping(target = "provincia", source = "comunidad.provincia")
    @Mapping(target = "parroquia", source = "comunidad.parroquia")
    @Mapping(target = "direccion", source = "comunidad.direccion")
    @Mapping(target = "latitud", source = "huerto.latitud")
    @Mapping(target = "longitud", source = "huerto.longitud")
    @Mapping(target = "altitudMsnm", source = "huerto.altitudMsnm")
    DirectorioComunidadResponse toDirectorioResponse(Comunidad comunidad, String nombreResponsable, int totalMiembros, Huerto huerto);
}

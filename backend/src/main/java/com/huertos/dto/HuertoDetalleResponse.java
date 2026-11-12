package com.huertos.dto;

import com.huertos.model.DisenoPrincipal;
import com.huertos.model.FuenteAgua;
import com.huertos.model.TipoAbono;
import com.huertos.model.TipoCerca;
import com.huertos.model.TipoHuerto;
import com.huertos.model.TipoSuelo;
import java.math.BigDecimal;

public record HuertoDetalleResponse(
        Long id,
        Long comunidadId,
        String nombreHuerto,
        BigDecimal latitud,
        BigDecimal longitud,
        int altitudMsnm,
        String orientacion,
        BigDecimal areaTotalM2,
        DisenoPrincipal disenoPrincipal,
        TipoSuelo tipoSuelo,
        FuenteAgua fuenteAgua,
        TipoAbono tipoAbono,
        TipoCerca tipoCerca,
        String detalleCerca,
        TipoHuerto tipoHuerto,
        int puntosHuerto,
        BigDecimal areaOcupadaSubhuertosM2) {
}


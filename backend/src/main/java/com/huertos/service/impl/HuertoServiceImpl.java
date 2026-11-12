package com.huertos.service.impl;

import com.huertos.dto.ActualizarHuertoRequest;
import com.huertos.dto.HuertoDetalleResponse;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.mapper.HuertoMapper;
import com.huertos.model.Huerto;
import com.huertos.model.Usuario;
import com.huertos.repository.HuertoRepository;
import com.huertos.repository.SubhuertoRepository;
import com.huertos.repository.UsuarioRepository;
import com.huertos.service.HuertoService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HuertoServiceImpl implements HuertoService {

    private static final BigDecimal AREA_MINIMA_M2 = new BigDecimal("16.00");
    private static final BigDecimal AREA_MAXIMA_M2 = new BigDecimal("400.00");

    private final HuertoRepository huertoRepository;
    private final SubhuertoRepository subhuertoRepository;
    private final UsuarioRepository usuarioRepository;
    private final HuertoMapper huertoMapper;

    @Override
    @Transactional(readOnly = true)
    public HuertoDetalleResponse obtenerPorCorreo(String correo) {
        Huerto huerto = obtenerHuertoDelResponsable(correo);
        BigDecimal areaOcupada = subhuertoRepository.sumAreaActivaByHuertoId(huerto.getId());
        return huertoMapper.toDetalleResponse(huerto, areaOcupada);
    }

    @Override
    @Transactional
    public HuertoDetalleResponse actualizarPorCorreo(String correo, ActualizarHuertoRequest request) {
        Huerto huerto = obtenerHuertoDelResponsable(correo);
        validarRangoAreaMsp(request.areaTotalM2());

        BigDecimal areaOcupada = subhuertoRepository.sumAreaActivaByHuertoId(huerto.getId());
        if (request.areaTotalM2().compareTo(areaOcupada) < 0) {
            throw new ReglaNegocioException(
                    "No puedes reducir a " + request.areaTotalM2() + " m² porque tus subhuertos ocupan " + areaOcupada + " m²");
        }

        huertoMapper.actualizarDesdeRequest(request, huerto);
        Huerto huertoActualizado = huertoRepository.save(huerto);
        return huertoMapper.toDetalleResponse(huertoActualizado, areaOcupada);
    }

    private Huerto obtenerHuertoDelResponsable(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el usuario con correo: " + correo));

        return huertoRepository.findByComunidadResponsableId(usuario.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("La comunidad no tiene un huerto registrado"));
    }

    private void validarRangoAreaMsp(BigDecimal areaTotalM2) {
        if (areaTotalM2.compareTo(AREA_MINIMA_M2) < 0 || areaTotalM2.compareTo(AREA_MAXIMA_M2) > 0) {
            throw new ReglaNegocioException(
                    "El huerto debe medir entre 16.00 m² y 400.00 m² según lineamientos MSP");
        }
    }
}
package com.huertos.service;

import com.huertos.dto.ActividadCatalogoResponse;
import com.huertos.dto.CrearActividadCatalogoRequest;
import com.huertos.exception.ConflictoException;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.model.ActividadComunidad;
import com.huertos.model.Comunidad;
import com.huertos.repository.ActividadComunidadRepository;
import com.huertos.repository.ComunidadRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActividadCatalogoServiceImpl implements ActividadCatalogoService {

    private static final int PUNTOS_POR_HORA_BASE = 1;

    private final ActividadComunidadRepository actividadRepository;
    private final ComunidadRepository comunidadRepository;

    @Override
    @Transactional
    public ActividadCatalogoResponse crear(String correoResponsable, CrearActividadCatalogoRequest request) {
        Comunidad comunidad = obtenerComunidadActiva(correoResponsable);
        String nombreLimpio = request.nombre().trim();

        if (actividadRepository.existsByComunidadIdAndNombreIgnoreCase(comunidad.getId(), nombreLimpio)) {
            throw new ConflictoException("Ya existe una actividad con este nombre en tu comunidad");
        }

        ActividadComunidad nueva = new ActividadComunidad();
        nueva.setComunidad(comunidad);
        nueva.setNombre(nombreLimpio);
        nueva.setCategoria(request.categoria());
        nueva.setPuntosPorHora(PUNTOS_POR_HORA_BASE);
        nueva.setActiva(true);

        ActividadComunidad guardada = actividadRepository.save(nueva);
        return aResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActividadCatalogoResponse> listarPorResponsable(String correoResponsable) {
        Comunidad comunidad = obtenerComunidadActiva(correoResponsable);
        return actividadRepository.findByComunidadIdOrderByNombreAsc(comunidad.getId())
                .stream()
                .map(this::aResponse)
                .toList();
    }

    private Comunidad obtenerComunidadActiva(String correoResponsable) {
        Comunidad comunidad = comunidadRepository.findByResponsableCorreo(correoResponsable)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró una comunidad asociada al usuario"));
        if (!comunidad.isActiva()) {
            throw new ReglaNegocioException("La comunidad no se encuentra activa en el sistema");
        }
        return comunidad;
    }

    private ActividadCatalogoResponse aResponse(ActividadComunidad actividad) {
        return new ActividadCatalogoResponse(
                actividad.getId(),
                actividad.getNombre(),
                actividad.getCategoria(),
                actividad.getPuntosPorHora(),
                actividad.isActiva()
        );
    }
}
package com.huertos.service.impl;

import com.huertos.config.ConstantesGamificacion;
import com.huertos.dto.InsigniaMiembroResponse;
import com.huertos.dto.RegistrarLaborMiembroRequest;
import com.huertos.dto.ResultadoAcreditacionMiembroResponse;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.model.ActividadComunidad;
import com.huertos.model.Comunidad;
import com.huertos.model.Miembro;
import com.huertos.model.MiembroInsignia;
import com.huertos.model.NivelMiembro;
import com.huertos.model.RegistroActividadMiembro;
import com.huertos.model.TalentoComunidad;
import com.huertos.model.TipoOrigenInsignia;
import com.huertos.repository.ActividadComunidadRepository;
import com.huertos.repository.ComunidadMiembroRepository;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.MiembroInsigniaRepository;
import com.huertos.repository.MiembroRepository;
import com.huertos.repository.RegistroActividadMiembroRepository;
import com.huertos.repository.TalentoComunidadRepository;
import com.huertos.service.RegistroActividadMiembroService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistroActividadMiembroServiceImpl implements RegistroActividadMiembroService {

    private final ComunidadRepository comunidadRepository;
    private final ComunidadMiembroRepository comunidadMiembroRepository;
    private final MiembroRepository miembroRepository;
    private final ActividadComunidadRepository actividadComunidadRepository;
    private final RegistroActividadMiembroRepository registroActividadMiembroRepository;
    private final TalentoComunidadRepository talentoComunidadRepository;
    private final MiembroInsigniaRepository miembroInsigniaRepository;

    @Override
    @Transactional
    public ResultadoAcreditacionMiembroResponse registrarLabor(String correoResponsable, RegistrarLaborMiembroRequest request) {
        Comunidad comunidad = comunidadRepository.findByResponsableCorreo(correoResponsable)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró una comunidad asociada al usuario"));

        Miembro miembro = miembroRepository.findById(request.miembroId())
                .orElseThrow(() -> new RecursoNoEncontradoException("El miembro especificado no existe"));

        if (!miembro.isActivo() || !comunidadMiembroRepository.existsByComunidadIdAndMiembroIdAndActivoEnComunidadTrue(comunidad.getId(), miembro.getId())) {
            throw new ReglaNegocioException("El miembro seleccionado no está activo en la comunidad");
        }

        ActividadComunidad actividad = actividadComunidadRepository.findById(request.actividadComunidadId())
                .orElseThrow(() -> new RecursoNoEncontradoException("La actividad comunitaria especificada no existe"));

        if (!actividad.isActiva() || !actividad.getComunidad().getId().equals(comunidad.getId())) {
            throw new ReglaNegocioException("La actividad comunitaria no está activa o no pertenece a su comunidad");
        }

        int puntosPorHora = actividad.getPuntosPorHora() > 0 ? actividad.getPuntosPorHora() : 1;
        int puntosOtorgados = request.horasTrabajadas() * puntosPorHora;

        RegistroActividadMiembro registro = new RegistroActividadMiembro();
        registro.setComunidad(comunidad);
        registro.setMiembro(miembro);
        registro.setActividadComunidad(actividad);
        registro.setFechaActividad(request.fechaActividad());
        registro.setHorasTrabajadas(request.horasTrabajadas());
        registro.setPuntosOtorgados(puntosOtorgados);
        registro.setObservacion(request.observacion());

        RegistroActividadMiembro guardado = registroActividadMiembroRepository.save(registro);

        int nuevosPuntos = miembro.getPuntosTotales() + puntosOtorgados;
        miembro.setPuntosTotales(nuevosPuntos);

        NivelMiembro nivelAnterior = miembro.getNivel();
        NivelMiembro nuevoNivel = determinarNuevoNivel(nuevosPuntos, nivelAnterior);
        boolean subioDeNivel = (nuevoNivel != nivelAnterior);
        miembro.setNivel(nuevoNivel);
        miembroRepository.save(miembro);

        long repeticiones = registroActividadMiembroRepository.countByMiembroIdAndActividadComunidadId(miembro.getId(), actividad.getId());
        List<TalentoComunidad> talentos = talentoComunidadRepository.findByActividadComunidadIdAndActivoTrue(actividad.getId());
        List<String> talentosDesbloqueados = new ArrayList<>();

        for (TalentoComunidad talento : talentos) {
            if (repeticiones >= talento.getMetaRepeticiones()
                    && !miembroInsigniaRepository.existsByMiembroIdAndTalentoComunidadId(miembro.getId(), talento.getId())) {
                MiembroInsignia insignia = new MiembroInsignia();
                insignia.setMiembro(miembro);
                insignia.setComunidad(comunidad);
                insignia.setTipoOrigen(TipoOrigenInsignia.TALENTO_COMUNIDAD);
                insignia.setNombreInsignia(talento.getNombreInsignia());
                insignia.setDescripcion(talento.getDescripcion());
                insignia.setIconoCodigo(talento.getIconoCodigo());
                insignia.setTalentoComunidad(talento);
                insignia.setFechaObtencion(request.fechaActividad());
                miembroInsigniaRepository.save(insignia);
                talentosDesbloqueados.add(talento.getNombreInsignia());
            }
        }

        return new ResultadoAcreditacionMiembroResponse(
                guardado.getId(),
                miembro.getId(),
                puntosOtorgados,
                miembro.getPuntosTotales(),
                miembro.getNivel(),
                subioDeNivel,
                talentosDesbloqueados
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<InsigniaMiembroResponse> listarInsigniasMiembro(String correoResponsable, Long miembroId) {
        Comunidad comunidad = comunidadRepository.findByResponsableCorreo(correoResponsable)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró una comunidad asociada al usuario"));

        if (!comunidadMiembroRepository.existsByComunidadIdAndMiembroIdAndActivoEnComunidadTrue(comunidad.getId(), miembroId)) {
            throw new ReglaNegocioException("El miembro no pertenece a la comunidad del usuario");
        }

        return miembroInsigniaRepository.findByMiembroIdOrderByFechaObtencionDesc(miembroId)
                .stream()
                .map(insignia -> new InsigniaMiembroResponse(
                        insignia.getId(),
                        insignia.getNombreInsignia(),
                        insignia.getDescripcion(),
                        insignia.getIconoCodigo(),
                        insignia.getTipoOrigen(),
                        insignia.getFechaObtencion()
                ))
                .toList();
    }

    private NivelMiembro determinarNuevoNivel(int puntosTotales, NivelMiembro nivelActual) {
        if (puntosTotales >= ConstantesGamificacion.UMBRAL_MAESTRO) {
            return NivelMiembro.MAESTRO;
        }
        if (puntosTotales >= ConstantesGamificacion.UMBRAL_OFICIAL) {
            return (nivelActual == NivelMiembro.MAESTRO) ? NivelMiembro.MAESTRO : NivelMiembro.OFICIAL;
        }
        return (nivelActual == null) ? NivelMiembro.APRENDIZ : nivelActual;
    }
}

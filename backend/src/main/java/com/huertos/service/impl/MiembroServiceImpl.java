package com.huertos.service.impl;

import com.huertos.dto.ActualizarMiembroRequest;
import com.huertos.dto.CambiarEstadoMiembroRequest;
import com.huertos.dto.MiembroDetalleResponse;
import com.huertos.dto.MiembroEstadoResponse;
import com.huertos.dto.RegistrarMiembroRequest;
import com.huertos.exception.ConflictoException;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.mapper.MiembroMapper;
import com.huertos.model.Comunidad;
import com.huertos.model.ComunidadMiembro;
import com.huertos.model.Miembro;
import com.huertos.model.Usuario;
import com.huertos.repository.ComunidadMiembroRepository;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.MiembroRepository;
import com.huertos.repository.UsuarioRepository;
import com.huertos.service.MiembroService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MiembroServiceImpl implements MiembroService {

    private final MiembroRepository miembroRepository;
    private final ComunidadMiembroRepository comunidadMiembroRepository;
    private final ComunidadRepository comunidadRepository;
    private final UsuarioRepository usuarioRepository;
    private final MiembroMapper miembroMapper;
    private final Clock clock;

    @Override
    @Transactional
    public MiembroDetalleResponse registrarMiembroEnComunidad(Long responsableId, RegistrarMiembroRequest request) {
        if (request.consentimientoDatos() == null || !request.consentimientoDatos()) {
            throw new ReglaNegocioException("Debe certificar la autorización del voluntario o su representante legal bajo la LOPDP");
        }

        validarCedula(request.cedula());

        LocalDate hoy = LocalDate.now(clock);
        int edad = Period.between(request.fechaNacimiento(), hoy).getYears();
        if (edad < 10) {
            throw new ReglaNegocioException("La persona voluntaria debe tener al menos 10 años de edad cumplidos");
        }

        Comunidad comunidad = comunidadRepository.findByResponsableId(responsableId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró ninguna comunidad asociada al responsable autenticado"));

        Optional<Miembro> miembroOpt = miembroRepository.findByCedula(request.cedula());

        if (miembroOpt.isPresent()) {
            Miembro miembro = miembroOpt.get();
            if (comunidadMiembroRepository.existsByComunidadIdAndMiembroIdAndActivoEnComunidadTrue(comunidad.getId(), miembro.getId())) {
                throw new ConflictoException("El voluntario con la cédula ingresada ya se encuentra registrado y activo en esta comunidad");
            }

            ComunidadMiembro relacion = comunidadMiembroRepository.findByComunidadIdAndMiembroId(comunidad.getId(), miembro.getId())
                    .orElseGet(() -> {
                        ComunidadMiembro nuevaRelacion = new ComunidadMiembro();
                        nuevaRelacion.setComunidad(comunidad);
                        nuevaRelacion.setMiembro(miembro);
                        return nuevaRelacion;
                    });

            relacion.setActivoEnComunidad(true);
            relacion.setFechaIngreso(hoy);
            relacion.setFechaBaja(null);
            relacion.setMotivoBaja(null);

            ComunidadMiembro relacionGuardada = comunidadMiembroRepository.save(relacion);
            int edadExistente = Period.between(miembro.getFechaNacimiento(), hoy).getYears();
            return miembroMapper.toDetalleResponse(miembro, relacionGuardada, edadExistente);
        }

        Miembro nuevoMiembro = miembroMapper.toMiembro(request);
        nuevoMiembro.setPuntosTotales(0);
        nuevoMiembro.setActivo(true);
        nuevoMiembro.setConsentimientoDatos(true);
        nuevoMiembro.setConsentimientoEn(LocalDateTime.now(clock));
        nuevoMiembro.setCreadoEn(LocalDateTime.now(clock));

        Miembro miembroGuardado = miembroRepository.save(nuevoMiembro);

        ComunidadMiembro relacion = new ComunidadMiembro();
        relacion.setComunidad(comunidad);
        relacion.setMiembro(miembroGuardado);
        relacion.setActivoEnComunidad(true);
        relacion.setFechaIngreso(hoy);

        ComunidadMiembro relacionGuardada = comunidadMiembroRepository.save(relacion);
        return miembroMapper.toDetalleResponse(miembroGuardado, relacionGuardada, edad);
    }

    @Override
    @Transactional
    public MiembroDetalleResponse registrarMiembroPorCorreo(String correo, RegistrarMiembroRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el usuario"));
        return registrarMiembroEnComunidad(usuario.getId(), request);
    }

    @Override
    @Transactional
    public MiembroEstadoResponse cambiarEstadoEnComunidad(Long responsableId, Long miembroId, CambiarEstadoMiembroRequest request) {
        Comunidad comunidad = comunidadRepository.findByResponsableId(responsableId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró ninguna comunidad asociada al responsable autenticado"));

        ComunidadMiembro relacion = comunidadMiembroRepository.findByComunidadIdAndMiembroId(comunidad.getId(), miembroId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El miembro especificado no pertenece a tu comunidad"));

        if (relacion.isActivoEnComunidad() == request.activo()) {
            throw new ConflictoException("El voluntario ya se encuentra en el estado solicitado");
        }

        if (Boolean.FALSE.equals(request.activo())) {
            relacion.setActivoEnComunidad(false);
            relacion.setFechaBaja(LocalDate.now(clock));
            relacion.setMotivoBaja(request.motivoBaja());
        } else {
            relacion.setActivoEnComunidad(true);
            relacion.setFechaBaja(null);
            relacion.setMotivoBaja(null);
        }

        ComunidadMiembro relacionGuardada = comunidadMiembroRepository.save(relacion);
        return miembroMapper.toEstadoResponse(relacionGuardada, "Estado de participación actualizado exitosamente");
    }

    @Override
    @Transactional
    public MiembroEstadoResponse cambiarEstadoPorCorreo(String correo, Long miembroId, CambiarEstadoMiembroRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el usuario"));
        return cambiarEstadoEnComunidad(usuario.getId(), miembroId, request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MiembroDetalleResponse> listarMiembrosDeMiComunidad(Long responsableId) {
        Comunidad comunidad = comunidadRepository.findByResponsableId(responsableId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró ninguna comunidad asociada al responsable autenticado"));

        LocalDate hoy = LocalDate.now(clock);
        return comunidadMiembroRepository.findByComunidadId(comunidad.getId()).stream()
                .map(rel -> {
                    int edad = Period.between(rel.getMiembro().getFechaNacimiento(), hoy).getYears();
                    return miembroMapper.toDetalleResponse(rel.getMiembro(), rel, edad);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MiembroDetalleResponse> listarMiembrosPorCorreo(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el usuario"));
        return listarMiembrosDeMiComunidad(usuario.getId());
    }

    @Override
    @Transactional
    public MiembroDetalleResponse actualizarDatosContacto(Long responsableId, Long miembroId, ActualizarMiembroRequest request) {
        Comunidad comunidad = comunidadRepository.findByResponsableId(responsableId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró ninguna comunidad asociada al responsable autenticado"));

        ComunidadMiembro relacion = comunidadMiembroRepository.findByComunidadIdAndMiembroId(comunidad.getId(), miembroId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El voluntario no pertenece a la comunidad administrada"));

        Miembro miembro = relacion.getMiembro();
        miembroMapper.actualizarDesdeRequest(request, miembro);
        Miembro miembroGuardado = miembroRepository.save(miembro);

        LocalDate hoy = LocalDate.now(clock);
        int edad = Period.between(miembroGuardado.getFechaNacimiento(), hoy).getYears();
        return miembroMapper.toDetalleResponse(miembroGuardado, relacion, edad);
    }

    @Override
    @Transactional
    public MiembroDetalleResponse actualizarDatosContactoPorCorreo(String correo, Long miembroId, ActualizarMiembroRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el usuario"));
        return actualizarDatosContacto(usuario.getId(), miembroId, request);
    }

    private void validarCedula(String cedula) {
        if (cedula == null || !cedula.matches("^[0-9]{10}$")) {
            throw new ReglaNegocioException("La cédula ingresada no es válida");
        }
        int provincia = Integer.parseInt(cedula.substring(0, 2));
        if ((provincia < 1 || provincia > 24) && provincia != 30) {
            throw new ReglaNegocioException("La cédula ingresada no es válida");
        }
        int tercerDigito = Character.getNumericValue(cedula.charAt(2));
        if (tercerDigito >= 6) {
            throw new ReglaNegocioException("La cédula ingresada no es válida");
        }
        int[] coeficientes = {2, 1, 2, 1, 2, 1, 2, 1, 2};
        int suma = 0;
        for (int i = 0; i < 9; i++) {
            int valor = Character.getNumericValue(cedula.charAt(i)) * coeficientes[i];
            suma += (valor >= 10) ? valor - 9 : valor;
        }
        int digitoVerificador = (suma % 10 == 0) ? 0 : 10 - (suma % 10);
        if (digitoVerificador != Character.getNumericValue(cedula.charAt(9))) {
            throw new ReglaNegocioException("La cédula ingresada no es válida");
        }
    }
}

package com.huertos.service.impl;

import com.huertos.dto.BajaMaterialResponse;
import com.huertos.dto.MaterialVegetalResponse;
import com.huertos.dto.RegistrarBajaMaterialRequest;
import com.huertos.dto.RegistrarMaterialVegetalRequest;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.mapper.BodegaMapper;
import com.huertos.model.BajaMaterialVegetal;
import com.huertos.model.BodegaMaterialVegetal;
import com.huertos.model.Comunidad;
import com.huertos.model.Planta;
import com.huertos.repository.BajaMaterialVegetalRepository;
import com.huertos.repository.BodegaMaterialVegetalRepository;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.PlantaRepository;
import com.huertos.service.BodegaService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BodegaServiceImpl implements BodegaService {

    private final BodegaMaterialVegetalRepository bodegaRepository;
    private final ComunidadRepository comunidadRepository;
    private final PlantaRepository plantaRepository;
    private final BajaMaterialVegetalRepository bajaMaterialVegetalRepository;
    private final BodegaMapper bodegaMapper;

    @Override
    @Transactional
    public BajaMaterialResponse registrarBajaMaterial(Long bodegaMaterialId, RegistrarBajaMaterialRequest request) {
        BodegaMaterialVegetal material = bodegaRepository.findById(bodegaMaterialId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El material vegetal indicado no existe en bodega"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            validarAccesoComunidad(material.getComunidad().getId(), authentication);
        }

        if (request.cantidadBaja().compareTo(material.getCantidadDisponible()) > 0) {
            throw new ReglaNegocioException("La cantidad a dar de baja supera el stock disponible en bodega");
        }

        material.setCantidadDisponible(material.getCantidadDisponible().subtract(request.cantidadBaja()));
        bodegaRepository.save(material);

        BajaMaterialVegetal baja = new BajaMaterialVegetal();
        baja.setBodegaMaterial(material);
        baja.setCantidadBaja(request.cantidadBaja());
        baja.setCausa(request.causaFracaso());
        baja.setObservacion(request.observacion());

        BajaMaterialVegetal guardada = bajaMaterialVegetalRepository.save(baja);
        return bodegaMapper.toBajaMaterialResponse(guardada);
    }

    @Override
    @Transactional
    public MaterialVegetalResponse registrarIngresoMaterial(Long comunidadId, RegistrarMaterialVegetalRequest request) {
        Comunidad comunidad = comunidadRepository.findById(comunidadId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La comunidad indicada no existe"));

        Planta planta = plantaRepository.findByIdAndActivaTrue(request.plantaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("La planta seleccionada no existe en el catálogo"));

        BodegaMaterialVegetal material = new BodegaMaterialVegetal();
        material.setComunidad(comunidad);
        material.setPlanta(planta);
        material.setTipoMaterial(request.tipoMaterial());
        material.setCantidadDisponible(request.cantidad());
        material.setUnidadMedida(request.unidadMedida());
        material.setLoteOrigen(request.loteOrigen());
        material.setFechaIngreso(request.fechaIngreso());
        material.setActivo(true);

        BodegaMaterialVegetal guardado = bodegaRepository.save(material);
        return bodegaMapper.toMaterialVegetalResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public Long obtenerComunidadIdPorCorreo(String correo) {
        return comunidadRepository.findByResponsableCorreo(correo)
                .map(Comunidad::getId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una comunidad asociada al usuario"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialVegetalResponse> listarMaterialPorComunidad(Long comunidadId) {
        List<BodegaMaterialVegetal> materiales = bodegaRepository
                .findByComunidadIdAndActivoTrueOrderByPlantaNombreComunAsc(comunidadId);
        return bodegaMapper.toListMaterialVegetalResponse(materiales);
    }

    @Override
    @Transactional(readOnly = true)
    public void validarAccesoComunidad(Long comunidadId, Authentication authentication) {
        if (authentication == null) {
            throw new AccessDeniedException("No autenticado");
        }
        boolean esTecnico = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TECNICO_MSP"));
        validarAccesoComunidad(comunidadId, authentication.getName(), esTecnico);
    }

    @Override
    @Transactional(readOnly = true)
    public void validarAccesoComunidad(Long comunidadId, String correo, boolean esTecnico) {
        if (esTecnico) {
            return;
        }
        Long comunidadAsignadaId = obtenerComunidadIdPorCorreo(correo);
        if (!comunidadAsignadaId.equals(comunidadId)) {
            throw new AccessDeniedException("No tiene permisos para consultar el inventario de otra comunidad");
        }
    }
}

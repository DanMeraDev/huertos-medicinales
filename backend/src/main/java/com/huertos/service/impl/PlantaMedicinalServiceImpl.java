package com.huertos.service.impl;

import com.huertos.dto.CrearPlantaRequest;
import com.huertos.dto.PlantaResponse;
import com.huertos.exception.ConflictoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.mapper.PlantaMedicinalMapper;
import com.huertos.model.PlantaMedicinal;
import com.huertos.repository.PlantaMedicinalRepository;
import com.huertos.service.AlmacenamientoFotoService;
import com.huertos.service.PlantaMedicinalService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PlantaMedicinalServiceImpl implements PlantaMedicinalService {

    private static final long TAMANO_MAXIMO_FOTO = 2L * 1024 * 1024;
    private static final Set<String> TIPOS_FOTO = Set.of(MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE);
    private static final Set<String> EXTENSIONES_FOTO = Set.of("jpg", "jpeg", "png");

    private final PlantaMedicinalRepository plantaMedicinalRepository;
    private final PlantaMedicinalMapper plantaMedicinalMapper;
    private final AlmacenamientoFotoService almacenamientoFotoService;
    private final Clock clock;

    @Override
    @Transactional
    public PlantaResponse registrar(CrearPlantaRequest datos, MultipartFile foto) {
        String nombreCientifico = datos.nombreCientifico().strip();
        validarNombreCientificoUnico(nombreCientifico);
        validarFoto(foto);
        PlantaMedicinal planta = plantaMedicinalMapper.aEntidad(datos);
        planta.setNombreCientifico(nombreCientifico);
        planta.setFotoUrl(almacenamientoFotoService.guardar(foto));
        planta.setCreadoEn(LocalDateTime.now(clock));
        return plantaMedicinalMapper.aResponse(plantaMedicinalRepository.save(planta));
    }

    private void validarNombreCientificoUnico(String nombreCientifico) {
        if (plantaMedicinalRepository.existsByNombreCientificoIgnoreCase(nombreCientifico)) {
            throw new ConflictoException("Ya existe una planta registrada con ese nombre científico");
        }
    }

    private void validarFoto(MultipartFile foto) {
        if (foto == null || foto.isEmpty() || !TIPOS_FOTO.contains(foto.getContentType())
                || !tieneExtensionPermitida(foto) || foto.getSize() > TAMANO_MAXIMO_FOTO) {
            throw new ReglaNegocioException(MENSAJE_FOTO_INVALIDA);
        }
    }

    private boolean tieneExtensionPermitida(MultipartFile foto) {
        String extension = StringUtils.getFilenameExtension(foto.getOriginalFilename());
        return extension != null && EXTENSIONES_FOTO.contains(extension.toLowerCase(Locale.ROOT));
    }
}

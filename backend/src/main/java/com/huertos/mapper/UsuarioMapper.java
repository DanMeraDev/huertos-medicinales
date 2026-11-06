package com.huertos.mapper;

import com.huertos.dto.UsuarioResponse;
import com.huertos.model.Usuario;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    UsuarioResponse aResponse(Usuario usuario);
}

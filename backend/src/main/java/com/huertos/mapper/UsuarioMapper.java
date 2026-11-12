package com.huertos.mapper;

import com.huertos.dto.CrearUsuarioRequest;
import com.huertos.dto.LoginResponse;
import com.huertos.dto.UsuarioDetalleResponse;
import com.huertos.model.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "token", source = "token")
    @Mapping(target = "tipoToken", constant = "Bearer")
    LoginResponse toLoginResponse(Usuario usuario, String token);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", source = "passwordHash")
    @Mapping(target = "activo", constant = "true")
    @Mapping(target = "requiereCambioClave", constant = "true")
    @Mapping(target = "creadoEn", ignore = true)
    Usuario toUsuario(CrearUsuarioRequest request, String passwordHash);

    UsuarioDetalleResponse toUsuarioDetalleResponse(Usuario usuario);
}

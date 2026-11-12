package com.huertos.mapper;

import com.huertos.dto.LoginResponse;
import com.huertos.model.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "token", source = "token")
    @Mapping(target = "tipoToken", constant = "Bearer")
    LoginResponse toLoginResponse(Usuario usuario, String token);
}

package com.huertos.mapper;

import com.huertos.dto.CalendarioLunarResponse;
import com.huertos.model.CalendarioLunar;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CalendarioLunarMapper {

    CalendarioLunarResponse toCalendarioLunarResponse(CalendarioLunar entidad);
}

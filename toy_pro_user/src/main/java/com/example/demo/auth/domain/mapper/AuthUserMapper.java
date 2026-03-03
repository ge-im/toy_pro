package com.example.demo.auth.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.example.demo.auth.api.dto.AuthUserResponseDTO;
import com.example.demo.auth.domain.model.AuthUser;

@Mapper(componentModel = "spring")
public interface AuthUserMapper {
	AuthUserMapper INSTANCE = Mappers.getMapper(AuthUserMapper.class);
	
	AuthUserResponseDTO toResponse(AuthUser entity);
	
}

package com.example.demo.role.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.example.demo.role.api.dto.RoleCreateRequestDTO;
import com.example.demo.role.api.dto.RoleResponseDTO;
import com.example.demo.role.api.dto.RoleUpdateRequestDTO;
import com.example.demo.role.domain.model.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {
	RoleMapper INSTANCE = Mappers.getMapper(RoleMapper.class);
	
	RoleResponseDTO toResponse(Role roleEntity);
	
	Role toEntity(RoleCreateRequestDTO dto);
	
	Role toEntity(RoleUpdateRequestDTO dto);
	
	
}

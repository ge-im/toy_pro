package com.example.demo.user.domain.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import com.example.demo.user.api.dto.UserCreateRequestDTO;
import com.example.demo.user.api.dto.UserResponseDTO;
import com.example.demo.user.api.dto.UserUpdateRequestDTO;
import com.example.demo.user.domain.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
	UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
	
	UserResponseDTO toResponse(User userEntity);
	
	User toEntity(UserCreateRequestDTO dto);
	
	User toEntity(UserUpdateRequestDTO dto);
	
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UserUpdateRequestDTO dto, @MappingTarget User entity);

}

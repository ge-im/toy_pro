package com.example.demo.post.domain.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import com.example.demo.post.api.dto.PostCreateRequestDTO;
import com.example.demo.post.api.dto.PostResponseDTO;
import com.example.demo.post.api.dto.PostUpdateRequestDTO;
import com.example.demo.post.domain.model.Post;

@Mapper(componentModel = "spring")
public interface PostMapper {
	PostMapper INSTANCE = Mappers.getMapper(PostMapper.class);
	
	PostResponseDTO toResponse(Post entity);
	
	Post toEntity(PostCreateRequestDTO dto);
	
	Post toEntity(PostUpdateRequestDTO dto);
	
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	void updateEntityFromDto(PostUpdateRequestDTO dto, @MappingTarget Post entity);
	
}

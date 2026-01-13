package com.example.demo.comment.domain.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import com.example.demo.comment.api.dto.CommentCreateRequestDTO;
import com.example.demo.comment.api.dto.CommentResponseDTO;
import com.example.demo.comment.api.dto.CommentUpdateRequestDTO;
import com.example.demo.comment.domain.model.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {
	CommentMapper INSTANCE = Mappers.getMapper(CommentMapper.class);
	
	CommentResponseDTO toResponse(Comment entity);
	
	Comment toEntity(CommentCreateRequestDTO dto);
	
	Comment toEntity(CommentUpdateRequestDTO dto);
	
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	void updateEntityFromDto(CommentUpdateRequestDTO dto, @MappingTarget Comment entity);
}

package com.example.demo.comment.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.demo.comment.api.dto.CommentCreateRequestDTO;
import com.example.demo.comment.api.dto.CommentResponseDTO;
import com.example.demo.comment.api.dto.CommentSearchRequestDTO;
import com.example.demo.comment.api.dto.CommentUpdateRequestDTO;
import com.example.demo.comment.domain.mapper.CommentMapper;
import com.example.demo.comment.domain.model.Comment;
import com.example.demo.comment.domain.repository.CommentCustomRepository;
import com.example.demo.comment.domain.repository.CommentRepository;
import com.example.demo.common.dto.PageableDTO;
import com.example.demo.common.dto.SearchDTO;
import com.example.demo.exception.ObjectNotFoundException;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class CommentService {
	
	private final CommentRepository repository;
	private final CommentCustomRepository customRepository;
	private final CommentMapper mapper;
	
	public Flux<CommentResponseDTO> findAllByCondition(SearchDTO<CommentSearchRequestDTO> dto) {
		return null;
	}
	
	public Flux<CommentResponseDTO> findAllByPostSn(long postSn, PageableDTO page) {
		return repository.findAllByPostSn(postSn, page.getSize(), page.getOffset())
						 .map(mapper::toResponse);
	}
	
	public Flux<CommentResponseDTO> findAllByUserSn(long userSn, PageableDTO page, LocalDateTime startRegDt, LocalDateTime endRegDt) {
		return customRepository.findAllByUserSn(userSn, page, startRegDt, endRegDt)
							   .map(mapper::toResponse);
	}
	
	//commentSn만 return
	public Mono<Long> create(CommentCreateRequestDTO dto) {
		Comment entity = new Comment();
		entity.setDelYn("N");
		entity.setRegDt(LocalDateTime.now());
		entity.setUpdtDt(LocalDateTime.now());
		
		return repository.save(entity)
						 .map(r -> r.getPostSn());
	}
	
	public Mono<Void> update(CommentUpdateRequestDTO dto) {
		return repository.findById(dto.commentSn())
						 .filter(p -> "N".equals(p.getDelYn()))
						 .switchIfEmpty(Mono.error(new ObjectNotFoundException()))
						 .flatMap(p -> {
							 mapper.updateEntityFromDto(dto, p);
							 p.setUpdtDt(LocalDateTime.now());
							 return repository.save(p);
						 })
						 .then();
	}
	
	public Mono<Void> delete(long commentSn) {
		return repository.findById(commentSn)
						 .filter(p -> "N".equals(p.getDelYn()))
						 .switchIfEmpty(Mono.error(new ObjectNotFoundException()))
						 .flatMap(p -> {
							 p.setDelYn("Y");
							 p.setUpdtDt(LocalDateTime.now());
							 return repository.save(p);
						 })
						 .then();
	}
	
}

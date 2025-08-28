package com.example.demo.post.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.demo.common.DTO.PageableDTO;
import com.example.demo.common.DTO.SearchDTO;
import com.example.demo.exception.ObjectNotFoundException;
import com.example.demo.post.api.dto.PostCreateRequestDTO;
import com.example.demo.post.api.dto.PostResponseDTO;
import com.example.demo.post.api.dto.PostSearchRequestDTO;
import com.example.demo.post.api.dto.PostUpdateRequestDTO;
import com.example.demo.post.domain.mapper.PostMapper;
import com.example.demo.post.domain.model.Post;
import com.example.demo.post.domain.repository.PostCustomRepository;
import com.example.demo.post.domain.repository.PostRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PostService {
	private final PostRepository repository;
	private final PostCustomRepository customRepository;
	private final PostMapper postMapper;
	
	public Flux<PostResponseDTO> findAll(String title, String userNm, PageableDTO pageDTO) {
		return repository.findAll(title, userNm, pageDTO.getSize(), pageDTO.getOffset())
						 .map(postMapper::toResponse);
	}
	
	public Mono<PostResponseDTO> findPostById(long postSn) {
		return repository.findPostById(postSn)
						 .filter(p -> "N".equals(p.getDelYn()))
						 .switchIfEmpty(Mono.error(new ObjectNotFoundException()))
						 .map(postMapper::toResponse);
	}
	
	public Mono<Void> increaseViewCount(long postSn) {
		return repository.increaseViewCount(postSn)
						 .flatMap(rows -> {
							 if (rows == 0) 
								 return Mono.error(new ObjectNotFoundException("post not found"));
							 else if (rows > 1) 
								 return Mono.error(new IllegalStateException("Unexpected update"));
							 else 
								 return Mono.empty();
						 });
	}

	public Flux<PostResponseDTO> findAllByContditions (SearchDTO<PostSearchRequestDTO> dto) {
		return customRepository.findAllByConditions(dto)
							   .map(postMapper::toResponse);
	}
	
	public Mono<PostResponseDTO> create(PostCreateRequestDTO dto) {
		Post p = postMapper.toEntity(dto);
		p.setDelYn("N");
		p.setRegDt(LocalDateTime.now());
		p.setUpdtDt(LocalDateTime.now());
		
		return repository.save(p)
						 .map(postMapper::toResponse);
	}
	
	public Mono<PostResponseDTO> update(PostUpdateRequestDTO dto) {
		return repository.findById(dto.postSn())
						 .filter(p -> "N".equals(p.getDelYn()))
						 .switchIfEmpty(Mono.error(new ObjectNotFoundException()))
						 .flatMap(p -> {
							 postMapper.updateEntityFromDto(dto, p);
							 p.setUpdtDt(LocalDateTime.now());
							 return repository.save(p);
						 })
						 .map(postMapper::toResponse);
	}
	
	public Mono<Void> delete(long postSn) {
		return repository.findById(postSn)
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

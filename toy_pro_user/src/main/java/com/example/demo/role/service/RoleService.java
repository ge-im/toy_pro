package com.example.demo.role.service;

import org.springframework.stereotype.Service;

import com.example.demo.common.error.code.BusinessErrorCode;
import com.example.demo.common.error.exception.BusinessException;
import com.example.demo.role.api.dto.RoleCreateRequestDTO;
import com.example.demo.role.api.dto.RoleResponseDTO;
import com.example.demo.role.api.dto.RoleUpdateRequestDTO;
import com.example.demo.role.domain.mapper.RoleMapper;
import com.example.demo.role.domain.repository.RoleRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class RoleService {
	
	private final RoleRepository repository;
	private final RoleMapper mapper;
	
	public Flux<RoleResponseDTO> findAll() {
		return repository.findAll().map(mapper::toResponse);
	}
	
	public Mono<RoleResponseDTO> findById(long roleSn) {
		return repository.findById(roleSn)
						 .switchIfEmpty(Mono.error(new BusinessException(BusinessErrorCode.OBJECT_NOT_FOUND)))
						 .map(mapper::toResponse);
	}
	
	public Mono<Long> create(RoleCreateRequestDTO dto) {
		return repository.save(mapper.toEntity(dto))
						 .map(r -> r.getRoleSn());
	}
	
	public Mono<Void> update(RoleUpdateRequestDTO dto) {
		return repository.findById(dto.roleSn())
						 .switchIfEmpty(Mono.error(new BusinessException(BusinessErrorCode.OBJECT_NOT_FOUND)))
						 .flatMap(p -> {
							 return repository.save(mapper.toEntity(dto));
						 })
						 .then();
	}
	
	public Mono<Void> delete(long roleSn) {
		return repository.findById(roleSn)
						 .switchIfEmpty(Mono.error(new BusinessException(BusinessErrorCode.OBJECT_NOT_FOUND)))
						 .flatMap(repository::delete)
						 .then();
	}
}

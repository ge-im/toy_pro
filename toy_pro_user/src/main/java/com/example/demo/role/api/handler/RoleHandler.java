package com.example.demo.role.api.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.demo.role.api.dto.RoleCreateRequestDTO;
import com.example.demo.role.api.dto.RoleResponseDTO;
import com.example.demo.role.api.dto.RoleUpdateRequestDTO;
import com.example.demo.role.service.RoleService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class RoleHandler {

	private final RoleService service;
	
	public Mono<ServerResponse> findAll(ServerRequest request) {
		return ServerResponse.ok()
					.body(service.findAll(), RoleResponseDTO.class);
	}
	
	public Mono<ServerResponse> findById(ServerRequest request) {
		long roleSn = Long.parseLong(request.pathVariable("roleSn"));
		return ServerResponse.ok()
					.body(service.findById(roleSn), RoleResponseDTO.class);
	}
	
	public Mono<ServerResponse> create(ServerRequest request) {
		return request.bodyToMono(RoleCreateRequestDTO.class)
					  .flatMap(service::create)
					  .flatMap(ServerResponse.ok()::bodyValue);
	}
	
	public Mono<ServerResponse> update(ServerRequest request) {
		return request.bodyToMono(RoleUpdateRequestDTO.class)
					  .flatMap(service::update)
					  .then(ServerResponse.noContent().build());
	}
	
	public Mono<ServerResponse> delete(ServerRequest request) {
		long roleSn = Long.parseLong(request.pathVariable("roleSn"));
		return service.delete(roleSn)
					  .then(ServerResponse.noContent().build());
	}
}

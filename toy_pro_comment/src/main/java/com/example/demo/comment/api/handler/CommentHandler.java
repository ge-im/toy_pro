package com.example.demo.comment.api.handler;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.demo.comment.api.dto.CommentCreateRequestDTO;
import com.example.demo.comment.api.dto.CommentResponseDTO;
import com.example.demo.comment.api.dto.CommentUpdateRequestDTO;
import com.example.demo.comment.service.CommentService;
import com.example.demo.common.dto.PageableDTO;
import com.example.demo.util.RequestParameterUtil;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class CommentHandler {
	
	private final CommentService service;
	
	public Mono<ServerResponse> findByPostSn(ServerRequest request) {
		long postSn = Long.parseLong(request.pathVariable("postSn"));
		PageableDTO page = RequestParameterUtil.getPageableDTO(request);
		
		return ServerResponse.ok()
					.body(service.findAllByPostSn(postSn, page), CommentResponseDTO.class);
	}
	
	public Mono<ServerResponse> findByUserSn(ServerRequest request) {
		long userSn = Long.parseLong(request.pathVariable("userSn"));
		LocalDateTime startRegDt = RequestParameterUtil.getQueryParamAsDateTime(request, "startRegDt");
		LocalDateTime endRegDt = RequestParameterUtil.getQueryParamAsDateTime(request, "endRegDt");
		PageableDTO page = RequestParameterUtil.getPageableDTO(request);
		
		return ServerResponse.ok()
							 .body(service.findAllByUserSn(userSn, page, startRegDt, endRegDt), CommentResponseDTO.class);
	}
	
	public Mono<ServerResponse> create(ServerRequest request) {
		return request.bodyToMono(CommentCreateRequestDTO.class)
					  .flatMap(service::create)
					  .flatMap(ServerResponse.ok()::bodyValue);
	}
	
	public Mono<ServerResponse> update(ServerRequest request) {
		return request.bodyToMono(CommentUpdateRequestDTO.class)
					  .flatMap(service::update)
					  .flatMap(ServerResponse.ok()::bodyValue);
	}
	
	public Mono<ServerResponse> delete(ServerRequest request) {
		long commentSn = Long.parseLong(request.pathVariable("commentSn"));
		
		return service.delete(commentSn)
					  .then(ServerResponse.noContent().build());
	}
}

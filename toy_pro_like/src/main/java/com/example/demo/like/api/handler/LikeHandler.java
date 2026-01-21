package com.example.demo.like.api.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.demo.like.service.LikeService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class LikeHandler {
	
	private final LikeService service;
	
	public Mono<ServerResponse> checkLike(ServerRequest request) {
		String targetType = request.pathVariable("targetType");
		long targetSn = Long.parseLong(request.pathVariable("targetSn"));
		//추후 security에서 정보 꺼내오기로 변경
		long userSn = Long.parseLong(request.pathVariable("userSn"));
		
		return ServerResponse.ok()
				.body(service.checkLike(targetType, targetSn, userSn), Boolean.class);
	}
	
	public Mono<ServerResponse> countLike(ServerRequest request) {
		String targetType = request.pathVariable("targetType");
		long targetSn = Long.parseLong(request.pathVariable("targetSn"));
		
		return ServerResponse.ok()
				.body(service.countLike(targetType, targetSn), Long.class);
	}
	
	public Mono<ServerResponse> findUserIdsByLikes(ServerRequest request) {
		String targetType = request.pathVariable("targetType");
		long targetSn = Long.parseLong(request.pathVariable("targetSn"));
		
		return ServerResponse.ok()
				.body(service.findUserIdsByLikes(targetType, targetSn), Long.class);
	}
	
	public Mono<ServerResponse> doLike(ServerRequest request) {
		String targetType = request.pathVariable("targetType");
		long targetSn = Long.parseLong(request.pathVariable("targetSn"));
		//추후 security에서 정보 꺼내오기로 변경
		long userSn = Long.parseLong(request.pathVariable("userSn"));
		
		return service.doLike(targetType, targetSn, userSn)
				.then(ServerResponse.noContent().build());
	}
	
	public Mono<ServerResponse> undoLike(ServerRequest request) {
		String targetType = request.pathVariable("targetType");
		long targetSn = Long.parseLong(request.pathVariable("targetSn"));
		//추후 security에서 정보 꺼내오기로 변경
		long userSn = Long.parseLong(request.pathVariable("userSn"));
		
		return service.undoLike(targetType, targetSn, userSn)
				.then(ServerResponse.noContent().build());
	}
	
}

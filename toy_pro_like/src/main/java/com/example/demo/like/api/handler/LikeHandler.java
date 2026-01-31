package com.example.demo.like.api.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.demo.like.enums.TargetType;
import com.example.demo.like.service.LikeService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class LikeHandler {
	
	private final LikeService service;
	
	public Mono<ServerResponse> isLiked(ServerRequest request) {
		TargetType targetType = TargetType.from(request.pathVariable("targetType"));
		long targetSn = Long.parseLong(request.pathVariable("targetSn"));
		//추후 security에서 정보 꺼내오기로 변경
		long userSn = Long.parseLong(request.pathVariable("userSn"));
		
		return ServerResponse.ok()
				.body(service.isLiked(targetType, targetSn, userSn), Boolean.class);
	}
	
	public Mono<ServerResponse> countLikes(ServerRequest request) {
		TargetType targetType = TargetType.from(request.pathVariable("targetType"));
		long targetSn = Long.parseLong(request.pathVariable("targetSn"));
		
		return ServerResponse.ok()
				.body(service.countLikes(targetType, targetSn), Long.class);
	}
	
	public Mono<ServerResponse> findIsLikedUsers(ServerRequest request) {
		TargetType targetType = TargetType.from(request.pathVariable("targetType"));
		long targetSn = Long.parseLong(request.pathVariable("targetSn"));
		
		return ServerResponse.ok()
				.body(service.findIsLikedUsers(targetType, targetSn), Long.class);
	}
	
	public Mono<ServerResponse> like(ServerRequest request) {
		TargetType targetType = TargetType.from(request.pathVariable("targetType"));
		long targetSn = Long.parseLong(request.pathVariable("targetSn"));
		//추후 security에서 정보 꺼내오기로 변경
		long userSn = Long.parseLong(request.pathVariable("userSn"));
		
		return service.like(targetType, targetSn, userSn)
				.then(ServerResponse.noContent().build());
	}
	
	public Mono<ServerResponse> unLike(ServerRequest request) {
		TargetType targetType = TargetType.from(request.pathVariable("targetType"));
		long targetSn = Long.parseLong(request.pathVariable("targetSn"));
		//추후 security에서 정보 꺼내오기로 변경
		long userSn = Long.parseLong(request.pathVariable("userSn"));
		
		return service.unLike(targetType, targetSn, userSn)
				.then(ServerResponse.noContent().build());
	}
	
}

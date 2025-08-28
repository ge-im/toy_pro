package com.example.demo.post.api.handler;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.demo.common.DTO.PageableDTO;
import com.example.demo.common.DTO.SearchDTO;
import com.example.demo.post.api.dto.PostCreateRequestDTO;
import com.example.demo.post.api.dto.PostResponseDTO;
import com.example.demo.post.api.dto.PostSearchRequestDTO;
import com.example.demo.post.api.dto.PostUpdateRequestDTO;
import com.example.demo.post.service.PostService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class PostHandler {
	
	private final PostService postService;
	
	//(제목, 사용자 이름) 조건만 들어간 조회 [GET]
	public Mono<ServerResponse> findAll(ServerRequest request) {
		String title = request.queryParam("title").orElse(null);
		String userNm = request.queryParam("userNm").orElse(null);
		PageableDTO pageDTO = new PageableDTO(Integer.parseInt(request.queryParam("page").orElse("0")), 
				Integer.parseInt(request.queryParam("size").orElse("20")));
		return ServerResponse.ok()
					.contentType(MediaType.APPLICATION_JSON)
					.body(postService.findAll(title, userNm, pageDTO), PostResponseDTO.class);
	}
	
	//정렬조건, 게시글 관련 항목 전체 조건이 들어간 조회 [POST]
	public Mono<ServerResponse> searchPost(ServerRequest request) {
		return request.bodyToMono(new ParameterizedTypeReference<SearchDTO<PostSearchRequestDTO>>() {})
					  .flatMap(dto -> {
						  return ServerResponse.ok()
								  .contentType(MediaType.APPLICATION_JSON)
								  .body(postService.findAllByContditions(dto), PostResponseDTO.class);
					  });
	}
	
	public Mono<ServerResponse> findPostById(ServerRequest request) {
		long postSn = Long.parseLong(request.pathVariable("postSn"));
		return postService.findPostById(postSn)
						  .flatMap(ServerResponse.ok()::bodyValue);
	}
	
	public Mono<ServerResponse> increaseViewCount(ServerRequest request) {
		long postSn = Long.parseLong(request.pathVariable("postSn"));
		return postService.increaseViewCount(postSn)
						  .then(ServerResponse.noContent().build());
	}
	
	public Mono<ServerResponse> create(ServerRequest request) {
		return request.bodyToMono(PostCreateRequestDTO.class)
					  .flatMap(postService::create)
					  .flatMap(ServerResponse.ok()::bodyValue);
	}
	
	public Mono<ServerResponse> update(ServerRequest request) {
		return request.bodyToMono(PostUpdateRequestDTO.class)
					  .flatMap(postService::update)
					  .flatMap(ServerResponse.ok()::bodyValue);
	}
	
	public Mono<ServerResponse> delete(ServerRequest request) {
		long postSn = Long.parseLong(request.pathVariable("postSn"));
		return postService.delete(postSn)
						  .then(ServerResponse.noContent().build());
	}
}

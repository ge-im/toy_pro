package com.example.demo.user.api.handler;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.demo.common.dto.PageableDTO;
import com.example.demo.common.dto.SearchDTO;
import com.example.demo.user.api.dto.UserCreateRequestDTO;
import com.example.demo.user.api.dto.UserResponseDTO;
import com.example.demo.user.api.dto.UserSearchRequestDTO;
import com.example.demo.user.api.dto.UserUpdateRequestDTO;
import com.example.demo.user.service.UserService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserHandler {
	
	private final UserService userService;
	
	//간단한 텍스트 조건만 들어간 조회 [GET]
	public Mono<ServerResponse> findAll(ServerRequest request) {
		//공용으로 searchDTO를 return 하는 util method를 만들어서 적용시키기
		String userNm = request.queryParam("userNm").orElse(null);
		PageableDTO pageDTO = new PageableDTO(Integer.parseInt(request.queryParam("page").orElse("0")), 
									Integer.parseInt(request.queryParam("size").orElse("20")));
		return ServerResponse.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(userService.findAll(userNm, pageDTO), UserResponseDTO.class);
	}
	
	//정렬조건등 복잡한 조건이 들어간 조회 [POST]
	public Mono<ServerResponse>	searchUsers(ServerRequest request) {
		return request.bodyToMono(new ParameterizedTypeReference<SearchDTO<UserSearchRequestDTO>>() {})
					  .flatMap(dto -> {
					  		return ServerResponse.ok()
					  				.contentType(MediaType.APPLICATION_JSON)
					  				.body(userService.findAllByConditions(dto), UserResponseDTO.class);
					  });
	}
	
	public Mono<ServerResponse> findById(ServerRequest request) {
		Long userSn = Long.parseLong(request.pathVariable("userSn"));
		return userService.findById(userSn).flatMap(ServerResponse.ok()::bodyValue);
	}
	
	public Mono<ServerResponse> create(ServerRequest request) {
		return request.bodyToMono(UserCreateRequestDTO.class)
					.flatMap(userService::create)
					.flatMap(ServerResponse.ok()::bodyValue);
	}
	
	public Mono<ServerResponse> update(ServerRequest request) {
		return request.bodyToMono(UserUpdateRequestDTO.class)
					.flatMap(userService::update)
					.flatMap(ServerResponse.ok()::bodyValue);
	}
	
	public Mono<ServerResponse> delete(ServerRequest request) {
		Long userSn = Long.parseLong(request.pathVariable("userSn"));
		return userService.delete(userSn).then(ServerResponse.noContent().build());
	}
}

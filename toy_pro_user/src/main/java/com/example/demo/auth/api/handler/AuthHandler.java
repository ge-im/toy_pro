package com.example.demo.auth.api.handler;

import java.time.Duration;

import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.demo.auth.api.dto.AuthUserRequestDTO;
import com.example.demo.auth.security.JwtProperties;
import com.example.demo.auth.service.AuthService;
import com.example.demo.common.error.code.BusinessErrorCode;
import com.example.demo.common.error.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class AuthHandler {
	
	private final String REFRESH_TOKEN = "refreshToken"; 
	private final AuthService service;
	private final JwtProperties jwtProperties;
	
	public Mono<ServerResponse> login(ServerRequest request) {
		return request.bodyToMono(AuthUserRequestDTO.class)
					.flatMap(service::login)
					.flatMap(itr ->
						ServerResponse.ok()
							.cookie(
								setCookie(
									REFRESH_TOKEN, 
									itr.refreshToken(), 
									jwtProperties.expireTime().refreshDate()
								)
							)
							.bodyValue(itr.response())
					);
	}
	
	public Mono<ServerResponse> reissueToken(ServerRequest request) {
		return service.reissue(resolveRefreshToken(request))
					.flatMap(itr ->
						ServerResponse.ok()
							.cookie(
								setCookie(
									REFRESH_TOKEN, 
									itr.refreshToken(), 
									jwtProperties.expireTime().refreshDate()
								)
							)
							.bodyValue(itr.response())
					);
	}
	
	public Mono<ServerResponse> logout(ServerRequest request) {
		return service.logout(
					resolveAccessToken(request), 
					resolveRefreshToken(request)
				)
				.then(ServerResponse.ok()
					.cookie(setCookie(REFRESH_TOKEN, "", 0))
					.build()
				);
	}
	
	private ResponseCookie setCookie(String name, String value, long maxAge) {
		return ResponseCookie.from(name, value)
							 .httpOnly(true)
							 .path("/")
							 .maxAge(Duration.ofDays(maxAge))
							 .build();
	}
	
	private String resolveAccessToken(ServerRequest request) {
		String header = request.headers().firstHeader(HttpHeaders.AUTHORIZATION);

		if (header == null || !header.startsWith("Bearer ")) 
		    throw new BusinessException(BusinessErrorCode.INVALID_ACCESS);
		
		return header.substring(7);
	}
	
	private String resolveRefreshToken(ServerRequest request) {
		HttpCookie refreshTokenCookie = request.cookies().getFirst("refreshToken");
		
		if(refreshTokenCookie == null) 
		    throw new BusinessException(BusinessErrorCode.INVALID_ACCESS);
		
		return refreshTokenCookie.getValue();
	}
}

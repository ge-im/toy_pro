package com.example.demo.auth.api.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.demo.auth.api.handler.AuthHandler;
import com.example.demo.auth.api.dto.AuthUserRequestDTO;
import com.example.demo.auth.api.dto.TokenResponseDTO;

import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Configuration
public class AuthRouter {
	
	@Bean
	@RouterOperations({
		@RouterOperation(
			path = "/auth/login", method = RequestMethod.POST,
			beanClass = AuthHandler.class, beanMethod = "login",
			operation = @Operation(
				operationId = "login", tags = "Auth", summary = "로그인",
				requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AuthUserRequestDTO.class))),
				responses = @ApiResponse(responseCode = "200", description = "액세스 토큰과 사용자 정보", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TokenResponseDTO.class)))
			)
		),
		@RouterOperation(
			path = "/auth/reissue", method = RequestMethod.POST,
			beanClass = AuthHandler.class, beanMethod = "reissueToken",
			operation = @Operation(
				operationId = "reissueToken", tags = "Auth", summary = "토큰 재발급",
				parameters = @Parameter(name = "refreshToken", in = ParameterIn.COOKIE, required = true, description = "리프레시 토큰 쿠키"),
				responses = {
					@ApiResponse(responseCode = "200", description = "재발급된 액세스 토큰과 사용자 정보", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TokenResponseDTO.class))),
					@ApiResponse(responseCode = "401", description = "리프레시 토큰을 사용할 수 없음")
				}
			)
		),
		@RouterOperation(
			path = "/auth/logout", method = RequestMethod.POST,
			beanClass = AuthHandler.class, beanMethod = "logout",
			operation = @Operation(
				operationId = "logout", tags = "Auth", summary = "로그아웃",
				parameters = {
					@Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer 액세스 토큰"),
					@Parameter(name = "refreshToken", in = ParameterIn.COOKIE, required = true, description = "리프레시 토큰 쿠키")
				},
				responses = {
					@ApiResponse(responseCode = "200", description = "로그아웃 완료"),
					@ApiResponse(responseCode = "401", description = "액세스 또는 리프레시 토큰을 사용할 수 없음")
				}
			)
		)
	})
	RouterFunction<ServerResponse> authRoutes(AuthHandler handler) {
		return RouterFunctions.route()
				.path("/auth", builder -> builder
						.POST("/login", handler::login)
						.POST("/reissue", handler::reissueToken)
						.POST("/logout", handler::logout)
				)
				.build();
	}
}

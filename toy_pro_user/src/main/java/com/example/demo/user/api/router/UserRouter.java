package com.example.demo.user.api.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.demo.user.api.handler.UserHandler;
import com.example.demo.common.dto.SearchDTO;
import com.example.demo.user.api.dto.UserCreateRequestDTO;
import com.example.demo.user.api.dto.UserResponseDTO;
import com.example.demo.user.api.dto.UserUpdateRequestDTO;

import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Configuration
public class UserRouter {

    @Bean
	@RouterOperations({
		@RouterOperation(
			path = "/users/{userSn}", method = RequestMethod.GET,
			beanClass = UserHandler.class, beanMethod = "findById",
			operation = @Operation(
				operationId = "findUserById", tags = "User", summary = "사용자 단건 조회",
				parameters = @Parameter(name = "userSn", in = ParameterIn.PATH, required = true, description = "사용자 식별자", schema = @Schema(type = "integer", format = "int64")),
				responses = @ApiResponse(responseCode = "200", description = "사용자", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserResponseDTO.class)))
			)
		),
		@RouterOperation(
			path = "/users", method = RequestMethod.GET,
			beanClass = UserHandler.class, beanMethod = "findAll",
			operation = @Operation(
				operationId = "findUsers", tags = "User", summary = "사용자 목록 조회",
				description = "userNm, page, size 쿼리 파라미터로 사용자 목록을 조회합니다.",
				parameters = {
					@Parameter(name = "userNm", in = ParameterIn.QUERY, description = "사용자 이름 조건"),
					@Parameter(name = "page", in = ParameterIn.QUERY, description = "페이지 번호", schema = @Schema(type = "integer", defaultValue = "0")),
					@Parameter(name = "size", in = ParameterIn.QUERY, description = "페이지 크기", schema = @Schema(type = "integer", defaultValue = "20"))
				},
				responses = @ApiResponse(responseCode = "200", description = "사용자 목록", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = UserResponseDTO.class))))
			)
		),
		@RouterOperation(
			path = "/users/search", method = RequestMethod.POST,
			beanClass = UserHandler.class, beanMethod = "searchUsers",
			operation = @Operation(
				operationId = "searchUsers", tags = "User", summary = "사용자 조건 검색",
				description = "검색 조건, 페이지, 정렬 조건을 request body로 받아 사용자 목록을 조회합니다.",
				requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SearchDTO.class))),
				responses = @ApiResponse(responseCode = "200", description = "사용자 목록", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = UserResponseDTO.class))))
			)
		),
		@RouterOperation(
			path = "/users", method = RequestMethod.POST,
			beanClass = UserHandler.class, beanMethod = "create",
			operation = @Operation(
				operationId = "createUser", tags = "User", summary = "사용자 생성",
				requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserCreateRequestDTO.class))),
				responses = @ApiResponse(responseCode = "200", description = "생성된 사용자", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserResponseDTO.class)))
			)
		),
		@RouterOperation(
			path = "/users", method = RequestMethod.PUT,
			beanClass = UserHandler.class, beanMethod = "update",
			operation = @Operation(
				operationId = "updateUser", tags = "User", summary = "사용자 수정",
				requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserUpdateRequestDTO.class))),
				responses = @ApiResponse(responseCode = "204", description = "사용자 수정 완료")
			)
		),
		@RouterOperation(
			path = "/users", method = RequestMethod.DELETE,
			beanClass = UserHandler.class, beanMethod = "delete",
			operation = @Operation(
				operationId = "deleteUser", tags = "User", summary = "사용자 삭제",
				responses = @ApiResponse(responseCode = "204", description = "사용자 삭제 완료")
			)
		)
	})
    RouterFunction<ServerResponse> userRoutes(UserHandler handler) {
		/* 
		 * 여러 방법으로 구현 가능
//import static org.springframework.web.reactive.function.server.RequestPredicates.*;
		return RouterFunctions.route(GET("/users"), handler::search)
					.andRoute(GET("/users/{userSn}"), handler::findById)
					.andRoute(POST("/users"), handler::create)
					.andRoute(PUT("/users"), handler::update)
					.andRoute(DELETE("/users"), handler::delete);

		return RouterFunctions.route()
					.GET("/users", handler::search)
					.GET("/users/{userSn}", handler::findById)
					.POST("/users", handler::create)
					.PUT("/users", handler::update)
					.DELETE("/users", handler::delete)
					.build();
		 */
		return RouterFunctions.route()
					.path("/users", builder -> builder
							.GET("/{userSn}", handler::findById)
							.GET(handler::findAll)
							.POST("/search", handler::searchUsers)
							.POST(handler::create)
							.PUT(handler::update)
							.DELETE(handler::delete))
					.build();
	}

}

package com.example.demo.like.api.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.demo.like.api.handler.LikeHandler;

import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Configuration
public class LikeRouter {

	@Bean
	@RouterOperations({
		@RouterOperation(
			path = "/likes/{targetType}/{targetSn}/check/{userSn}", method = RequestMethod.GET,
			beanClass = LikeHandler.class, beanMethod = "isLiked",
			operation = @Operation(
				operationId = "isLiked", tags = "Like", summary = "좋아요 여부 조회",
				parameters = {
					@Parameter(name = "targetType", in = ParameterIn.PATH, required = true, description = "좋아요 대상 유형", schema = @Schema(type = "string", allowableValues = { "P", "C" })),
					@Parameter(name = "targetSn", in = ParameterIn.PATH, required = true, description = "좋아요 대상 식별자", schema = @Schema(type = "integer", format = "int64")),
					@Parameter(name = "userSn", in = ParameterIn.PATH, required = true, description = "사용자 식별자", schema = @Schema(type = "integer", format = "int64"))
				},
				responses = @ApiResponse(responseCode = "200", description = "좋아요 여부", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class)))
			)
		),
		@RouterOperation(
			path = "/likes/{targetType}/{targetSn}/count", method = RequestMethod.GET,
			beanClass = LikeHandler.class, beanMethod = "countLikes",
			operation = @Operation(
				operationId = "countLikes", tags = "Like", summary = "좋아요 수 조회",
				parameters = {
					@Parameter(name = "targetType", in = ParameterIn.PATH, required = true, description = "좋아요 대상 유형", schema = @Schema(type = "string", allowableValues = { "P", "C" })),
					@Parameter(name = "targetSn", in = ParameterIn.PATH, required = true, description = "좋아요 대상 식별자", schema = @Schema(type = "integer", format = "int64"))
				},
				responses = @ApiResponse(responseCode = "200", description = "좋아요 수", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "integer", format = "int64")))
			)
		),
		@RouterOperation(
			path = "/likes/{targetType}/{targetSn}/users", method = RequestMethod.GET,
			beanClass = LikeHandler.class, beanMethod = "findIsLikedUsers",
			operation = @Operation(
				operationId = "findLikedUsers", tags = "Like", summary = "좋아요 사용자 식별자 목록 조회",
				parameters = {
					@Parameter(name = "targetType", in = ParameterIn.PATH, required = true, description = "좋아요 대상 유형", schema = @Schema(type = "string", allowableValues = { "P", "C" })),
					@Parameter(name = "targetSn", in = ParameterIn.PATH, required = true, description = "좋아요 대상 식별자", schema = @Schema(type = "integer", format = "int64"))
				},
				responses = @ApiResponse(responseCode = "200", description = "사용자 식별자 목록", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(type = "integer", format = "int64"))))
			)
		),
		@RouterOperation(
			path = "/likes/{targetType}/{targetSn}/{userSn}", method = RequestMethod.POST,
			beanClass = LikeHandler.class, beanMethod = "like",
			operation = @Operation(
				operationId = "like", tags = "Like", summary = "좋아요 등록",
				parameters = {
					@Parameter(name = "targetType", in = ParameterIn.PATH, required = true, description = "좋아요 대상 유형", schema = @Schema(type = "string", allowableValues = { "P", "C" })),
					@Parameter(name = "targetSn", in = ParameterIn.PATH, required = true, description = "좋아요 대상 식별자", schema = @Schema(type = "integer", format = "int64")),
					@Parameter(name = "userSn", in = ParameterIn.PATH, required = true, description = "사용자 식별자", schema = @Schema(type = "integer", format = "int64"))
				},
				responses = {
					@ApiResponse(responseCode = "204", description = "좋아요 등록 완료"),
					@ApiResponse(responseCode = "409", description = "이미 좋아요가 존재함")
				}
			)
		),
		@RouterOperation(
			path = "/likes/{targetType}/{targetSn}/{userSn}", method = RequestMethod.DELETE,
			beanClass = LikeHandler.class, beanMethod = "unLike",
			operation = @Operation(
				operationId = "unlike", tags = "Like", summary = "좋아요 취소",
				parameters = {
					@Parameter(name = "targetType", in = ParameterIn.PATH, required = true, description = "좋아요 대상 유형", schema = @Schema(type = "string", allowableValues = { "P", "C" })),
					@Parameter(name = "targetSn", in = ParameterIn.PATH, required = true, description = "좋아요 대상 식별자", schema = @Schema(type = "integer", format = "int64")),
					@Parameter(name = "userSn", in = ParameterIn.PATH, required = true, description = "사용자 식별자", schema = @Schema(type = "integer", format = "int64"))
				},
				responses = {
					@ApiResponse(responseCode = "204", description = "좋아요 취소 완료"),
					@ApiResponse(responseCode = "400", description = "좋아요 이력을 찾을 수 없음")
				}
			)
		)
	})
	RouterFunction<ServerResponse> likeRoutes(LikeHandler handler) {
		//userSn은 security 설정 이후 모두 제거
		return RouterFunctions.route()
					.path("/likes/{targetType}/{targetSn}", builder -> builder
							.GET("/check/{userSn}", handler::isLiked)//특정 컨텐츠+유저의 좋아요 여부
							.GET("/count", handler::countLikes)//특정 컨텐츠의 좋아요 수
							.GET("/users", handler::findIsLikedUsers)//특정 컨텐츠의 좋아요 유저ID 목록
							.POST("/{userSn}", handler::like)//좋아요 do
							.DELETE("/{userSn}", handler::unLike))//좋아요 undo
					.build();
	}
}

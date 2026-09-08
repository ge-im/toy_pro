package com.example.demo.comment.api.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.demo.comment.api.handler.CommentHandler;
import com.example.demo.comment.api.dto.CommentCreateRequestDTO;
import com.example.demo.comment.api.dto.CommentResponseDTO;
import com.example.demo.comment.api.dto.CommentUpdateRequestDTO;

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
public class CommentRouter {
	
	@Bean
	@RouterOperations({
		@RouterOperation(
			path = "/comment/{postSn}", method = RequestMethod.GET,
			beanClass = CommentHandler.class, beanMethod = "findByPostSn",
			operation = @Operation(
				operationId = "findCommentsByPostSn", tags = "Comment",
				summary = "게시글별 댓글 목록 조회",
				description = "postSn, page, size를 기준으로 댓글 목록을 조회합니다.",
				parameters = {
					@Parameter(name = "postSn", in = ParameterIn.PATH, required = true, description = "게시글 식별자", schema = @Schema(type = "integer", format = "int64")),
					@Parameter(name = "page", in = ParameterIn.QUERY, description = "페이지 번호", schema = @Schema(type = "integer", defaultValue = "0")),
					@Parameter(name = "size", in = ParameterIn.QUERY, description = "페이지 크기", schema = @Schema(type = "integer", defaultValue = "20"))
				},
				responses = @ApiResponse(responseCode = "200", description = "댓글 목록", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = CommentResponseDTO.class))))
			)
		),
		@RouterOperation(
			path = "/comment/user/{userSn}", method = RequestMethod.GET,
			beanClass = CommentHandler.class, beanMethod = "findByUserSn",
			operation = @Operation(
				operationId = "findCommentsByUserSn", tags = "Comment",
				summary = "사용자별 댓글 목록 조회",
				description = "userSn, startRegDt, endRegDt, page, size를 기준으로 댓글 목록을 조회합니다.",
				parameters = {
					@Parameter(name = "userSn", in = ParameterIn.PATH, required = true, description = "사용자 식별자", schema = @Schema(type = "integer", format = "int64")),
					@Parameter(name = "startRegDt", in = ParameterIn.QUERY, description = "등록 일시 시작 조건", schema = @Schema(type = "string", format = "date-time")),
					@Parameter(name = "endRegDt", in = ParameterIn.QUERY, description = "등록 일시 종료 조건", schema = @Schema(type = "string", format = "date-time")),
					@Parameter(name = "page", in = ParameterIn.QUERY, description = "페이지 번호", schema = @Schema(type = "integer", defaultValue = "0")),
					@Parameter(name = "size", in = ParameterIn.QUERY, description = "페이지 크기", schema = @Schema(type = "integer", defaultValue = "20"))
				},
				responses = @ApiResponse(responseCode = "200", description = "댓글 목록", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = CommentResponseDTO.class))))
			)
		),
		@RouterOperation(
			path = "/comment", method = RequestMethod.POST,
			beanClass = CommentHandler.class, beanMethod = "create",
			operation = @Operation(
				operationId = "createComment", tags = "Comment", summary = "댓글 생성",
				requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommentCreateRequestDTO.class))),
				responses = @ApiResponse(responseCode = "200", description = "저장 결과 값", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "integer", format = "int64")))
			)
		),
		@RouterOperation(
			path = "/comment", method = RequestMethod.PUT,
			beanClass = CommentHandler.class, beanMethod = "update",
			operation = @Operation(
				operationId = "updateComment", tags = "Comment", summary = "댓글 수정",
				requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommentUpdateRequestDTO.class))),
				responses = @ApiResponse(responseCode = "200", description = "댓글 수정 처리 결과")
			)
		),
		@RouterOperation(
			path = "/comment/{commentSn}", method = RequestMethod.DELETE,
			beanClass = CommentHandler.class, beanMethod = "delete",
			operation = @Operation(
				operationId = "deleteComment", tags = "Comment", summary = "댓글 삭제",
				parameters = @Parameter(name = "commentSn", in = ParameterIn.PATH, required = true, description = "댓글 식별자", schema = @Schema(type = "integer", format = "int64")),
				responses = @ApiResponse(responseCode = "204", description = "댓글 삭제 완료")
			)
		)
	})
	RouterFunction<ServerResponse> commentRoutes(CommentHandler handler) {
		return RouterFunctions.route()
					.path("/comment", builder -> builder
							.GET("/{postSn}", handler::findByPostSn)
							.GET("/user/{userSn}", handler::findByUserSn)
							.POST(handler::create)
							.PUT(handler::update)
							.DELETE("/{commentSn}", handler::delete))
					.build();
	}
}

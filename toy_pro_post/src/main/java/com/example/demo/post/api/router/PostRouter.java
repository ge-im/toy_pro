package com.example.demo.post.api.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.demo.common.dto.SearchDTO;
import com.example.demo.post.api.dto.PostCreateRequestDTO;
import com.example.demo.post.api.dto.PostResponseDTO;
import com.example.demo.post.api.dto.PostUpdateRequestDTO;
import com.example.demo.post.api.handler.PostHandler;

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
public class PostRouter {
	
	@Bean
	@RouterOperations({
		@RouterOperation(
			path = "/post",
			method = RequestMethod.GET,
			beanClass = PostHandler.class,
			beanMethod = "findAll",
			operation = @Operation(
				operationId = "findPosts",
				tags = "Post",
				summary = "게시글 목록 조회",
				description = "title, userNm, page, size 쿼리 파라미터로 게시글 목록을 조회합니다.",
				parameters = {
					@Parameter(name = "title", in = ParameterIn.QUERY, description = "게시글 제목 조건"),
					@Parameter(name = "userNm", in = ParameterIn.QUERY, description = "사용자 이름 조건"),
					@Parameter(name = "page", in = ParameterIn.QUERY, description = "페이지 번호", schema = @Schema(type = "integer", defaultValue = "0")),
					@Parameter(name = "size", in = ParameterIn.QUERY, description = "페이지 크기", schema = @Schema(type = "integer", defaultValue = "20"))
				},
				responses = @ApiResponse(
					responseCode = "200",
					description = "게시글 목록",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
						array = @ArraySchema(schema = @Schema(implementation = PostResponseDTO.class)))
				)
			)
		),
		@RouterOperation(
			path = "/post/{postSn}",
			method = RequestMethod.GET,
			beanClass = PostHandler.class,
			beanMethod = "findPostById",
			operation = @Operation(
				operationId = "findPostById",
				tags = "Post",
				summary = "게시글 단건 조회",
				parameters = @Parameter(name = "postSn", in = ParameterIn.PATH, required = true, description = "게시글 식별자", schema = @Schema(type = "integer", format = "int64")),
				responses = {
					@ApiResponse(responseCode = "200", description = "게시글", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PostResponseDTO.class))),
					@ApiResponse(responseCode = "400", description = "게시글을 찾을 수 없음")
				}
			)
		),
		@RouterOperation(
			path = "/post/search",
			method = RequestMethod.POST,
			beanClass = PostHandler.class,
			beanMethod = "searchPost",
			operation = @Operation(
				operationId = "searchPosts",
				tags = "Post",
				summary = "게시글 조건 검색",
				description = "검색 조건, 페이지, 정렬 조건을 request body로 받아 게시글 목록을 조회합니다.",
				requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SearchDTO.class))),
				responses = @ApiResponse(responseCode = "200", description = "게시글 목록", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = PostResponseDTO.class))))
			)
		),
		@RouterOperation(
			path = "/post/{postSn}/views",
			method = RequestMethod.POST,
			beanClass = PostHandler.class,
			beanMethod = "increaseViewCount",
			operation = @Operation(
				operationId = "increasePostViewCount",
				tags = "Post",
				summary = "게시글 조회 수 증가",
				parameters = @Parameter(name = "postSn", in = ParameterIn.PATH, required = true, description = "게시글 식별자", schema = @Schema(type = "integer", format = "int64")),
				responses = @ApiResponse(responseCode = "204", description = "조회 수 증가 완료")
			)
		),
		@RouterOperation(
			path = "/post",
			method = RequestMethod.POST,
			beanClass = PostHandler.class,
			beanMethod = "create",
			operation = @Operation(
				operationId = "createPost",
				tags = "Post",
				summary = "게시글 생성",
				requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PostCreateRequestDTO.class))),
				responses = @ApiResponse(responseCode = "200", description = "생성된 게시글", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PostResponseDTO.class)))
			)
		),
		@RouterOperation(
			path = "/post",
			method = RequestMethod.PUT,
			beanClass = PostHandler.class,
			beanMethod = "update",
			operation = @Operation(
				operationId = "updatePost",
				tags = "Post",
				summary = "게시글 수정",
				requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PostUpdateRequestDTO.class))),
				responses = {
					@ApiResponse(responseCode = "200", description = "수정된 게시글", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PostResponseDTO.class))),
					@ApiResponse(responseCode = "400", description = "게시글을 찾을 수 없음")
				}
			)
		),
		@RouterOperation(
			path = "/post/{postSn}",
			method = RequestMethod.DELETE,
			beanClass = PostHandler.class,
			beanMethod = "delete",
			operation = @Operation(
				operationId = "deletePost",
				tags = "Post",
				summary = "게시글 삭제",
				parameters = @Parameter(name = "postSn", in = ParameterIn.PATH, required = true, description = "게시글 식별자", schema = @Schema(type = "integer", format = "int64")),
				responses = {
					@ApiResponse(responseCode = "204", description = "게시글 삭제 완료"),
					@ApiResponse(responseCode = "400", description = "게시글을 찾을 수 없음")
				}
			)
		)
	})
	RouterFunction<ServerResponse>	postRoutes(PostHandler handler) {
		return RouterFunctions.route()
					.path("/post", builder -> builder
							.GET(handler::findAll)
							.GET("/{postSn}", handler::findPostById)
							.POST("/search", handler::searchPost)
							.POST("/{postSn}/views", handler::increaseViewCount)
							.POST(handler::create)
							.PUT(handler::update)
							.DELETE("/{postSn}", handler::delete))
					.build();
	}
}

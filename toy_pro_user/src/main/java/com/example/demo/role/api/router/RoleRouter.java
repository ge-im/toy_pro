package com.example.demo.role.api.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.demo.role.api.handler.RoleHandler;
import com.example.demo.role.api.dto.RoleCreateRequestDTO;
import com.example.demo.role.api.dto.RoleResponseDTO;
import com.example.demo.role.api.dto.RoleUpdateRequestDTO;

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
public class RoleRouter {
	
	@Bean
	@RouterOperations({
		@RouterOperation(
			path = "/role/{roleSn}", method = RequestMethod.GET,
			beanClass = RoleHandler.class, beanMethod = "findById",
			operation = @Operation(
				operationId = "findRoleById", tags = "Role", summary = "역할 단건 조회",
				parameters = @Parameter(name = "roleSn", in = ParameterIn.PATH, required = true, description = "역할 식별자", schema = @Schema(type = "integer", format = "int64")),
				responses = @ApiResponse(responseCode = "200", description = "역할", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RoleResponseDTO.class)))
			)
		),
		@RouterOperation(
			path = "/role", method = RequestMethod.GET,
			beanClass = RoleHandler.class, beanMethod = "findAll",
			operation = @Operation(
				operationId = "findRoles", tags = "Role", summary = "역할 목록 조회",
				responses = @ApiResponse(responseCode = "200", description = "역할 목록", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = RoleResponseDTO.class))))
			)
		),
		@RouterOperation(
			path = "/role", method = RequestMethod.POST,
			beanClass = RoleHandler.class, beanMethod = "create",
			operation = @Operation(
				operationId = "createRole", tags = "Role", summary = "역할 생성",
				requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RoleCreateRequestDTO.class))),
				responses = @ApiResponse(responseCode = "200", description = "생성된 역할", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RoleResponseDTO.class)))
			)
		),
		@RouterOperation(
			path = "/role", method = RequestMethod.PUT,
			beanClass = RoleHandler.class, beanMethod = "update",
			operation = @Operation(
				operationId = "updateRole", tags = "Role", summary = "역할 수정",
				requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RoleUpdateRequestDTO.class))),
				responses = @ApiResponse(responseCode = "204", description = "역할 수정 완료")
			)
		),
		@RouterOperation(
			path = "/role/{roleSn}", method = RequestMethod.DELETE,
			beanClass = RoleHandler.class, beanMethod = "delete",
			operation = @Operation(
				operationId = "deleteRole", tags = "Role", summary = "역할 삭제",
				parameters = @Parameter(name = "roleSn", in = ParameterIn.PATH, required = true, description = "역할 식별자", schema = @Schema(type = "integer", format = "int64")),
				responses = @ApiResponse(responseCode = "204", description = "역할 삭제 완료")
			)
		)
	})
	RouterFunction<ServerResponse>	roleRoutes(RoleHandler handler) {
		return RouterFunctions.route()
					.path("/role", builder -> builder
							.GET("/{roleSn}", handler::findById)
							.GET(handler::findAll)
							.POST(handler::create)
							.PUT(handler::update)
							.DELETE("/{roleSn}", handler::delete))
					.build();
	}
}

package com.example.demo.config.security;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.example.demo.common.error.code.ErrorCode;
import com.example.demo.config.error.ErrorResponseFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Writes Security errors in the same format as the global error handler.
 */
@Component
@RequiredArgsConstructor
public class SecurityErrorResponseWriter {

	private final ObjectMapper objectMapper;
	private final ErrorResponseFactory errorResponseFactory;

	public Mono<Void> write(ServerWebExchange exchange, ErrorCode errorCode) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(errorCode.getHttpStatus());
		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

		Map<String, Object> body = errorResponseFactory.create(
			errorCode,
			errorCode.getMessage(),
			exchange.getRequest().getPath().value()
		);

		try {
			byte[] bytes = objectMapper.writeValueAsBytes(body);
			return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
		} catch (JsonProcessingException e) {
			return Mono.error(e);
		}
	}
}

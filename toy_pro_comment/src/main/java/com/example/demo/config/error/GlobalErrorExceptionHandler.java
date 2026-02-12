package com.example.demo.config.error;

import java.util.Map;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class GlobalErrorExceptionHandler extends AbstractErrorWebExceptionHandler {

	public GlobalErrorExceptionHandler(GlobalErrorAttributes errorAttributes, 
			ApplicationContext applicationContext, ServerCodecConfigurer codecConfigurer) {
		super(errorAttributes, new WebProperties().getResources(), applicationContext);
		super.setMessageReaders(codecConfigurer.getReaders());
		super.setMessageWriters(codecConfigurer.getWriters());
	}

	@Override
	protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
		return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
	}
	
	private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
		Map<String, Object> errorProperties = getErrorAttributes(request, ErrorAttributeOptions.defaults());
		
		int status = (int) errorProperties.getOrDefault("status", 500);
		
		return ServerResponse.status(status)
							 .contentType(MediaType.APPLICATION_JSON)
							 .bodyValue(errorProperties);
	}

}

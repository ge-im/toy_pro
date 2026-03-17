package com.example.demo.auth.api.router;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.demo.auth.api.handler.AuthHandler;

@Configuration
public class AuthRouter {
	
	
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

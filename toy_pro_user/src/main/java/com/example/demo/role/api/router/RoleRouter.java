package com.example.demo.role.api.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.demo.role.api.handler.RoleHandler;

@Configuration
public class RoleRouter {
	
	@Bean
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

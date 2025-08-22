package com.example.demo.user.api.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.demo.user.api.handler.UserHandler;

@Configuration
public class UserRouter {

    @Bean
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
							.GET(handler::findAll)
							.POST("/search", handler::searchUsers)
							.GET("/{userSn}", handler::findById)
							.POST(handler::create)
							.PUT(handler::update)
							.DELETE(handler::delete))
					.build();
	}

}

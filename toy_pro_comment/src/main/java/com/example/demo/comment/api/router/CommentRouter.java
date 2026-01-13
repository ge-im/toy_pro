package com.example.demo.comment.api.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.demo.comment.api.handler.CommentHandler;

@Configuration
public class CommentRouter {
	
	@Bean
	RouterFunction<ServerResponse> commentRouter(CommentHandler handler) {
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

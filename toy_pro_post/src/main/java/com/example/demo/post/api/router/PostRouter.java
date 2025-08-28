package com.example.demo.post.api.router;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.demo.post.api.handler.PostHandler;

@Configuration
public class PostRouter {
	
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

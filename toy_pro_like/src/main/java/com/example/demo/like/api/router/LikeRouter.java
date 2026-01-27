package com.example.demo.like.api.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.demo.like.api.handler.LikeHandler;

@Configuration
public class LikeRouter {

	@Bean
	RouterFunction<ServerResponse> likeRoutes(LikeHandler handler) {
		//userSn은 security 설정 이후 모두 제거
		return RouterFunctions.route()
					.path("/likes/{targetType}/{targetSn}", builder -> builder
							.GET("/check/{userSn}", handler::isLiked)//특정 컨텐츠+유저의 좋아요 여부
							.GET("/count", handler::countLikes)//특정 컨텐츠의 좋아요 수
							.GET("/users", handler::findIsLikedUsers)//특정 컨텐츠의 좋아요 유저ID 목록
							.POST("/{userSn}", handler::like)//좋아요 do
							.DELETE("/{userSn}", handler::unLike))//좋아요 undo
					.build();
	}
}

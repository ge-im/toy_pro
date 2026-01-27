package com.example.demo.like.domain.repository;

import org.springframework.stereotype.Repository;

import com.example.demo.infra.redis.executor.ReactiveStringRedisExecutor;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Repository
public class LikeRedisRepository {
	
	private final ReactiveStringRedisExecutor redisExecutor;
	
	public Mono<Long> countLikes(String key) {
		return redisExecutor.ssize(key);
	}
	
	public Mono<Boolean> isLiked(String key, long userSn) {
		return redisExecutor.sisMember(key, String.valueOf(userSn));
	}
	
	public Flux<Long> findIsLikedUsers(String key) {
		return redisExecutor.smembers(key)
							.map(e -> Long.parseLong(e));
	}
	
	public Mono<Boolean> addLike(String key, long userSn) {
		return redisExecutor.sadd(key, String.valueOf(userSn))
							.map(e -> e > 0);
	}
	
	public Mono<Boolean> removeLike(String key, long userSn) {
		return redisExecutor.sremove(key, String.valueOf(userSn))
							.map(e -> e > 0);
	}
}

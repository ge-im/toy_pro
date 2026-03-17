package com.example.demo.auth.infra.redis.repository;

import org.springframework.stereotype.Repository;

import com.example.demo.infra.redis.executor.ReactiveStringRedisExecutor;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Repository
public class TokenRedisRepository {
	private final ReactiveStringRedisExecutor redisExecutor;
	
	public Mono<Boolean> addRefreshToken(String key, String value, long durationMS) {
		return redisExecutor.vset(key, value, durationMS);
	}
	
	public Mono<String> findRefreshToken(String key) {
		return redisExecutor.vget(key);
	}
	
	public Mono<Boolean> removeRefreshToken(String key) {
		return redisExecutor.vremove(key);
	}

	public Mono<Boolean> removeRefreshTokenByPrefix(String key) {
		return redisExecutor.vremoveByPattern(key + "*")
							.map(r -> r > 0);
	}
	
	public Mono<Boolean> addAccessTokenBlacklist(String key, String value, long durationMS) {
		return redisExecutor.vset(key, value, durationMS);
	}
	
	public Mono<Boolean> existAccesTokenBlacklist(String key) {
		return redisExecutor.hasKey(key);
	}
}

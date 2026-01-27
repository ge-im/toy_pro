package com.example.demo.infra.redis.executor;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class ReactiveStringRedisExecutor {
	
	private final ReactiveStringRedisTemplate template;
	
	public Mono<Long> sadd(String key, String value) {
		return template.opsForSet().add(key, value);
	}
	
	public Mono<Long> sremove(String key, String value) {
		return template.opsForSet().remove(key, value);
	}
	
	public Flux<String> smembers(String key) {
		return template.opsForSet().members(key);
	}
	
	public Mono<Long> ssize(String key) {
		return template.opsForSet().size(key);
	}
	
	public Mono<Boolean> sisMember(String key, String value) {
		return template.opsForSet().isMember(key, value);
	}
}

package com.example.demo.infra.redis.executor;

import java.time.Duration;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class ReactiveStringRedisExecutor {
	
	private final ReactiveStringRedisTemplate template;
	
	public Mono<Boolean> vset(String key, String value, long durationMS) {
		return template.opsForValue().set(key, value, Duration.ofMillis(durationMS));
	}
	
	public Mono<String> vget(String key) {
		return template.opsForValue().get(key);
	}
	
	public Mono<Boolean> vremove(String key) {
		return template.opsForValue().delete(key);
	}
	
	public Mono<Long> vremoveByPattern(String pattern) {
		return template.scan(
							ScanOptions.scanOptions()
									   .match(pattern)
									   .count(100)
									   .build()
						)
						.flatMap(template::delete)
						.reduce(0L, Long::sum);
	}
	
	public Mono<Boolean> hasKey(String key) {
		return template.hasKey(key);
	}
	
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

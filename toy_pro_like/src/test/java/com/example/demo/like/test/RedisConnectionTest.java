package com.example.demo.like.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;

import reactor.core.publisher.Mono;

@SpringBootTest
public class RedisConnectionTest {
	@Autowired
    ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
	
	@Autowired
	RedisTemplate<String, String> redisTemplate;

    @Test
    void redis_connection_test() {
        Mono<Boolean> result =
                reactiveRedisTemplate
                        .opsForValue()
                        .set("test:key", "hello");

        Boolean success = result.block();

        System.out.println("Redis set result = " + success);
    }
    
    @Test
    void redis_get_set_test() {
        reactiveRedisTemplate.opsForValue()
                .set("test:key", "123")
                .then(
                    reactiveRedisTemplate.opsForValue().get("test:key")
                )
                .doOnNext(value -> {
                    System.out.println("value = " + value);
                })
                .block();
    }
    
    @Test
    void redis_blocking_template_test() {
        redisTemplate.opsForValue().set("block:key", "ok");
        String value = redisTemplate.opsForValue().get("block:key");

        System.out.println("blocking value = " + value);
    }


}

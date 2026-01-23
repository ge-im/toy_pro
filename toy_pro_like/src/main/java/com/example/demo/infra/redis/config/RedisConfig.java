package com.example.demo.infra.redis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class RedisConfig {

	@Value("${spring.data.redis.host}")
	private String host;
	
	@Value("${spring.data.redis.port}")
	private int port;
	
	//password 설정이 있는 경우
//	@Value("${spring.data.redis.password}")
//	private String password;
	
	@Bean
	RedisConnectionFactory redisConnectionFactory() {
		//host,port 이외의 config 설정 셋팅이 있는 경우 사용, 없는 경우 객체에 직접 parameter 입력 가능.
		RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
		//password 설정이 있는 경우
//		config.setPassword(password);
		
		return new LettuceConnectionFactory(config);
	}
	
	@Bean
	ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
		RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
		//password 설정이 있는 경우
//		config.setPassword(password);
		
		return new LettuceConnectionFactory(config); 
	}
	
	/**
	 * reactive(non-blocking) type template : StringSerializer template 
	 */
	@Bean
	ReactiveStringRedisTemplate reactiveStringRedisTemplate() {
		return new ReactiveStringRedisTemplate(reactiveRedisConnectionFactory());
	}
	
	/**
	 * reactive(non-blocking) type template : 객체 저장 위한 범용 template<k,v> 
	 */
	@Bean
	ReactiveRedisTemplate<String, Object> reactiveRedisTemplate() {
		//serializer 설정 위한 context
		RedisSerializationContext<String, Object> context = 
				RedisSerializationContext
					.<String, Object>newSerializationContext(new StringRedisSerializer())
					.value(getJacksonSerializer())
					.build();
		
		return new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory(), context);
	}
	
	/**
	 * blocking type template : StringSerializer template 
	 */
	@Bean
	StringRedisTemplate stringRedisTemplate() {
		return new StringRedisTemplate(redisConnectionFactory());
	}
	
	/**
	 * blocking type template : 객체 저장 위한 범용 template<k,v> 
	 */
	@Bean
	RedisTemplate<String, Object> redisTemplate() {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		
		redisTemplate.setConnectionFactory(redisConnectionFactory());
		
		//직렬화기 생성
		//string : key serializer에 사용
		StringRedisSerializer stringSerializer = new StringRedisSerializer();
		
		redisTemplate.setKeySerializer(stringSerializer);
		redisTemplate.setValueSerializer(getJacksonSerializer());
		redisTemplate.setHashKeySerializer(stringSerializer);
		redisTemplate.setHashValueSerializer(getJacksonSerializer());

		return redisTemplate;
	}
	
	private GenericJackson2JsonRedisSerializer getJacksonSerializer() {
		//jackson 다형성 타입 검증기생성
		PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator
				.builder()
				.allowIfSubType(Object.class)//역직렬화 혀용규칙 설정
				.build();
		/*mapper 설정 설명
		 * 1. 날짜 및 시간 ISO 8601 문자열 형식으로 직렬화
		 * 2. 알 수 없는 속성이 있어도 역직렬화 실패를 방지
		 * 3. 객체 타입 정보를 JSON에 포함, 역직렬화 시 복원할 수 있도록 활성화
		 *    typeValidator를 사용하여 허용되는 타입만 역직렬화하도록 제한
		 * 4. Java 8 날짜 및 시간 API (LocalDateTime 등)를 지원하는 모듈 등록
		 */
		ObjectMapper om = new ObjectMapper();
		om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
		.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
		.activateDefaultTyping(typeValidator, DefaultTyping.NON_FINAL_AND_ENUMS)
		.registerModule(new JavaTimeModule());
		
		//특정타입이 아닌 범용 사용을 위한 Serializer 
		return new GenericJackson2JsonRedisSerializer(om);
	}
}

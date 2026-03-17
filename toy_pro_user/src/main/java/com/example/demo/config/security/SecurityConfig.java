package com.example.demo.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import com.example.demo.auth.security.filter.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
	
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	 
	@Bean
	SecurityWebFilterChain springSecurityWebFilterChain(ServerHttpSecurity http) throws Exception {
		/* 공식 문서 예시
		http
        .authorizeExchange(exchanges -> exchanges
            .anyExchange().authenticated()
        )
        .httpBasic(withDefaults())
        .formLogin(withDefaults());
		
		http
		.authorizeExchange((authorize) -> authorize                          
			.pathMatchers("/resources/**", "/signup", "/about").permitAll()  
			.pathMatchers("/admin/**").hasRole("ADMIN")                      
			.pathMatchers("/db/**").access((authentication, context) ->      
				hasRole("ADMIN").check(authentication, context)
					.filter(decision -> !decision.isGranted())
					.switchIfEmpty(hasRole("DBA").check(authentication, context))
			)
			.anyExchange().denyAll()                                         
		);
		*/
		return http
		        .csrf(ServerHttpSecurity.CsrfSpec::disable) // 테스트용 CSRF 비활성화
		        .formLogin(ServerHttpSecurity.FormLoginSpec::disable) //security 기본 로그인 비활성
		        .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable) //security 기본 인증 비활성
		        .authorizeExchange(auth -> auth
		        		.pathMatchers("/auth/login", "/auth/reissue").permitAll()
		        		.pathMatchers("/error").permitAll()
		                .pathMatchers(HttpMethod.GET, "/**").hasAnyRole("ROLE_ADMIN", "ROLE_USER")
		                .pathMatchers(HttpMethod.POST, "/**").hasAnyRole("ROLE_ADMIN", "ROLE_USER")
		                .pathMatchers(HttpMethod.PUT, "/**").hasAnyRole("ROLE_ADMIN", "ROLE_USER")
		                .pathMatchers(HttpMethod.DELETE, "/**").hasAnyRole("ROLE_ADMIN")
		                .anyExchange().authenticated()
		        )
		        .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
		        .exceptionHandling(e -> e
		        	.authenticationEntryPoint((exchange, ex) ->
		        		Mono.fromRunnable(() -> exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED)))
		        	.accessDeniedHandler((exchange, ex) -> 
		        		Mono.fromRunnable(() -> exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN)))
		        )
		        .build();
	}
}

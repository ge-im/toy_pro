package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
	 
	@Bean
	SecurityWebFilterChain springSecurityWebFilterChain(ServerHttpSecurity http) throws Exception {
		http
	        .csrf(ServerHttpSecurity.CsrfSpec::disable) // 테스트용 CSRF 비활성화
	        .authorizeExchange(auth -> auth
	                .pathMatchers(HttpMethod.GET, "/**").permitAll()
	                .pathMatchers(HttpMethod.POST, "/**").permitAll()
	                .pathMatchers("/error").permitAll()
	                .anyExchange().authenticated()
	        );
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
		return http.build();
	}
}

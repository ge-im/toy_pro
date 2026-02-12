package com.example.demo.config.security;

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
		http.csrf(ServerHttpSecurity.CsrfSpec::disable)
			.authorizeExchange(auth -> auth
					.pathMatchers(HttpMethod.GET, "/**").permitAll()
					.pathMatchers(HttpMethod.POST, "/**").permitAll()
					.pathMatchers("/error").permitAll()
					.anyExchange().authenticated()
		);
		return http.build();
	}

}

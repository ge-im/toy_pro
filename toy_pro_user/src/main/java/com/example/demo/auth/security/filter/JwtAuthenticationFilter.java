package com.example.demo.auth.security.filter;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.example.demo.auth.security.JwtAuthenticationToken;
import com.example.demo.auth.security.JwtReactiveAuthenticationManager;

import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter extends AuthenticationWebFilter {

	public JwtAuthenticationFilter(JwtReactiveAuthenticationManager authenticationManager) {
		super(authenticationManager);
		setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/**"));
		setServerAuthenticationConverter(this::convert);
	}
	
	private Mono<Authentication> convert(ServerWebExchange exchange) {
		return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
					.filter(header -> header.startsWith("Bearer "))
					.map(header -> header.substring(7))
					.map(JwtAuthenticationToken::new);
	}
}

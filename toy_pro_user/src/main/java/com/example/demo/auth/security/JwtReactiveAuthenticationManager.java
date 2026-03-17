package com.example.demo.auth.security;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.example.demo.auth.infra.redis.TokenRedisKeyGenerator;
import com.example.demo.auth.infra.redis.repository.TokenRedisRepository;
import com.example.demo.common.error.code.BusinessErrorCode;
import com.example.demo.common.error.exception.BusinessException;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

	private final JwtProvider jwtProvider;
	private final TokenRedisRepository tokenRepository;
	private final TokenRedisKeyGenerator keyGenerator;
	
	@Override
	public Mono<Authentication> authenticate(Authentication authentication) {
		String token = (String) authentication.getCredentials();
		Claims claims = jwtProvider.parseToken(token);
		
		
		return Mono.just(claims)
				.filter(jwtProvider::validationToken)
				.map(jwtProvider::parseTokenId)
				.flatMap(jti ->
					tokenRepository.existAccesTokenBlacklist(keyGenerator.generateBlackListKey(jti))
						.flatMap(exists -> {
							if(exists)
								return Mono.error(new BusinessException(BusinessErrorCode.BLACKLISTED_TOKEN));
							return Mono.just(token);
						})
				)
				.map(t -> {
					List<String> roles = claims.get("roles", List.class);
					
					return new UsernamePasswordAuthenticationToken(
									claims.getSubject(),	//principal
									null, 					//credentials
									roles.stream()			//authorities
										 .map(SimpleGrantedAuthority::new)
										 .collect(Collectors.toList())
					);
				}); 
	}
}

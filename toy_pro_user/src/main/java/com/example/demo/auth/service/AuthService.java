package com.example.demo.auth.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.auth.api.dto.AuthUserRequestDTO;
import com.example.demo.auth.api.dto.InternalTokenResponseDTO;
import com.example.demo.auth.api.dto.TokenResponseDTO;
import com.example.demo.auth.domain.mapper.AuthUserMapper;
import com.example.demo.auth.domain.model.AuthUser;
import com.example.demo.auth.domain.repository.AuthRoleRepository;
import com.example.demo.auth.domain.repository.AuthUserRepository;
import com.example.demo.auth.infra.redis.TokenRedisKeyGenerator;
import com.example.demo.auth.infra.redis.repository.TokenRedisRepository;
import com.example.demo.auth.security.JwtProvider;
import com.example.demo.common.error.code.BusinessErrorCode;
import com.example.demo.common.error.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class AuthService {
	
	private final AuthUserRepository userRepository;
	private final AuthRoleRepository roleRepository;
	private final TokenRedisRepository tokenRedisRepository;
	private final TokenRedisKeyGenerator keyGenerator;
	private final AuthUserMapper mapper;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;
	
	public Mono<InternalTokenResponseDTO> login(AuthUserRequestDTO requestDTO) {
		return userRepository.findByLoginId(requestDTO.loginId())
					.switchIfEmpty(Mono.error(new BusinessException(BusinessErrorCode.OBJECT_NOT_FOUND)))
					.filter(user -> passwordEncoder.matches(requestDTO.loginPassword(), user.getUserPswd()))
					.switchIfEmpty(Mono.error(new BusinessException(BusinessErrorCode.INVALID_PASSWORD)))
					.flatMap(this::issueToken);
	}
	
	public Mono<InternalTokenResponseDTO> reissue(String refreshToken) {
		String userSn = jwtProvider.parseTokenSubject(refreshToken);
		String jti = jwtProvider.parseTokenId(refreshToken);
		String key = keyGenerator.generateRefreshTokenKey(userSn, jti);
		
		return tokenRedisRepository.findRefreshToken(key)
				.switchIfEmpty(Mono.error(new BusinessException(BusinessErrorCode.INVALID_TOKEN)))
				.filter(sn -> sn.equals(userSn))
				.switchIfEmpty(tokenRedisRepository
					.removeRefreshTokenByPrefix(keyGenerator.generateRefreshTokenKey(userSn))
					.then(
						Mono.error(new BusinessException(BusinessErrorCode.INVALID_ACCESS))
					)
				)
				.map(sn -> Long.valueOf(sn))
				.flatMap(userRepository::findById)
				.flatMap(authUser -> issueToken(authUser)
					.flatMap(tokenResponse -> 
						tokenRedisRepository.removeRefreshToken(key)
											.thenReturn(tokenResponse)
					)
				);
	}
	
	public Mono<Void> logout(String accessToken, String refreshToken) {
		return tokenRedisRepository
					.removeRefreshToken(
						keyGenerator.generateRefreshTokenKey(
							jwtProvider.parseTokenSubject(refreshToken), 
							jwtProvider.parseTokenId(refreshToken)
						)
					)
					.switchIfEmpty(Mono.error(new BusinessException(BusinessErrorCode.TOKEN_REMOVE_FAILED)))
					.flatMap(e ->
						tokenRedisRepository.addAccessTokenBlacklist(
							keyGenerator.generateBlackListKey(
								jwtProvider.parseTokenId(accessToken)
							), 
							"1", 
							jwtProvider.calculateTokenExpirationMS(accessToken)
						)
						.then()
					);
	}
	
	private Mono<InternalTokenResponseDTO> issueToken(AuthUser authUser) { 
		return roleRepository.findAllByUserId(authUser.getUserSn())
					.collectList()
					.flatMap(roles -> createToken(authUser, roles));
	}
	
	private Mono<InternalTokenResponseDTO> createToken(AuthUser authUser, List<String> roles) {
		String accessToken = jwtProvider.createAccessToken(authUser.getUserSn(), roles);
		String refreshToken = jwtProvider.createRefreshToken(authUser.getUserSn());

		String userSn = jwtProvider.parseTokenSubject(refreshToken);
		String jti = jwtProvider.parseTokenId(refreshToken);
		
		return tokenRedisRepository
					.addRefreshToken(
						keyGenerator.generateRefreshTokenKey(userSn, jti), 
						"1", 
						jwtProvider.getRefreshExpireMS()
					)
					.filter(saved -> saved)
					.switchIfEmpty(Mono.error(new BusinessException(BusinessErrorCode.TOKEN_SAVE_FAILED)))
					.thenReturn(createTokenResponse(authUser, accessToken , refreshToken));
	}
	
	private InternalTokenResponseDTO createTokenResponse(AuthUser authUser, String accessToken, String refreshToken) {
		return new InternalTokenResponseDTO(
					new TokenResponseDTO(accessToken, mapper.toResponse(authUser)), 
					refreshToken
				);
	}
}
	

package com.example.demo.auth.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.example.demo.common.error.code.BusinessErrorCode;
import com.example.demo.common.error.exception.BusinessException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtProvider {
	
	private final JwtProperties jwtProperties;
	private final SecretKey secretKey; //Key -> SecretKey로 변경(parsing할 때 지정 객체 주입 필요)
	
	public JwtProvider(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
		this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secretKey().getBytes(StandardCharsets.UTF_8));
	}
	
	public String createAccessToken(long userSn, List<String> roles) {
		return Jwts.builder()
				.claims()
				.subject(String.valueOf(userSn))
				.id(UUID.randomUUID().toString())
				.add("roles", roles)
				.add("type", "access")
				.issuedAt(new Date())
				.expiration(
					Date.from(
						Instant.now()
							.plus(jwtProperties.expireTime().accessMin(), ChronoUnit.MINUTES)
					)
				)
				.and()
				.signWith(secretKey) //key의 길이에 따라 자동으로 hs암호화를 찾는다는거 같음
				.compact();
	}

	public String createRefreshToken(long userSn) {
		return Jwts.builder()
				.claims()
				.subject(String.valueOf(userSn))
				.id(UUID.randomUUID().toString())
				.add("type", "refresh")
				.issuedAt(new Date())
				.expiration(
					Date.from(
						Instant.now()
							.plus(jwtProperties.expireTime().refreshDate(), ChronoUnit.DAYS)
					)
				)
				.and()
				.signWith(secretKey)
				.compact();
	}
	
	public Claims parseToken(String token) {
		try {
			return Jwts.parser()
					.verifyWith(secretKey)
					.build()
					.parseSignedClaims(token)
					.getPayload();
		} catch(ExpiredJwtException e) {
			throw new BusinessException(BusinessErrorCode.EXPIRED_TOKEN);
		} catch (JwtException e2) {
			throw new BusinessException(BusinessErrorCode.INVALID_TOKEN);
		}
	}

	public boolean validationToken(Claims claims) {
//		Claims claims = parseToken(token);
		List<String> roles = claims.get("roles", List.class);
		
		//roles 개수 체크가 필요한 경우는 정책에 따라 변경, 지금은 roles 있는지만 체크(token 발급시에는 무조건 발급하기 때문에)
		if(!"access".equals(claims.get("type"))
				|| !StringUtils.hasLength(claims.getSubject())
				|| roles == null)
			return false;
		
		return true;
	}
	
	public String parseTokenSubject(String token) {
		return parseToken(token).getSubject();
	}
	
	public String parseTokenId(String token) {
		return parseToken(token).getId();
	}
	
	public String parseTokenId(Claims claims) {
		return claims.getId();
	}
	
	public long calculateTokenExpirationMS(String token) {
		return parseToken(token).getExpiration().getTime()
				- System.currentTimeMillis();
	}
	
	public long getRefreshExpireMS() {
		return (long) jwtProperties.expireTime().refreshDate() * 24 * 60 * 60 * 1000;
	}
	
}

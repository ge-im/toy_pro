package com.example.demo.auth.infra.redis;

import org.springframework.stereotype.Component;

@Component
public class TokenRedisKeyGenerator {
	
	public String generateRefreshTokenKey(String userSn, String jti) {
		return "refresh_token:" + userSn + ":" + jti;
	}

	
	public String generateRefreshTokenKey(String userSn) {
		return "refresh_token:" + userSn + ":";
	}
	
	public String generateBlackListKey(String jti) {
		return "blacklist:access_token:" + jti;
	}
}

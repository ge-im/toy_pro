package com.example.demo.auth.infra.redis;

import org.springframework.stereotype.Component;

@Component
public class TokenRedisKeyGenerator {
	
	public String generateRefreshTokenKey(String userSn, String jti) {
		return "auth:refresh:" + userSn + ":" + jti;
	}

	
	public String generateRefreshTokenKey(String userSn) {
		return "auth:refresh:" + userSn + ":";
	}
	
	public String generateBlackListKey(String jti) {
		return "auth:blacklist:access:" + jti;
	}
}

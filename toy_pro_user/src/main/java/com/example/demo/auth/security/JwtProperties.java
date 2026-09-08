package com.example.demo.auth.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="jwt")
public record JwtProperties(
		String secretKey, 
		ExpireTime expireTime	
) {
	public record ExpireTime(int accessMin, int refreshDate) {}
}

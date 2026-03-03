package com.example.demo.auth.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="jwt")
public record JwtProperties(
		String secretKey, 
		ExpireTime expireTime	
) {
	public record ExpireTime(int accessMin, int refreshDate) {}
}

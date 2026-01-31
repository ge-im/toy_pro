package com.example.demo.like.infra.redis;

import org.springframework.stereotype.Component;

import com.example.demo.like.enums.TargetType;

@Component
public class LikeRedisKeyGenerator {
	
	public String generate(TargetType targetType, long targetSn) {
		return "like:" + targetType.getValue() + ":" + targetSn;
	}
	
}

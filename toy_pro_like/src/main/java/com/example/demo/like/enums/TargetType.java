package com.example.demo.like.enums;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TargetType {
	POST("P"), 
	COMMENT("C");
	
	private final String value;
	
	public static TargetType from(String value) {
		return Arrays.stream(values())
				.filter(e -> e.value.equals(value))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Illegal targetType"));
	}
	
}

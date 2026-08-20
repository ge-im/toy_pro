package com.example.demo.like.enums;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.example.demo.common.error.code.BusinessErrorCode;
import com.example.demo.common.error.exception.BusinessException;

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
				.orElseThrow(() -> new BusinessException(BusinessErrorCode.INVALID_REQUEST_PARAMETER));
	}
	
}

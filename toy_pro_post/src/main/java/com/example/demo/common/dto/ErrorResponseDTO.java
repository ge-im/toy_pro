package com.example.demo.common.dto;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDTO {
	private final int status;
	private final String code;
	private final String message;
	private final String time;
	private final String path;

	public static ErrorResponseDTO from(Map<String, Object> attributes) {
		return new ErrorResponseDTO(
			((Number) attributes.getOrDefault("status", 500)).intValue(),
			(String) attributes.getOrDefault("code", ""),
			(String) attributes.getOrDefault("message", "Unexpected error occurred"),
			(String) attributes.getOrDefault("time", ""),
			(String) attributes.getOrDefault("path", "")
		);
	}
}

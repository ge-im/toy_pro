package com.example.demo.config.error;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.example.demo.common.error.code.ErrorCode;

/**
 * Builds the application's common error response body.
 */
@Component
public class ErrorResponseFactory {

	public Map<String, Object> create(ErrorCode errorCode, String message, String path) {
		Map<String, Object> attributes = new LinkedHashMap<>();
		attributes.put("status", errorCode.getHttpStatus().value());
		attributes.put("code", errorCode.getCode());
		attributes.put("message", message);
		attributes.put("time", LocalDateTime.now().toString());
		attributes.put("path", path);
		return attributes;
	}
}

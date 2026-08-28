package com.example.demo.config.error;

import org.springframework.stereotype.Component;

import com.example.demo.common.dto.ErrorResponseDTO;
import com.example.demo.common.error.code.ErrorCode;

/**
 * Builds the application's common error response body.
 */
@Component
public class GlobalErrorResponseFactory {

	public ErrorResponseDTO create(ErrorCode errorCode, String message, String path) {
		return new ErrorResponseDTO(
			errorCode.getHttpStatus().value(),
			errorCode.getCode(),
			message,
			java.time.LocalDateTime.now().toString(),
			path
		);
	}
}

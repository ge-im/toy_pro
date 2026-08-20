package com.example.demo.common.error.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HttpErrorCode implements ErrorCode {
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication is required"),
	FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "Access is denied"),
	INTERVAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "", "Interval Server Error")
	;
	
	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}

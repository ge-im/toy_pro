package com.example.demo.common.error.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HttpErrorCode implements ErrorCode {
	INTERVAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "Internal Server Error")
	;
	
	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}

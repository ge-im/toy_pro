package com.example.demo.common.error.exception;

import org.springframework.http.HttpStatus;

import com.example.demo.common.error.code.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HttpErrorCode implements ErrorCode {
	INTERVAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "", "Interval Server Error")
	;
	
	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}

package com.example.demo.common.error.exception;

import com.example.demo.common.error.code.ErrorCode;

import lombok.Getter;

@Getter
public class HttpException extends RuntimeException {
	private final ErrorCode errorCode;
	
	public HttpException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}

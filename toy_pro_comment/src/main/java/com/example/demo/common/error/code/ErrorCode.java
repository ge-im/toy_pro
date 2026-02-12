package com.example.demo.common.error.code;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
	
	HttpStatus getHttpStatus();
	
	String getCode();
	
	String getMessage();
	
}

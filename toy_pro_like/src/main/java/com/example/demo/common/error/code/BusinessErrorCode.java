package com.example.demo.common.error.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BusinessErrorCode implements ErrorCode {
	//common not found object, 400
	OBJECT_NOT_FOUND(HttpStatus.BAD_REQUEST, "", "Object not found"),
	
	//like
	LIKE_ALREADY_EXISTS(HttpStatus.CONFLICT, "", "Like already exists")
	;
	
	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

}

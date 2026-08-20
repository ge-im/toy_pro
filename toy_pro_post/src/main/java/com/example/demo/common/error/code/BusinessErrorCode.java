package com.example.demo.common.error.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BusinessErrorCode implements ErrorCode {
	//common not found object, 400
	OBJECT_NOT_FOUND(HttpStatus.BAD_REQUEST, "OBJECT_NOT_FOUND", "Object not found"),
	INVALID_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, "INVALID_REQUEST_PARAMETER", "Invalid request parameter"),
	
	//like
	LIKE_ALREADY_EXISTS(HttpStatus.CONFLICT, "LIKE_ALREADY_EXISTS", "Like already exists"),
	INVALID_STATE(HttpStatus.CONFLICT, "INVALID_STATE", "Invalid resource state")
	;
	
	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

}

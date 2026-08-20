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
	
	//Authentication
	INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "INVALID_PASSWORD", "Invalid password"),
	INVALID_ACCESS(HttpStatus.UNAUTHORIZED, "INVALID_ACCESS", "Invalid access"),
	
	//Authentication - JWT
	EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_TOKEN","Expired Token"),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN","Invailed Token"),
	TOKEN_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "TOKEN_SAVE_FAILED","Fail to save the token"),
	TOKEN_REMOVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "TOKEN_REMOVE_FAILED", "Fail to logout"),
	BLACKLISTED_TOKEN(HttpStatus.BAD_REQUEST, "BLACKLISTED_TOKEN", "Already logout token")
	;
	
	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

}

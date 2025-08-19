package com.example.demo.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalErrorExceptionHandler {
	
	//@ExceptionHandler
	// 변경 예정 -->> ResponseEntityExceptionHandler
	
	@ExceptionHandler(Exception.class)
	public ErrorResponseDTO exceptionError(Exception e) {
		ErrorResponseDTO result = new ErrorResponseDTO();
		result.setCode(500);
		result.setMessage(e.getMessage());
		
		e.printStackTrace();
//		log.trace(e.getMessage());
		return result; 
	}
	
	@ExceptionHandler(RuntimeException.class)
	public ErrorResponseDTO runtimeExceptionError(RuntimeException e) {
		ErrorResponseDTO result = new ErrorResponseDTO();
		result.setCode(500);
		result.setMessage(e.getMessage());
		
		e.printStackTrace();
//		log.trace(e.getMessage());
		return result; 
	}
	
}

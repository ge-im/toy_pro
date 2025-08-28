package com.example.demo.exception.handler;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.demo.common.DTO.ErrorResponseDTO;

@RestControllerAdvice
public class GlobalErrorExceptionHandler {
	
	@ExceptionHandler(Exception.class)
	public ErrorResponseDTO exeptionError(Exception e) {
		ErrorResponseDTO result = new ErrorResponseDTO();
		result.setCode(500);
		result.setMessage(e.getMessage());
		
		e.printStackTrace();
		return result;
	}
	
	@ExceptionHandler(RuntimeException.class)
	public ErrorResponseDTO runtimeExeptionError(RuntimeException e) {
		ErrorResponseDTO result = new ErrorResponseDTO();
		result.setCode(500);
		result.setMessage(e.getMessage());
		
		e.printStackTrace();
		return result;
	}
	
}

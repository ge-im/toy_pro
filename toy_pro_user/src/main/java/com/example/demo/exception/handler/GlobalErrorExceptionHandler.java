package com.example.demo.exception.handler;

import java.time.LocalDateTime;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.NotAcceptableStatusException;
import org.springframework.web.server.ServerWebExchange;

import com.example.demo.common.dto.ErrorResponseDTO;
import com.example.demo.exception.ObjectNotFoundException;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalErrorExceptionHandler {
	
	/*
	e.printStackTrace();
	-> 프로필 지정
	-> log 추적으로 바꾸고 error로 해서 trace하기
	 */
	
	//error 전역 처리
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> exeptionError(Exception e) {
		e.printStackTrace();
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ErrorResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), LocalDateTime.now()));
	}
	
	//runtime error 전역 처리
	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<Object> runtimeExeptionError(RuntimeException e) {
		e.printStackTrace();
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ErrorResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), LocalDateTime.now()));
	}
	
	//사용자가 id를 잘못 보냈을 수도 있지만, db상으로 삭제될 수 도 있기 때문에 어떻게 내보낼지 고민 조금 더 해보기
	@ExceptionHandler(ObjectNotFoundException.class)
	public ResponseEntity<?> handleObjectNotFoundException(ObjectNotFoundException e) {
		e.printStackTrace();
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ErrorResponseDTO(HttpStatus.BAD_REQUEST.value(), e.getMessage(), LocalDateTime.now()));
	}
	
	@Override
	protected Mono<ResponseEntity<Object>> handleMethodNotAllowedException(MethodNotAllowedException ex,
			HttpHeaders headers, HttpStatusCode status, ServerWebExchange exchange) {
		ex.printStackTrace();
		return Mono.just(ResponseEntity.status(status.value())
				.body(new ErrorResponseDTO(status.value(), ex.getMessage(), LocalDateTime.now())));
	}
	
	@Override
	protected Mono<ResponseEntity<Object>> handleNotAcceptableStatusException(NotAcceptableStatusException ex,
			HttpHeaders headers, HttpStatusCode status, ServerWebExchange exchange) {
		ex.printStackTrace();
		return Mono.just(ResponseEntity.status(status.value())
				.body(new ErrorResponseDTO(status.value(), ex.getMessage(), LocalDateTime.now())));
	}
	
}

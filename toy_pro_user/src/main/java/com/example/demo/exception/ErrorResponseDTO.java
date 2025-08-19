package com.example.demo.exception;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDTO {
	private int code;
	private String status;
	private String message;
	private LocalDateTime time;
	private String stackTrace;
}

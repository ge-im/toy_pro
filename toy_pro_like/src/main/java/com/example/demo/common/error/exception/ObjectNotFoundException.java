package com.example.demo.common.error.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ObjectNotFoundException extends RuntimeException {
	
	public ObjectNotFoundException(String message) {
		super(message);
	}
	
}

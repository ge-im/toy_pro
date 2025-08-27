package com.example.demo.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ObjectNotFoundException extends Exception {
	
	public ObjectNotFoundException(String message) {
		super(message);
	}
	
}

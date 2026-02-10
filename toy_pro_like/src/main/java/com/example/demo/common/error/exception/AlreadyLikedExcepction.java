package com.example.demo.common.error.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AlreadyLikedExcepction extends RuntimeException {
	
	public AlreadyLikedExcepction(String message) {
		super(message);
	}
}

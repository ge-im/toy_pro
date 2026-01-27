package com.example.demo.like.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LikeActionType {
	ADD("A"), REMOVE("D");
	
	private final String value;
}

package com.example.demo.user.api.dto;

public record UserCreateRequestDTO(
		String userId,
		String userNm,
		String userPswd
) { }

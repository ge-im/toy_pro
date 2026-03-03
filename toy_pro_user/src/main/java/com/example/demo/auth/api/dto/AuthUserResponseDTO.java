package com.example.demo.auth.api.dto;

public record AuthUserResponseDTO (
		long userSn,
		String userId,
		String userNm
) { } 

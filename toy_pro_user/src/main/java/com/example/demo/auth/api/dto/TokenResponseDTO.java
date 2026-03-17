package com.example.demo.auth.api.dto;

public record TokenResponseDTO(
	String accessToken,
	AuthUserResponseDTO userInfo
) {}

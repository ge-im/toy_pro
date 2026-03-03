package com.example.demo.auth.api.dto;

public record InternalTokenResponseDTO(
		TokenResponseDTO response,
		String refreshToken
) {}

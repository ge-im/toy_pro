package com.example.demo.auth.api.dto;

public record AuthUserRequestDTO (
		String loginId,
		String loginPassword
) { }

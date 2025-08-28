package com.example.demo.post.api.dto;

public record PostCreateRequestDTO(
		String title,
		long userSn,
		String content
) { }

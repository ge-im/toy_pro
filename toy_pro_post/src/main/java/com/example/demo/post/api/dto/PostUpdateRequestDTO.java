package com.example.demo.post.api.dto;

public record PostUpdateRequestDTO(
		long postSn,
		String title,
		long userSn,
		String content
) { }

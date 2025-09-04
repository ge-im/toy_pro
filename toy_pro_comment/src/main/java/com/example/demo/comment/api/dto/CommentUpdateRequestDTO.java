package com.example.demo.comment.api.dto;

public record CommentUpdateRequestDTO(
		long commentSn,
		String content
) { }

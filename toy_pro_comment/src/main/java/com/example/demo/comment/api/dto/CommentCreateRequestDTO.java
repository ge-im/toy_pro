package com.example.demo.comment.api.dto;

public record CommentCreateRequestDTO(
		long postSn,
		long userSn,
		String content,
		long parentSn
) { }

package com.example.demo.comment.api.dto;

import java.time.LocalDateTime;

public record CommentResponseDTO(
		long commentSn,
		long postSn,
		long userSn,
		String content,
		long parentSn,
		String delYn,
		LocalDateTime regDt,
		LocalDateTime updtdt,
		String userId,
		String userNm,
		int level,
		String path
) { }
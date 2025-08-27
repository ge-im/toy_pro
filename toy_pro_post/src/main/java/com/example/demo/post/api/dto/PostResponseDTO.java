package com.example.demo.post.api.dto;

import java.time.LocalDateTime;

public record PostResponseDTO(
		long post_sn,
		String title,
		long user_sn,
		String userId,
		String userNm,
		String content,
		String delYn,
		LocalDateTime regDt,
		LocalDateTime updtDt
) {}

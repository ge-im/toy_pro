package com.example.demo.post.api.dto;

import java.time.LocalDateTime;

public record PostResponseDTO(
		long postSn,
		String title,
		long userSn,
		String userId,
		String userNm,
		String content,
		long viewCnt,
		String delYn,
		LocalDateTime regDt,
		LocalDateTime updtDt
) {}

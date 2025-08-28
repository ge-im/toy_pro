package com.example.demo.post.api.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class PostSearchRequestDTO {
	private String title;
	private String userId;
	private String userNm;
	private String content;
	private LocalDate startUpdtDt;
	private LocalDate endUpdtDt;
}

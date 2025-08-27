package com.example.demo.post.api.dto;

import lombok.Data;

@Data
public class PostSearchRequestDTO {
	private String title;
	private String userId;
	private String userNm;
	private String content;
	private String startUpdtDt;
	private String endUpdtDt;
}

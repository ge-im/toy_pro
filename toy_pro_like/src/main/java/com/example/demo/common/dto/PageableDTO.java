package com.example.demo.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageableDTO {

	public static final int DEFAULT_PAGE = 0;
	public static final int DEFAULT_SIZE = 50;//댓글이니깐 기본 사이즈를 조금 크게 가져가기
	
	private int page;
	private int size;
	
	public int getOffset() {
		return size * page;
	}
	
}

package com.example.demo.common.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PageableDTO {

	public static final int DEFAULT_PAGE = 0;
	public static final int DEFAULT_SIZE = 20;
	
	private int page;
	private int size;
	
	public PageableDTO() {
		checkSize(size);
		checkPage(page);
	}
	
	public PageableDTO(int page, int size) {
		checkSize(size);
		checkPage(page);
	}
	
	public void setPage(int page) {
		checkPage(page);
	}
	
	public void setSize(int size) {
		checkSize(size);
	}
	
	public int getOffset() {
		return size * page;
	}
	
	private void checkPage(int page) {
		this.page = page < 0 ? DEFAULT_PAGE : page; 
	}
	
	private void checkSize(int size) {
		this.size = size < 0 ? DEFAULT_SIZE : size; 
	}
}

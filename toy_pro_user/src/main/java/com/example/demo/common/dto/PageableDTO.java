package com.example.demo.common.dto;

import lombok.Getter;

@Getter
public class PageableDTO {

	private static final int DEFAULT_PAGE = 0;
	private static final int DEFAULT_SIZE = 20;
	
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
	
	private void checkPage(int page) {
		this.page = page < 0 ? DEFAULT_PAGE : page; 
	}
	
	private void checkSize(int size) {
		this.size = size < 0 ? DEFAULT_SIZE : size; 
	}
}

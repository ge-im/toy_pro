package com.example.demo.user.api.dto;

import java.util.Optional;

public record UserSearchRequestDTO(
		Optional<String> userNm,
		Optional<Integer> page,
		Optional<Integer> size
) { 
	public int getOffset() {
		return (page.get() == null ? 0 : page.get()) 
				* (size.get() == null ? 20 : size.get());
	}
}

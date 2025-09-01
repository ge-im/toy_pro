package com.example.demo.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SortCondition {
	private String property;
	private SortDirection direction;

	public enum SortDirection {
		ASC, DESC
	}
}

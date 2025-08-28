package com.example.demo.common.DTO;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SearchDTO<T> {
	private T condition;
	private PageableDTO page;
	
	private List<SortCondition> sorts = new ArrayList<>();
	
	public SearchDTO(T condition, PageableDTO page, List<SortCondition> sorts) {
		this.condition = condition;
		this.page = page;
		checkSorts(sorts);
	}
	
	public void setSorts(List<SortCondition> sorts) {
		checkSorts(sorts);
	}
	
	private void checkSorts(List<SortCondition> sorts) {
		if(sorts == null) {
			this.sorts = new ArrayList<>();
		}
	}

}

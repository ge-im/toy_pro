package com.example.demo.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @packageName    : com.example.demo.common.dto
 * @fileName       : SortCondition.java
 * @author         : imge
 * @date           : 2025. 8. 20. 오후 7:13:01
 * @description    : 검색기능에서 정렬 관련 조건 (정렬 컬럼이 2개 이상일 경우 사용)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 8. 20.        imge       최초 생성
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SortCondition {
	private String property;
	private SortDirection direction;

	//해당 클래스에서만 사용되기 때문에 내부 enum으로 사용
	public enum SortDirection {
		ASC, DESC
	}
}

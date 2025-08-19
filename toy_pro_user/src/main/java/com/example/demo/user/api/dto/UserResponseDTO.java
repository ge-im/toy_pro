package com.example.demo.user.api.dto;

/**
 * @packageName    : com.example.demo.user.api.dto
 * @fileName       : UserReponseDTO.java
 * @author         : imge
 * @date           : 2025. 8. 12. 오후 5:54:25
 * @description    : 사용자 응답용 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 8. 12.        imge       최초 생성
 */
public record UserResponseDTO(
		long userSn, 
		String userId, 
		String userNm,
		String userPswd,
		String delYn,
		String regDt, 
		String updtDt
) { }

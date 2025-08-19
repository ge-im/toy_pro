package com.example.demo.user.domain.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.example.demo.user.api.dto.UserSearchRequestDTO;
import com.example.demo.user.domain.model.User;

import reactor.core.publisher.Flux;

/**
 * @packageName    : com.example.demo.user.domain.repository
 * @fileName       : UserRepository.java
 * @author         : imge
 * @date           : 2025. 8. 12. 오후 4:41:40
 * @description    : 사용자 기능 R2dbc domain repository
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 8. 12.        imge       최초 생성
 */
public interface UserRepository extends ReactiveCrudRepository<User, Long>{
	/**
	 * @methodName findActiveUser
	 * @param 
	 * @return Flux<User>
	 * @throws 
	 * @description 사용자(삭제되지 않은) 목록 조건 검색
	 */
	@Query("SELECT * FROM t_user_m01 WHERE del_yn = 'N' AND user_nm ILIKE CONCAT('%', :userNm, '%') LIMIT :size OFFSET :offset")
	Flux<User> findActiveUser(String userNm, int size, int offset);
}

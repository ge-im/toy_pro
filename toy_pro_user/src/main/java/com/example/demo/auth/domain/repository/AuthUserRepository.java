package com.example.demo.auth.domain.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.auth.domain.model.AuthUser;

import reactor.core.publisher.Mono;

@Repository
public interface AuthUserRepository extends ReactiveCrudRepository<AuthUser, Long> {
	/**
	 * @methodName findByUserLogin
	 * @param 
	 * @return Mono<AuthUser>
	 * @throws 
	 * @description id로 사용자 검색
	 */
	@Query("""
			SELECT 
			 	user_sn
			 	, user_id
			 	, user_nm
			 	, user_pswd
			FROM t_user_m01
			WHERE del_yn = 'N' 
				AND user_Id = :userId 
			""")
	Mono<AuthUser> findByLoginId(String userId);
}

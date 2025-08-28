package com.example.demo.post.domain.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.example.demo.post.domain.model.Post;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PostRepository extends ReactiveCrudRepository<Post, Long> {
	
	@Query("""
			SELECT
				p.post_sn
				, p.title
				, p.user_sn
				, u.user_id
				, u.user_nm
				, p.content
				, p.view_cnt
				, p.del_yn
				, p.red_dt
				, p.updt_dt
			FROM t_post_m01 p INNER JOIN t_user_m01 u
			 		ON ( p.user_sn = u.user_sn)
			WHERE
				p.del_yn = 'N'
				AND p.title LIKE CONCAT('%', :title, '%')
				AND u.user_nm LIKE CONCAT('%', :userNm, '%')
			ORDER BY p.updt_dt DESC
			LIMIT :size OFFSET :offset
			""")
	public Flux<Post> findAll(String title, String userNm, int size, int offset);
	
	@Query("""
			SELECT
				p.post_sn
				, p.title
				, p.user_sn
				, u.user_id
				, u.user_nm
				, p.content
				, p.view_cnt
				, p.del_yn
				, p.red_dt
				, p.updt_dt
			FROM t_post_m01 p INNER JOIN t_user_m01 u
			 		ON ( p.user_sn = u.user_sn)
			WHERE
				p.post_sn = :postSn
			""")
	public Mono<Post> findPostById(long postSn);
	
	@Query("""
			UPDATE t_post_m01 SET
				view_cnt = view_cnt + 1
			WHERE post_sn = :postSn 	
			""")
	public Mono<Integer> increaseViewCount(long postSn);
}

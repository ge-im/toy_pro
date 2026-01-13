package com.example.demo.comment.domain.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.example.demo.comment.domain.model.Comment;

import reactor.core.publisher.Flux;

public interface CommentRepository extends ReactiveCrudRepository<Comment, Long> {
	
	@Query("""
			WITH RECURSIVE comment_hierarchy (
					comment_sn, 
				 	parent_sn, 
				 	post_sn, 
				 	user_sn,
				 	content,
				 	del_yn,
					reg_dt,
				   	LEVEL, 
				   	PATH, 
				   	CYCLE) AS (
				   		SELECT  
				   			comment_sn, parent_sn, post_sn, user_sn, content, del_yn, reg_dt, 
				   			0, ARRAY[comment_sn], false
						FROM t_comment_m01 
						WHERE post_sn = :postSn
							AND parent_sn IS null 
						UNION ALL
						SELECT 
							ori.comment_sn, ori.parent_sn, ori.post_sn, ori.user_sn, ori.content, ori.del_yn, ori.reg_dt, 
							LEVEL + 1, PATH || ori.comment_sn, ori.comment_sn = ANY(PATH)
						FROM t_comment_m01 ori, comment_hierarchy dpt2
						WHERE ori.post_sn = :postSn
							AND ori.parent_sn = dpt2.comment_sn 
							AND NOT CYCLE
				)
				SELECT 
					c.comment_sn 
				 	, c.parent_sn 
				 	, c.post_sn 
				 	, c.user_sn
				 	, CASE
				 		WHEN c.del_yn = 'Y' THEN NULL
				 		ELSE c.content
				 	END AS content
					, c.del_yn
				 	, c.reg_dt
				 	, c.LEVEL
				 	, u.user_nm
				 	, u.user_id
				FROM comment_hierarchy c LEFT OUTER JOIN t_user_m01 u 
					ON c.user_sn = u.user_sn
				ORDER BY c.path, c.reg_Dt ASC
				LIMIT :size OFFSET :offset
			""")
	public Flux<Comment> findAllByPostSn(long postSn, int size, int offset);
	
	
}

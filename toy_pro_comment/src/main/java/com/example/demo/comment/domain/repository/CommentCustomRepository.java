package com.example.demo.comment.domain.repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.stereotype.Repository;

import com.example.demo.comment.domain.model.Comment;
import com.example.demo.common.dto.PageableDTO;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Repository
@RequiredArgsConstructor
public class CommentCustomRepository {
	
	private final DatabaseClient client;
	
	public Flux<Comment> findAllByUserSn(long userSn, PageableDTO page, LocalDateTime startRegDt, LocalDateTime endRegDt) {
		StringBuilder sql = new StringBuilder("""
				SELECT 
					c.comment_sn 
				 	, c.parent_sn 
				 	, c.post_sn 
				 	, c.user_sn
				 	, c.content
				 	, c.reg_dt
				 	, c.updt_dt
				 	, u.user_nm
				 	, u.user_id
				FROM t_comment_m01 c LEFT OUTER JOIN t_user_m01 u 
					ON c.user_sn = u.user_sn
				WHERE 
					c.del_yn = 'N'
					AND c.user_sn = :userSn
				""");
		
		Map<String, Object> params = new HashMap<>();
		params.put("userSn" , userSn);
		
		if(startRegDt != null) {
			sql.append(" AND c.reg_dt >= :startRegDt ");
			params.put("startRegDt", startRegDt);
		}
		
		if(endRegDt != null) {
			sql.append(" AND c.reg_dt <= :endRegDt ");
			params.put("endRegDt", endRegDt);
		}
		
		sql.append(" ORDER BY c.reg_dt DESC ");
		sql.append(" LIMIT :size OFFSET :offset ");
		params.put("limit", page.getSize());
		params.put("offset", page.getOffset());
		
		GenericExecuteSpec spec = client.sql(sql.toString());
		
		for(Entry<String, Object> entry : params.entrySet()) 
			spec = spec.bind(entry.getKey(), entry.getValue());
		return spec.map(r -> {
			Comment result = new Comment();
			result.setCommentSn(r.get("comment_sn", Long.class));
			result.setParentSn(r.get("parent_sn", Long.class));
			result.setPostSn(r.get("post_sn", Long.class));
			result.setUserSn(r.get("user_sn", Long.class));
			result.setContent(r.get("content", String.class));
			result.setRegDt(r.get("reg_dt", LocalDateTime.class));
			result.setUpdtDt(r.get("updt_dt", LocalDateTime.class));
			result.setUserNm(r.get("user_nm", String.class));
			result.setUserId(r.get("user_id", String.class));
			return result; 
		}).all();
	}
	
}

package com.example.demo.post.domain.repository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.example.demo.common.DTO.PageableDTO;
import com.example.demo.common.DTO.SearchDTO;
import com.example.demo.post.api.dto.PostSearchRequestDTO;
import com.example.demo.post.domain.model.Post;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Repository
@RequiredArgsConstructor
public class PostCustomRepository {
	
	private final DatabaseClient client;
	
	public Flux<Post> findAllByConditions(SearchDTO<PostSearchRequestDTO> cond) {
		StringBuilder sql = new StringBuilder("""
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
						ON p.user_sn = u.user_sn
				WHERE 
					p.del_yn = 'N'
				""");
		
		Map<String, Object> params = new HashMap<>();
		PostSearchRequestDTO reqDto = cond.getCondition();
		
		if(StringUtils.hasText(reqDto.getTitle())) {
			sql.append(" AND p.title LIKE :title ");
			params.put("title", "%" + reqDto.getTitle()+"%");
		}
		
		if(StringUtils.hasText(reqDto.getUserId())) {
			sql.append(" AND u.user_id LIKE :userId ");
			params.put("userId", "%" + reqDto.getUserId()+"%");
		}
		
		if(StringUtils.hasText(reqDto.getUserNm())) {
			sql.append(" AND u.user_nm LIKE :userNm ");
			params.put("userNm", "%" + reqDto.getUserNm()+"%");
		}
		
		if(StringUtils.hasText(reqDto.getContent())) {
			sql.append(" AND p.content LIKE :content ");
			params.put("content", "%" + reqDto.getContent()+"%");
		}
		
		if(reqDto.getStartUpdtDt() != null) {
			sql.append(" AND p.updt_dt >= : startUpdtDt ");
			params.put("startUpdtDt", reqDto.getStartUpdtDt().atStartOfDay());
		}
		
		if(reqDto.getEndUpdtDt() != null) {
			sql.append(" AND p.updt_dt <= : endUpdtDt ");
			params.put("endUpdtDt", reqDto.getEndUpdtDt().atTime(LocalTime.MAX));
		}
		
		if(!cond.getSorts().isEmpty()) {
			sql.append(" ORDER BY ");
			sql.append(
					cond.getSorts()
						.stream()
						.map(c -> String.format("%s %s", c.getProperty(), c.getDirection().name()))
						.collect(Collectors.joining(", "))
			);
		}
		
		PageableDTO pageDto = cond.getPage();
		int offset = pageDto.getSize() * pageDto.getPage();
		sql.append(" LIMIT :limit OFFSET :offset");
		params.put("limit", pageDto.getSize());
		params.put("offset", offset);
		
		GenericExecuteSpec spec = client.sql(sql.toString());
		
		for(Entry<String, Object> entry : params.entrySet()) 
			spec = spec.bind(entry.getKey(), entry.getValue());
		
		return spec.map(r -> new Post(
					r.get("post_sn", Long.class),
					r.get("title", String.class),
					r.get("user_sn", String.class),
					r.get("content", String.class),
					r.get("view_cnt", Long.class),
					r.get("del_yn", String.class),
					r.get("reg_dt", LocalDateTime.class),
					r.get("updt_dt", LocalDateTime.class),
					r.get("user_id ", String.class),
					r.get("user_nm", String.class)
				)).all();
	}
}

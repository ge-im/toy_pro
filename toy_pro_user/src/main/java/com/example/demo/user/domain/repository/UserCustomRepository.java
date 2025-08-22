package com.example.demo.user.domain.repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.example.demo.common.dto.PageableDTO;
import com.example.demo.common.dto.SearchDTO;
import com.example.demo.user.api.dto.UserSearchRequestDTO;
import com.example.demo.user.domain.model.User;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Repository
@RequiredArgsConstructor
public class UserCustomRepository {
	
	private final DatabaseClient client;
	
	public Flux<User> findAllByConditions(SearchDTO<UserSearchRequestDTO> cond) {
		StringBuilder sql = new StringBuilder(" SELECT * FROM t_user_m01 WHERE  del_yn = 'N' ");
		
		Map<String, Object> params = new HashMap<>();
		
		UserSearchRequestDTO reqDto = cond.getCondition();
		if(StringUtils.hasText(reqDto.getUserId())) {
			sql.append(" AND USER_ID LIKE :user_id ");
			params.put("user_id", "%" + reqDto.getUserId() + "%");
		}
		
		if(StringUtils.hasText(reqDto.getUserNm())) {
			sql.append(" AND USER_NM LIKE :user_nm ");
			params.put("user_nm", "%" + reqDto.getUserNm() + "%");
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
		
		return spec.map(r -> new User(
					r.get("user_sn", Long.class), 
					r.get("user_id", String.class), 
					r.get("user_nm", String.class), 
					r.get("user_pswd", String.class), 
					r.get("del_yn", String.class), 
					r.get("reg_dt", LocalDateTime.class), 
					r.get("updt_dt", LocalDateTime.class))
				).all();
	}
	
}

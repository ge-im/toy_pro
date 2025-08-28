package com.example.demo.post.domain.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table("t_post_m01")
public class Post {
	@Id @Column("post_sn") 
	private long postSn;
	
	private String title;
	
	@Column("user_sn")
	private String userSn;
	
	private String content;
	
	@Column("view_cnt")
	private long viewCnt;
	
	@Column("del_yn")
	private String delYn; 
	
	@Column("reg_dt")
	private LocalDateTime regDt; 
	
	@Column("updt_dt")
	private LocalDateTime updtDt;
	
/** Join으로 조회하는 컬럼 */
	
	@Column("user_id")
	private String userId; 
	
	@Column("user_nm")
	private String userNm;
}

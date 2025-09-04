package com.example.demo.comment.domain.model;

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
@Table("t_comment_m01")
public class Comment {
	@Id @Column("comment_sn")
	private long commentSn;
	
	@Column("post_sn")
	private long postSn;
	
	@Column("user_sn")
	private long userSn;
	
	private String content;
	
	@Column("parent_sn")
	private long parentSn;
	
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
	
/** 계층형 쿼리 레벨 관련 컬럼 */
	
	private int level;
	
	private String path;
}

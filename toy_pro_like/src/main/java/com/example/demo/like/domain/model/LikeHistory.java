package com.example.demo.like.domain.model;

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
@Table("t_like_h01")
public class LikeHistory {
	@Id 
	private long sn;
	
	@Column("target_type")
	private String targetType;
	
	@Column("target_sn")
	private long targetSn;
	
	@Column("user_sn")
	private long userSn;
	
	@Column("action_type")
	private String actionType;
	
	@Column("reg_dt")
	private LocalDateTime redDt;
}

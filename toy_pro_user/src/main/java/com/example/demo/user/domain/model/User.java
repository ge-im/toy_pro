package com.example.demo.user.domain.model;

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
@Table("t_user_m01")
public class User {
	
	@Id @Column("user_sn") 
	private long userSn;
	
	@Column("user_id") 
	private String userId;
	
	@Column("user_nm") 
	private String userNm;
	
	@Column("user_pswd") 
	private String userPswd;
	
	@Column("del_yn") 
	private String delYn;
	
	@Column("reg_dt") 
	private LocalDateTime regDt;
	
	@Column("updt_dt")
	private LocalDateTime updtDt;

}

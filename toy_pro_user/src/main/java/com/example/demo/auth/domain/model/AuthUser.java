package com.example.demo.auth.domain.model;

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
public class AuthUser {

	@Id @Column("user_sn") 
	private long userSn;
	
	@Column("user_id") 
	private String userId;
	
	@Column("user_nm") 
	private String userNm;
	
	@Column("user_pswd") 
	private String userPswd;
}

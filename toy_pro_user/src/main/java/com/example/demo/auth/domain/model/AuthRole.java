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
@Table("t_role_m01")
public class AuthRole {
	@Id @Column("role_sn")
	private long roleSn;
	
	@Column("role_cd")
	private String roleCd;

	@Column("role_nm")
	private String roleNm;
}

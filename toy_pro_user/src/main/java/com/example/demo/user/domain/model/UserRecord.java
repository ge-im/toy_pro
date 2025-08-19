package com.example.demo.user.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * @packageName    : com.example.demo.user.domain.model
 * @fileName       : User.java
 * @author         : imge
 * @date           : 2025. 8. 12. 오후 5:07:14
 * @description    : 사용자 기능의 t_user_m01 테이블 entity
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 8. 12.        imge       최초 생성
 */
@Table("t_user_m01")
public record UserRecord(
	@Column("user_sn") long userSn, 
	@Id @Column("user_id") String userId, 
	@Column("user_nm") String userNm,
	@Column("user_pswd") String userPswd,
	@Column("del_yn") String delYn,
	@Column("reg_dt") String regDt, 
	@Column("updt_dt") String updtDt
) { }

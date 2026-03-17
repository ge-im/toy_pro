/*
database, user, schema

toy_user: 개발 전용 db 계정 DB 접속 + 특정 스키마에서만 DML 가능
dev_toy: 개발 전용 database
dev_toy_schema01: 개발자가 작업할 수 있는 구역
*/

CREATE TABLE dev_toy_schema01.t_user_m01 (
	user_sn SERIAL NOT NULL PRIMARY KEY
	, user_id VARCHAR(20) NOT NULL
	, user_nm VARCHAR(100) 
	, user_pswd VARCHAR(255) 
	, del_yn CHAR(1) DEFAULT 'N'
	, reg_dt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
	, updt_dt TIMESTAMP
);

CREATE TABLE dev_toy_schema01.t_role_m01 (
	role_sn SERIAL PRIMARY KEY
	, role_cd VARCHAR(30) NOT NULL
	, role_nm VARCHAR(100)
);

CREATE TABLE dev_toy_schema01.t_role_hierarchy_s01 (
	parent_role_sn INTEGER
    , child_role_sn INTEGER
);

CREATE TABLE dev_toy_schema01.t_user_role_s01 (
	user_role_sn SERIAL NOT NULL PRIMARY KEY
	, user_sn INTEGER
	, role_sn INTEGER
);

CREATE TABLE dev_toy_schema01.t_post_m01 (
	post_sn SERIAL NOT NULL PRIMARY KEY
	, title VARCHAR(255)
	, user_sn INTEGER  
	, content TEXT
	, view_cnt INTEGER 
	, del_yn CHAR(1) 
	, reg_dt TIMESTAMP 
	, updt_dt TIMESTAMP 
);

CREATE TABLE dev_toy_schema01.t_comment_m01 (
	comment_sn SERIAL NOT NULL PRIMARY KEY
	, post_sn INTEGER
	, user_sn INTEGER 
	, content VARCHAR(255)
	, parent_sn INTEGER
	, del_yn CHAR(1) 
	, reg_dt TIMESTAMP 
	, updt_dt TIMESTAMP 
);

CREATE TABLE dev_toy_schema01.t_like_m01 (
	sn SERIAL NOT NULL PRIMARY KEY
	, target_type CHAR(1) --P:(POST), C(COMMENT)
	, target_sn INTEGER --POST_SN, COMMENT_SN
	, user_sn INTEGER
	, reg_dt TIMESTAMP DEFAULT CURRENT_TIMESTAMP 
);

CREATE TABLE dev_toy_schema01.t_like_h01 (
	sn SERIAL NOT NULL PRIMARY KEY
	, target_type CHAR(1) --P:(POST), C(COMMENT)
	, target_sn INTEGER --POST_SN, COMMENT_SN
	, user_sn INTEGER
	, action_type CHAR(1) --A(add: 좋아요), D:(delete: 좋아요 취소-물리삭제)
	, reg_dt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);





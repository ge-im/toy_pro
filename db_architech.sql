/*
DB user 및 schema

admin_postgre: 모든 권한 보유 (superuser)
dev_toy: 개발 전용 DB
dev_toy_schema01: 개발자가 작업할 수 있는 구역
*/

CREATE TABLE dev_toy_schema01.t_user_m01 (
	user_sn SERIAL NOT NULL PRIMARY KEY
	, user_id VARCHAR(20) NOT NULL 
	, user_nm VARCHAR(100) 
	, user_pswd VARCHAR(200) 
	, del_yn CHAR(1) 
	, reg_dt TIMESTAMP 
	, updt_dt TIMESTAMP
);

CREATE TABLE dev_toy_schema01.t_auth_m01 (
	auth_sn SERIAL NOT NULL PRIMARY KEY
	, auth_cd VARCHAR(20) NOT NULL
	, auth_nm VARCHAR(200)
	, up_auth_sn INTEGER
);

--sn or cd/id? 뭘로 해야하나?
CREATE TABLE dev_toy_schema01.t_user_auth_s01 (
	sn SERIAL NOT NULL PRIMARY KEY
	, user_id INTEGER
	, auth_cd INTEGER
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





-- 기본 role data
INSERT INTO dev_toy_schema01.t_role_m01 (role_cd, role_nm)
VALUES
('ROLE_USER', '일반 사용자'),
('ROLE_ADMIN', '관리자');

-- role 계층 구조(admin > user)
INSERT INTO dev_toy_schema01.t_role_hierarchy_s01 (parent_role_sn, child_role_sn)
VALUES (
    (SELECT role_sn FROM dev_toy_schema01.t_role_m01 WHERE role_cd = 'ROLE_ADMIN'),
    (SELECT role_sn FROM dev_toy_schema01.t_role_m01 WHERE role_cd = 'ROLE_USER')
);

-- 기본 관리자 계정
INSERT INTO dev_toy_schema01.t_user_m01
(user_id, user_nm, user_pswd, del_yn)
VALUES
(
    'admin',
    '관리자',
    '$2a$10$Dow1p9m6V9z8sH5K3s7fZ.9JQx8g4XKXqF5uWzL0O8mKzYtJf9u7G',
	'N'
);

--관리자 계정 role 매핑
INSERT INTO dev_toy_schema01.t_user_role_s01 (user_sn, role_sn)
VALUES (
    (SELECT user_sn FROM dev_toy_schema01.t_user_m01 WHERE user_id = 'admin'),
    (SELECT role_sn FROM dev_toy_schema01.t_role_m01 WHERE role_cd = 'ROLE_ADMIN')
);

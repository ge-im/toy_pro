/* 
db 초기 설정 

1. db랑 user는 초기 관리자가 생성한다고 가정, 사용자(개발자)에게 dml 부여 불필요.
2. 초기 postgres 계정을 제외하고 dml, ddl, dcl 모두 가능한 관리자(superuser) 권한 가진 계정 1개 필요
3. 2번의 관리자 계정이 개발용으로 쓰일 db, db내에 특정 개발용 schema, 사용자(개발자) user 생성 예정
4. 사용자(개발자)는 테이블 dml 필요

Database 구조
dev_toy: 개발 전용 DB

Schema 구조
dev_toy_schema01: 개발자가 작업할 수 있는 구역

DB User 목록 
admin_postgre: 모든 권한 보유 (superuser)
toy_user: 개발 전용 db 계정 DB 접속 + 특정 스키마(dev_toy_schema01)에서만 DML 가능

*/

-- 1. 관리자(superuser) 계정 생성
CREATE ROLE admin_postgre WITH
    LOGIN
    SUPERUSER
    CREATEDB
    CREATEROLE
    REPLICATION
    PASSWORD 'postgresA5432';

-- 2. 개발용 사용자 계정 생성 (DML만 가능, DDL은 불필요)
CREATE ROLE toy_user WITH
    LOGIN
    NOCREATEDB
    NOCREATEROLE
    NOINHERIT
    PASSWORD 'toypostgres';

-- 3. 개발용 DB 생성 (소유자는 관리자 계정)
CREATE DATABASE dev_toy
    WITH OWNER admin_postgre
    ENCODING 'UTF8'
    LC_COLLATE='en_US.UTF-8'
    LC_CTYPE='en_US.UTF-8'
    TEMPLATE template0;

-- 4. 개발자 계정에 해당 DB 접속 권한 부여
GRANT CONNECT ON DATABASE dev_toy TO toy_user;

-- 5. DB 접속 후 스키마 생성 (admin_postgre 계정으로 실행)
--\c dev_toy admin_postgre;

CREATE SCHEMA dev_toy_schema01 AUTHORIZATION admin_postgre;

-- 6. 개발자에게 스키마 접근 권한 부여 (DDL 권한은 제외)
GRANT USAGE ON SCHEMA dev_toy_schema01 TO toy_user;

-- 7. 개발자에게 DML 권한 부여 (SELECT, INSERT, UPDATE, DELETE)
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA dev_toy_schema01 TO toy_user;

-- 8. 앞으로 생성될 테이블에 대해 DML 권한 자동 부여
ALTER DEFAULT PRIVILEGES IN SCHEMA dev_toy_schema01
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO toy_user;

-- 9. 시퀀스 사용 권한 부여 (자동증가 컬럼 사용 시 필요)
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA dev_toy_schema01 TO toy_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA dev_toy_schema01
GRANT USAGE, SELECT ON SEQUENCES TO toy_user;

-- 10. public 스키마 권한 제거 (보안 목적)
REVOKE ALL ON SCHEMA public FROM toy_user;




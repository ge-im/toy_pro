# Spring WebFlux Community API Toy Project

## 1. 프로젝트 소개

본 프로젝트는 Spring WebFlux 기반으로 사용자(User), 게시글(Post), 댓글(Comment), 좋아요(Like) 기능을 도메인 중심으로 설계한 개인 프로젝트입니다.  

Java/Spring Framework 실무 경험 이후, Spring WebFlux 기반의 비동기 요청 처리 구조를 직접 학습하고 구현해 보기 위해 진행했습니다.  
단순 CRUD 구현을 넘어 Reactive 환경의 요청 처리 구조, Redis를 활용한 상태 관리, JWT 기반 인증·인가 구조를 직접 설계하고 적용하는 것을 목표로 개발했습니다.

---

## 2. 주요 기술 스택

- Java / Spring Boot
- Spring WebFlux / Spring Security
- PostgreSQL / R2DBC
- Redis
- JWT 

---

## 3. 핵심 설계 포인트

### 3.1 WebFlux 기반 구조

- Controller 대신 Router / Handler 구조 사용
- Reactive 흐름 유지
- 비동기 처리 기반 설계

### 3.2 도메인 중심 설계

- user / post / comment / like 도메인 분리
- Domain / Infra / Common 구조 분리
- 기능 단위 확장 고려

### 3.3 Redis 활용 전략

- Like : 현재 상태 캐싱 (Set 기반)
- Auth : Token 관리 (Blacklist / Refresh Token)

→ 도메인별 Redis 역할 분리

### 3.4 JWT 기반 인증/인가

- WebFlux Security 기반 Filter 인증 구조
- ReactiveAuthenticationManager 활용
- Redis 기반 Token Lifecycle 관리

### 3.5 Error Handling 표준화

- ErrorCode Enum 기반 구조
- WebFlux Global Error Handler 적용
- 서비스 전반 에러 응답 통일

---

## 4. 프로젝트 구조

```
common : 공통 코드 (Error, Util 등)  
config : 설정 (Security, Redis, WebFlux 등)  
infra : 외부 시스템 연동 (Redis 등)  
user : 사용자 도메인  
post : 게시글 도메인  
comment : 댓글 도메인  
like : 좋아요 도메인  
```

---

## 5. 주요 설계 문서

프로젝트 설계 상세 내용은 아래 문서를 참고해주세요.

- [Architecture Overview](./docs/architecture-overview.md)
- [Redis Like Design](./docs/redis-like-design.md)
- [Error Handling Design](./docs/error-handling-design.md)
- [Security Design](./docs/security-design.md)
- [DB Design](./docs/db-design.md)
- [Git Strategy](./docs/git-strategy.md)

---

## 6. 실행 환경

- Java 21
- PostgreSQL
- Redis

```
PostgreSQL 데이터베이스 생성
(db_init.sql, db_architech.sql, db_data_init.sql) 
→ Redis 서버 실행 
→ application 설정에서 DB 및 Redis 연결 정보 구성 
→ Spring Boot 애플리케이션 실행
```

## 7. 향후 개선 계획

- MSA 구조 분리 (Spring Cloud)
- 이벤트 기반 아키텍처 도입
- Redis Pub/Sub 활용
- 알림 시스템 구현

---

## 8. 프로젝트 목표
이 프로젝트는 단순 기능 구현이 아니라 다음 역량을 강화하는 것을 목표로 합니다.

- 구조 설계 능력
- Reactive 프로그래밍 이해
- Redis 활용 설계
- 인증/인가 시스템 설계

---

## 개발 기록

개발 과정에서의 고민 및 시행착오는 아래 로그 파일에 기록되어 있습니다.

- [docs/개발로그.md](./docs/개발로그.md)
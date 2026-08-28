# Markdown 문서와 소스코드 정합성 후속 검토 기록

> 이 문서는 2026-08-21 시점의 후속 검토 스냅샷이다. 이전 검토([2026-08-18 리뷰](2026-08-18-doc-code-consistency-review.md))에서 기록한 이슈의 해결 상태를 우선 확인하고, 남은 문서·코드 정합성 문제를 기록한다. 공식 설계 명세가 아니다.

## 이전 이슈 해결 현황

| 이전 이슈 | 현재 판정 | 확인 근거 | 비고 |
| --- | --- | --- | --- |
| JWT Role/Authority 검사 불일치 가능성 | 해결 | `toy_pro_user/.../SecurityConfig.java`가 `hasAnyRole("ADMIN", "USER")`를 사용하고, AuthenticationManager는 `ROLE_ADMIN`/`ROLE_USER` Authority를 생성 | Spring Security 기본 Role 접두사 규칙과 맞춤 |
| Security 401/403 JSON 응답 미통일 | 해결 | `SecurityErrorResponseWriter`, `ErrorResponseFactory`, `SecurityConfig` | Security와 전역 오류 응답이 같은 DTO 필드를 사용 |
| Refresh Token 14일 문서 vs 7일 설정 | 해결 | `toy_pro_user/src/main/resources/application.yml`의 `refreshDate: 14` | 문서의 14일과 일치 |
| Post/Auth RouterFunction Bean 누락 | 해결 | `PostRouter.postRoutes`, `AuthRouter.authRoutes`에 `@Bean` 확인 | 실제 endpoint 노출은 별도 기동 검증 필요 |
| Like ErrorCode 표준 흐름 미적용 | 해결 | `LikeService`가 `BusinessException(BusinessErrorCode)` 사용 | 구 `AlreadyLikedExcepction` 클래스는 남아 있으나 호출되지 않음 |
| Like의 DB/Redis Source of Truth 불명확 | 미해결 | `LikeService`는 Redis Set과 `t_like_h01` 이력만 사용 | `t_like_m01` 접근 구현 없음 |
| 독립 모듈 구조/버전 차이 문서화 부족 | 미해결 | 4개의 독립 Gradle 프로젝트와 서로 다른 Spring Boot 버전, README의 단일 프로젝트 서술 | 문서 보완 필요 |
| Auth 로그인 SQL의 불완전한 INNER JOIN | 해결 | `AuthUserRepository.findByLoginId`에서 불완전한 `INNER JOIN` 제거 | 실제 로그인 통합 테스트는 미확인 |
| Post/Comment Repository SQL 오류 가능성 | 부분 해결 | Post의 `red_dt`는 `reg_dt`로 수정됨. Comment `:size`와 bind key `limit`, Post Custom Repository의 `: startUpdtDt` 등은 남음 | 각 쿼리 실행 검증 필요 |
| Like 추가 이력이 REMOVE로 저장 | 미해결 | `LikeService.like`가 여전히 `LikeActionType.REMOVE` 사용 | `LikeActionType.ADD` 정의와 충돌 |
| Comment DTO 누락/조건 검색 미구현 | 미해결 | `CommentSearchRequestDTO` import는 남고 정의 파일 미확인, `findAllByCondition`은 `null` 반환 | 변경 로그에도 현 브랜치 컴파일 실패로 기록 |

## 1. Review Overview

- 검토 일자: 2026-08-21
- 검토 목적: 이전 정합성 리뷰 이슈의 해결 여부를 재판정하고 현재 문서와 코드의 남은 차이를 기록
- 검토 대상: README, `docs` Markdown, DB SQL, 4개 Spring Boot 모듈의 핵심 설정·보안·Router·Service·Repository·테스트 소스
- 검토 방식: 정적 파일 대조 및 `docs/codex/CHANGE_LOG.md`의 이전 빌드 기록 확인. 이번 검토에서는 빌드/테스트를 실행하지 않음.
- 코드 수정 여부: 없음

## 2. Overall Assessment

이전의 보안 응답 형식, JWT Role 검사, Refresh Token 기간, Auth/Post Router 등록 문제는 코드상 해결됐다. 특히 User 모듈의 Security 오류 처리와 ErrorResponseDTO 공통화는 문서의 큰 방향과 현재 구현이 잘 맞는다.

그러나 Like 현재 상태의 저장 기준은 여전히 설계 문서와 다르고, Comment 모듈은 현재 정적 소스 및 변경 로그 기준으로 컴파일 가능 상태가 아니다. 따라서 문서는 인증/오류 처리 영역에서는 신뢰도가 높아졌지만, Like 데이터 정합성과 Comment/Post 검색 기능의 실행 상태는 문서만으로 신뢰하면 안 된다.

## 3. Consistent Items

- Java 21, WebFlux, R2DBC/PostgreSQL, Redis, JWT 사용은 README와 각 모듈 Gradle 설정에 일치한다.
- User 모듈은 `JwtAuthenticationFilter` → `JwtReactiveAuthenticationManager` → SecurityContext 흐름, Refresh Token Redis 저장, Access Token Blacklist를 구현한다. 이는 `docs/security-design.md`와 일치한다.
- Refresh Token의 14일 만료, Access Token의 30분 만료는 `application.yml`, `JwtProvider`, `AuthHandler`와 Security 문서에 일치한다.
- Security 401/403은 `SecurityErrorResponseWriter`가 `ErrorResponseFactory`를 통해 `status`, `code`, `message`, `time`, `path` 형식의 JSON을 작성한다. Error Handling 문서의 응답 계약과 일치한다.
- Like는 `ReactiveStringRedisExecutor`와 `LikeRedisRepository`를 사용해 Redis Set 기반으로 Like 여부와 수를 조회한다. Redis Like 문서의 자료구조 선택과 일치한다.
- Post/Auth/User/Comment/Like의 현재 RouterFunction은 Bean으로 등록되어 있다.

## 4. Inconsistencies

### 4.1 Like 현재 상태의 DB Source of Truth 부재

- 중요도: High
- 관련 Markdown 파일: `docs/architecture-overview.md`, `docs/redis-like-design.md`, `docs/db-design.md`
- 관련 소스 파일: `toy_pro_like/src/main/java/com/example/demo/like/service/LikeService.java`, `toy_pro_like/src/main/java/com/example/demo/like/domain/repository/LikeHistoryRepository.java`
- 문서에 작성된 내용: Like의 최종 데이터 기준은 DB이며 `t_like_m01`은 현재 상태, `t_like_h01`은 이력으로 분리된다. Redis는 캐시와 상태 조회 성능 개선용이다.
- 실제 구현 내용: `LikeService`는 Redis Set을 현재 상태로 변경·조회하고 `LikeHistoryRepository`로 `t_like_h01`만 저장한다. `t_like_m01` Entity/Repository/동기화는 확인되지 않는다.
- 차이와 영향: Redis 유실 시 현재 Like 상태를 DB에서 복원할 현재 구현 근거가 없다. 문서의 DB Source of Truth와 장애 복구 설명을 운영 기준으로 사용할 수 없다.
- 권장 조치: 코드 재검토 및 문서 수정. 현재 상태 저장소, History 역할, `t_like_m01` 유지 여부, Redis 장애·복구 정책을 확정한다.

### 4.2 Like 추가 이벤트가 REMOVE로 기록됨

- 중요도: High
- 관련 Markdown 파일: `docs/db-design.md`, `docs/redis-like-design.md`
- 관련 소스 파일: `toy_pro_like/src/main/java/com/example/demo/like/service/LikeService.java`, `toy_pro_like/src/main/java/com/example/demo/like/enums/LikeActionType.java`
- 문서에 작성된 내용: `t_like_h01`은 Like 이벤트 이력이며 DDL은 `A`를 add, `D`를 delete로 정의한다.
- 실제 구현 내용: `LikeService.like`와 `LikeService.unLike` 모두 `LikeActionType.REMOVE`를 `getHistory`에 전달한다.
- 차이와 영향: Like 추가와 취소 이력을 구분할 수 없어 이력 분석, 데이터 복구, 향후 이벤트 처리의 정합성이 깨질 수 있다.
- 권장 조치: 코드 재검토. 기존 데이터 의미를 확인한 뒤 추가 이벤트의 action 값을 확정하고 검증 테스트를 추가한다.

### 4.3 Comment 기능 완료 서술과 현재 컴파일/검색 상태

- 중요도: Critical
- 관련 Markdown 파일: `README.md`, `docs/architecture-overview.md`, `docs/개발로그.md`
- 관련 소스 파일: `toy_pro_comment/src/main/java/com/example/demo/comment/service/CommentService.java`
- 문서에 작성된 내용: Comment CRUD와 계층형 조회가 구현된 기능으로 설명된다.
- 실제 구현 내용: `CommentService`는 존재하지 않는 것으로 보이는 `CommentSearchRequestDTO`를 import하고, `findAllByCondition`은 `null`을 반환한다. `docs/codex/CHANGE_LOG.md`도 이 누락 타입으로 Comment 컴파일이 실패했다고 기록한다.
- 차이와 영향: 현 브랜치의 Comment 모듈은 빌드 가능성과 조건 검색 기능을 보장하지 못한다. 문서의 “구현 완료” 인상과 다르다.
- 권장 조치: 코드 재검토. DTO의 실제 위치/필요성, 조건 검색의 범위를 확정하고 clean build 및 API 테스트 후 문서 상태를 갱신한다.

### 4.4 Post/Comment Custom Repository 검색 쿼리의 잔여 오류 가능성

- 중요도: High
- 관련 Markdown 파일: `docs/architecture-overview.md`, `docs/개발로그.md`
- 관련 소스 파일: `toy_pro_post/src/main/java/com/example/demo/post/domain/repository/PostCustomRepository.java`, `toy_pro_comment/src/main/java/com/example/demo/comment/domain/repository/CommentCustomRepository.java`
- 문서에 작성된 내용: Post 검색/Paging과 Comment 목록·Paging은 Custom Repository 구조로 구현된 기능이다.
- 실제 구현 내용: Post Custom Repository에는 `: startUpdtDt`, `: endUpdtDt`처럼 콜론 뒤 공백이 있는 named parameter와 `r.get("user_id ", String.class)`의 후행 공백이 남아 있다. Comment Custom Repository SQL은 `LIMIT :size`를 사용하지만 `limit`만 bind한다.
- 차이와 영향: 일부 검색 조건이나 Comment 사용자별 목록 요청이 런타임 SQL binding/mapping 오류를 낼 수 있다. 문서의 기능 완료 설명을 실제 동작 보증으로 해석할 수 없다.
- 권장 조치: 코드 재검토. 요청 조건별 Repository 통합 테스트로 쿼리를 검증하고, 동작 확인 전까지 문서에서 구현 완료 표현을 완화한다.

### 4.5 독립 실행 모듈 구조와 README 설명의 간극

- 중요도: Medium
- 관련 Markdown 파일: `README.md`, `docs/architecture-overview.md`
- 관련 소스 파일: `toy_pro_user/build.gradle`, `toy_pro_post/build.gradle`, `toy_pro_comment/build.gradle`, `toy_pro_like/build.gradle`
- 문서에 작성된 내용: 하나의 Community API 프로젝트 안에 도메인 패키지가 구성된 형태로 소개한다.
- 실제 구현 내용: 네 개의 독립 Gradle/Spring Boot 프로젝트가 있으며 Spring Boot 버전은 User 3.4.3, Post/Comment 3.5.5, Like 4.0.1로 다르다.
- 차이와 영향: 실행 단위, 배포 방식, 모듈 간 인증·데이터 공유 범위를 처음 보는 개발자가 오해할 수 있다.
- 권장 조치: 문서 수정. 모듈별 실행 방법, 포트, 공용 인프라, 현재 통합 방식과 버전 차이를 명시한다.

### 4.6 Error Handling 문서의 Map 표현과 실제 DTO 응답

- 중요도: Low
- 관련 Markdown 파일: `docs/error-handling-design.md`
- 관련 소스 파일: `toy_pro_user/src/main/java/com/example/demo/common/dto/ErrorResponseDTO.java`, `toy_pro_user/src/main/java/com/example/demo/config/error/ErrorResponseFactory.java`
- 문서에 작성된 내용: ErrorCode → Exception → Global Handler → Response(Map) 구조를 설명한다.
- 실제 구현 내용: 공통 응답 body는 `ErrorResponseDTO`이며, WebFlux ErrorAttributes 계약을 위해 내부적으로만 Map으로 변환한다.
- 차이와 영향: 외부 JSON 필드는 동일하여 동작 영향은 없다. 다만 유지보수자가 실제 공통 응답 모델의 존재를 놓칠 수 있다.
- 권장 조치: 문서 수정. DTO가 응답 계약의 중심이고 Map 변환은 WebFlux 연동 목적임을 반영한다.

## 5. Documented but Not Implemented

### Redis→DB Scheduler 동기화와 DB→Redis 복구

- 분류: 향후 구현 예정으로 보임
- 근거: `docs/redis-like-design.md`에서 계획된 구조/향후 확장으로 서술한다. `@Scheduled`, DB fallback, 복구 API는 현재 Like 코드에서 확인되지 않는다.

### Like 현재 상태 DB 테이블 사용

- 분류: 과거 설계가 문서에 남아 있는 것으로 보임
- 근거: `t_like_m01`은 DDL과 DB 문서에 있지만 Like 코드에서 사용되지 않는다. 다만 향후 DB 동기화 계획이 있어 최종 판단은 설계 결정이 필요하다.

### Docker 기반 실행 환경 및 Redis Pub/Sub 알림

- 분류: 향후 구현 예정으로 보임
- 근거: README/Architecture의 향후 개선 항목이며 Dockerfile, Docker Compose, Pub/Sub 구현 파일은 확인되지 않는다.

## 6. Implemented but Not Documented

- 다른 도메인(Post/Comment/Like)의 SecurityConfig는 GET/POST 전체를 `permitAll()`로 둔다. Security 문서가 User 모듈 한정 적용을 말하지만, 현재 공개 API 범위와 위험도는 명시하지 않는다.
- Like API는 사용자 식별자를 URL path variable로 받는다. `LikeHandler` 주석은 향후 SecurityContext에서 사용자 정보를 꺼낼 예정임을 나타낸다. 현재는 요청자가 임의 `userSn`을 넣을 수 있는 인터페이스이므로 문서화 가치가 있다.
- `SecurityErrorResponseWriter`와 `ErrorResponseFactory`는 401/403과 일반 오류의 JSON 계약을 공유하는 공통 구성요소다. 현재 Error Handling 문서의 패키지 구조도 이를 반영하지 않는다.
- `docs/codex/CHANGE_LOG.md`는 2026-08-20에 User/Post/Like 컴파일 성공 및 Comment 컴파일 실패를 기록한다. 현재 빌드 상태를 README의 실행/검증 가이드에 별도로 정리할 가치가 있다.
- 모든 `application.yml`에는 개발 DB 비밀번호가, User 설정에는 JWT secret이 평문으로 있다. 현재 로컬 개발 전제와 secret 관리 방침이 문서에 없다.

## 7. Items Requiring Confirmation

### Post/Comment Custom SQL의 실제 실행 결과

- 확인이 필요한 내용: 남아 있는 named parameter, column alias, bind key 문제가 실제 R2DBC 실행에서 오류를 내는지
- 현재 확인 가능한 사실: 소스상 Post의 날짜 parameter 표기와 `user_id ` alias, Comment의 `:size`/`limit` 불일치가 있다.
- 왜 판단할 수 없는지: 이번 검토에서는 빌드나 통합 테스트를 실행하지 않았고 해당 메서드의 모든 호출 경로도 확인하지 않았다.
- 개발자가 결정해야 할 질문: 조건별 검색과 사용자별 Comment 목록을 실제 DB에서 검증했는가? 쿼리별 기대 결과와 오류 계약은 무엇인가?

### `t_like_m01`의 장기 역할

- 확인이 필요한 내용: DB 현재 상태 테이블을 활성화할지, Redis Set과 History만을 현재 설계로 확정할지
- 현재 확인 가능한 사실: DDL/문서는 테이블을 정의하지만 코드가 사용하지 않는다.
- 왜 판단할 수 없는지: 향후 Scheduler 동기화 계획이 문서에 있어 단순 미사용 테이블인지 의도적 선행 설계인지 확정할 수 없다.
- 개발자가 결정해야 할 질문: Redis 장애·flush 이후 어떤 데이터로 현재 Like 상태를 복원할 것인가?

### User 외 모듈의 인증 적용 시점

- 확인이 필요한 내용: Post/Comment/Like API의 공개 상태가 임시인지 현재 정책인지
- 현재 확인 가능한 사실: 각 모듈 SecurityConfig는 GET/POST를 허용하고 Like는 `userSn`을 URL에서 받는다.
- 왜 판단할 수 없는지: Security 문서는 전체 서비스 공통 인증을 향후 확장으로만 설명하며, 개별 API 정책 표는 없다.
- 개발자가 결정해야 할 질문: 어느 API부터 JWT를 적용하고, 소유자 검증은 어느 계층에서 수행할 것인가?

## 8. Recommended Actions

### Priority 1

- Comment 모듈 → 누락 DTO로 컴파일 실패 기록 및 조건 검색 미구현 → DTO/기능 범위를 확정하고 clean build와 API 테스트를 수행한다.
- Like 이력 → 추가 요청이 REMOVE 이벤트로 기록됨 → 기존 데이터 의미를 확인한 후 action 값을 수정/정합성 검증한다.
- Like 데이터 기준 → Redis와 DB 현재 상태의 책임이 문서·DDL·코드에서 다름 → Source of Truth와 복구 정책을 설계 결정으로 확정한다.

### Priority 2

- Post/Comment Custom Repository → SQL parameter·alias·binding 오류 가능성 → 조건 조합별 R2DBC 통합 테스트를 추가하고 쿼리를 검증한다.
- 공개 API 범위 → User 외 GET/POST가 공개되고 Like가 path userSn을 신뢰함 → 인증 적용 로드맵과 소유자 검증 방식을 결정한다.

### Priority 3

- README/Architecture → 독립 모듈, 실행·배포 관계, Spring Boot 버전이 불명확 → 모듈별 실행 가이드를 추가한다.
- Error Handling 문서 → DTO 기반 공통 응답과 Security writer가 반영되지 않음 → 현재 클래스 구조와 응답 계약으로 갱신한다.
- 운영 문서 → 평문 설정 및 검증 범위 미기록 → 개발용 secret 처리, DB/Redis 준비, 테스트 범위를 명시한다.

## 9. Suggested Documentation Updates

- README에 네 모듈의 독립 실행 구조, 모듈별 Spring Boot 버전, DB/Redis 공유 전제와 실행 순서를 추가한다.
- Security 문서에 User 모듈의 JWT 적용 범위와 다른 모듈의 현재 공개 API 상태를 구분해 기록한다.
- Redis/DB 설계 문서에 현재 구현과 계획(Scheduler, fallback, warm-up)을 분리하고 `t_like_m01`의 상태를 명확히 한다.
- Error Handling 문서의 응답 생성 구조를 `ErrorResponseDTO`, `ErrorResponseFactory`, `SecurityErrorResponseWriter` 기준으로 갱신한다.
- Comment/Post 기능 문서에는 검색 기능을 “검증 완료”로 표시하기 전 Repository 통합 테스트 상태를 함께 기록한다.

## 10. Follow-up Review Candidates

1. Comment/Post 실행 가능성 리뷰: clean build, Router endpoint, Repository SQL, 요청 조건별 응답을 검증한다.
2. Like 데이터 정합성 리뷰: 동시 요청, Redis 유실, History action, DB 복구와 Scheduler 도입 여부를 검토한다.
3. 인증/인가 확장 리뷰: User 외 도메인의 JWT 적용, 소유자 검증, 공개 API 정책을 설계한다.
4. 테스트 설계 리뷰: Security 401/403, Token 회전, Router 등록, SQL 오류, Redis 장애에 대한 테스트 계층을 정의한다.

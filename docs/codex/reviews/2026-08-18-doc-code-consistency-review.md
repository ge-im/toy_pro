# Markdown 문서와 소스코드 정합성 검토 기록

> 이 문서는 2026-08-18 시점의 검토 스냅샷이다. 프로젝트의 공식 설계 명세가 아니며, 후속 수정과 재검토를 위한 기록으로 사용한다.

## 1. Review Overview

- 검토 일자: 2026-08-18
- 검토 목적: README 및 설계 문서의 설명이 현재 소스코드와 SQL 정의에 부합하는지 확인
- 검토 대상: 루트 README, `docs` Markdown, DB SQL, `toy_pro_user`·`toy_pro_post`·`toy_pro_comment`·`toy_pro_like` 모듈의 주요 Java/Gradle/YAML/테스트 코드
- 검토 방식: 정적 파일 검토 및 문서-구현 대조. 빌드와 테스트는 산출물 생성을 피하기 위해 실행하지 않음.
- 코드 수정 여부: 없음

## 2. Review Scope

- `README.md`
- `docs/architecture-overview.md`, `docs/security-design.md`, `docs/redis-like-design.md`, `docs/error-handling-design.md`, `docs/db-design.md`, `docs/개발로그.md`, `docs/git-strategy.md`
- `db_init.sql`, `db_data_init.sql`, `db_architech.sql`
- 각 Spring Boot 모듈의 `build.gradle`, `application.yml`
- Router/Handler, Service, Model/Repository/Mapper 구조
- Spring Security, JWT, Refresh Token 및 Redis Blacklist
- Like Redis Set 처리 및 Like History 저장
- R2DBC Repository 및 Custom Repository SQL
- Global Error Handler와 ErrorCode 구조
- 현재 존재하는 테스트 소스 및 Docker/Docker Compose 파일 존재 여부

## 3. Overall Assessment

WebFlux, R2DBC, Redis Set 기반 Like, JWT/Refresh Token 같은 기술 선택과 큰 구조는 문서와 대체로 일치한다. 반면 보안 적용 범위·권한 검사·오류 응답, Like의 DB/Redis 데이터 기준, Router 등록 여부에서는 문서가 실제 동작을 과대하게 설명하거나 실제 코드가 미완성인 부분이 확인된다.

문서는 **기술 방향과 설계 의도**를 이해하는 출발점으로는 신뢰할 수 있지만, **현재 실행 가능한 API와 보안/데이터 정합성의 사실상 명세**로 신뢰하기에는 부족하다. 우선적으로 인증/인가와 API 노출 상태를 코드 관점에서 재검토한 뒤 문서를 갱신해야 한다.

## 4. Consistent Items

- README와 Gradle 설정은 Java 21, Spring WebFlux, Spring Security, PostgreSQL/R2DBC를 공통 기술 축으로 사용한다는 점에서 일치한다.
- Like 모듈은 `ReactiveStringRedisTemplate`을 감싼 `ReactiveStringRedisExecutor`와 `LikeRedisRepository`를 통해 Redis Set의 추가·삭제·멤버 확인·크기 조회를 수행한다. 이는 Redis 설계 문서의 Set 기반 Like 상태 조회와 일치한다.
- User 모듈에는 `JwtProvider`, `JwtReactiveAuthenticationManager`, `JwtAuthenticationFilter`, `TokenRedisRepository`가 존재하며, Access Token Blacklist와 Refresh Token 저장/회전을 구현한다.
- User, Comment, Like에는 함수형 WebFlux Router/Handler가 Bean으로 등록되어 있다. 기존 `UserController`는 `@RestController`가 주석 처리된 과거 코드로, 현재 API 진입점은 Router/Handler이다.
- DB DDL의 User/Role/Post/Comment/Like History 테이블명은 해당 R2DBC Model의 `@Table` 매핑과 대체로 일치한다.
- `GlobalErrorAttributes`와 `GlobalErrorExceptionHandler`를 통한 WebFlux 전역 오류 처리 구조는 User와 Like 모듈에 구현되어 있으며, Error Handling 문서의 큰 방향과 일치한다.
- Docker 기반 실행 환경, Scheduler 기반 Redis 동기화, Redis Pub/Sub 알림은 문서에서 향후 계획으로 표기되어 있으며 관련 구현 파일은 확인되지 않았다.

## 5. Inconsistencies

### 5.1 Role 기반 인가 표현과 실제 Authority 검사

- 중요도: Critical
- 관련 Markdown 파일: `docs/security-design.md`, `docs/architecture-overview.md`
- 관련 소스 파일: `toy_pro_user/src/main/java/com/example/demo/config/security/SecurityConfig.java`, `toy_pro_user/src/main/java/com/example/demo/auth/security/JwtReactiveAuthenticationManager.java`
- 문서에 작성된 내용: 인증 후 SecurityContext의 Role을 기반으로 API별 권한을 검사하고, 권한이 없으면 403을 반환한다.
- 실제 구현 내용: `SecurityConfig`는 `hasAnyRole("ROLE_ADMIN", "ROLE_USER")`를 사용하고, `JwtReactiveAuthenticationManager`는 `ROLE_ADMIN` 또는 `ROLE_USER`를 `SimpleGrantedAuthority`로 생성한다.
- 어떤 점이 다른지: Spring Security의 `hasRole` 계열은 기본적으로 `ROLE_` 접두사를 붙여 Authority를 검사한다. 따라서 현재 설정은 `ROLE_ROLE_ADMIN`/`ROLE_ROLE_USER`를 요구하는 구성이 될 수 있다.
- 영향: 정상 JWT를 보유한 사용자도 인가 실패할 수 있어 API 접근과 권한 설계의 핵심 동작에 영향을 준다.
- 권장 조치: 코드 재검토. 실제 사용 의도가 `hasRole`인지 `hasAuthority`인지 결정하고, JWT authority 값과 Security matcher를 일치시킨 뒤 보안 테스트로 검증한다.

### 5.2 Security 오류 응답의 표준화 미구현

- 중요도: High
- 관련 Markdown 파일: `docs/security-design.md`, `docs/error-handling-design.md`
- 관련 소스 파일: `toy_pro_user/src/main/java/com/example/demo/config/security/SecurityConfig.java`, `toy_pro_user/src/main/java/com/example/demo/config/error/GlobalErrorExceptionHandler.java`
- 문서에 작성된 내용: 인증 401/인가 403은 Global Error Handler와 동일한 JSON 응답 형식으로 통일된다.
- 실제 구현 내용: `authenticationEntryPoint`와 `accessDeniedHandler`는 응답 상태 코드만 설정하며 JSON body를 작성하지 않는다.
- 어떤 점이 다른지: Security Filter 단계의 실패 응답은 일반 예외의 `status`, `code`, `message`, `time`, `path` 형식과 같지 않다.
- 영향: 클라이언트의 오류 처리 계약이 요청 경로에 따라 달라지고, 문서의 표준 오류 응답 설명을 그대로 사용할 수 없다.
- 권장 조치: 코드 재검토 및 문서 수정. 원하는 응답 계약을 정한 후 Security handler와 Global Error Handler의 역할 분담을 명시한다.

### 5.3 Refresh Token 만료 기간

- 중요도: High
- 관련 Markdown 파일: `docs/security-design.md`
- 관련 소스 파일: `toy_pro_user/src/main/resources/application.yml`, `toy_pro_user/src/main/java/com/example/demo/auth/api/handler/AuthHandler.java`, `toy_pro_user/src/main/java/com/example/demo/auth/security/JwtProvider.java`
- 문서에 작성된 내용: Refresh Token은 14일 동안 유지된다.
- 실제 구현 내용: `jwt.expireTime.refreshDate: 7`이며 JWT 만료, Redis TTL, HttpOnly Cookie Max-Age에 같은 값을 사용한다.
- 어떤 점이 다른지: 문서의 Token Lifecycle 기간과 실제 세션 유지 기간이 다르다.
- 영향: 인증 만료 정책과 사용자 경험, 운영 문의 대응 기준이 달라진다.
- 권장 조치: 문서 수정 또는 코드 재검토. 의도된 기간을 결정한 뒤 설정값을 단일한 정책으로 문서화한다.

### 5.4 Post/Auth Router Bean 미등록

- 중요도: High
- 관련 Markdown 파일: `README.md`, `docs/architecture-overview.md`, `docs/security-design.md`
- 관련 소스 파일: `toy_pro_post/src/main/java/com/example/demo/post/api/router/PostRouter.java`, `toy_pro_user/src/main/java/com/example/demo/auth/api/router/AuthRouter.java`
- 문서에 작성된 내용: Post CRUD와 Auth 로그인/재발급/로그아웃은 Router/Handler 기반 주요 기능으로 설명된다.
- 실제 구현 내용: 두 클래스의 `RouterFunction<ServerResponse>` 반환 메서드에는 `@Bean`이 없다. 반면 User/Comment/Like Router 메서드에는 `@Bean`이 있다.
- 어떤 점이 다른지: `@Configuration`만으로는 해당 RouterFunction이 Bean으로 등록되지 않는다.
- 영향: Post 및 Auth 라우트가 애플리케이션에 실제 노출되지 않을 수 있어 문서상 기능과 실행 상태가 달라진다.
- 권장 조치: 코드 재검토. 애플리케이션 컨텍스트에서 RouterFunction 등록 여부와 실제 엔드포인트 노출을 확인한 뒤 문서를 현재 상태에 맞춘다.

### 5.5 ErrorCode 중심 예외 처리와 Like 예외 처리의 차이

- 중요도: High
- 관련 Markdown 파일: `docs/error-handling-design.md`
- 관련 소스 파일: `toy_pro_like/src/main/java/com/example/demo/like/service/LikeService.java`, `toy_pro_like/src/main/java/com/example/demo/config/error/GlobalErrorAttributes.java`, `toy_pro_like/src/main/java/com/example/demo/common/error/exception/AlreadyLikedExcepction.java`
- 문서에 작성된 내용: 현재는 ErrorCode Enum → BusinessException → Global Handler를 기준으로 예외 응답을 통일한다.
- 실제 구현 내용: `LikeService.like`는 `AlreadyLikedExcepction`을, `unLike`는 `ObjectNotFoundException`을 사용한다. `GlobalErrorAttributes`는 `BusinessException`만 ErrorCode로 해석한다.
- 어떤 점이 다른지: Like의 핵심 비즈니스 오류가 문서에서 설명한 ErrorCode 흐름을 따르지 않는다.
- 영향: Like 중복·미존재 오류가 문서 예시의 코드/HTTP 상태가 아닌 내부 오류 응답으로 처리될 수 있다.
- 권장 조치: 코드 재검토. Like 예외를 ErrorCode 계약에 편입할지, 별도 예외 처리 규칙을 문서화할지 결정한다.

### 5.6 Like의 Redis/DB 책임 경계

- 중요도: High
- 관련 Markdown 파일: `docs/architecture-overview.md`, `docs/redis-like-design.md`, `docs/db-design.md`
- 관련 소스 파일: `toy_pro_like/src/main/java/com/example/demo/like/service/LikeService.java`, `toy_pro_like/src/main/java/com/example/demo/like/domain/repository/LikeRedisRepository.java`, `toy_pro_like/src/main/java/com/example/demo/like/domain/repository/LikeHistoryRepository.java`
- 문서에 작성된 내용: Like 현재 상태는 DB를 최종 기준으로 두고 Redis를 캐시로 사용하며, Domain은 Redis/DB에 직접 의존하지 않는 방향으로 설명된다.
- 실제 구현 내용: `LikeService`는 Redis 상태를 직접 변경·조회하고 Like History만 DB에 저장한다. `LikeRedisRepository`는 `domain.repository` 패키지에 있으나 Redis executor에 직접 의존한다.
- 어떤 점이 다른지: `t_like_m01` 현재 상태와 Redis 캐시의 동기화 구현은 없으며, 실제 현재 상태의 운영 기준은 Redis에 가깝다.
- 영향: Redis 유실 시 현재 Like 상태를 즉시 복원할 근거가 없고, 문서의 DB Source of Truth 설명으로 장애/복구 동작을 판단할 수 없다.
- 권장 조치: 코드 재검토와 문서 수정. 현재 상태의 Source of Truth, History의 역할, Redis 장애 시 정책을 확정한다.

### 5.7 단일 프로젝트 설명과 실제 독립 모듈 구성

- 중요도: Medium
- 관련 Markdown 파일: `README.md`, `docs/architecture-overview.md`
- 관련 소스 파일: `toy_pro_user/build.gradle`, `toy_pro_post/build.gradle`, `toy_pro_comment/build.gradle`, `toy_pro_like/build.gradle`
- 문서에 작성된 내용: 프로젝트를 User/Post/Comment/Like 도메인 중심으로 구성한 Community API로 소개한다.
- 실제 구현 내용: 저장소에는 독립 Gradle/Spring Boot 프로젝트 네 개가 있으며 Spring Boot 버전도 User 3.4.3, Post/Comment 3.5.5, Like 4.0.1로 다르다.
- 어떤 점이 다른지: 단일 애플리케이션인지 별도 실행 모듈의 집합인지, 모듈 간 호출·배포 관계가 문서에 드러나지 않는다.
- 영향: 처음 보는 개발자가 실행 방법, 인증 적용 범위, 데이터 공유와 배포 단위를 잘못 이해할 수 있다.
- 권장 조치: 문서 수정. 모듈별 실행 단위, 포트, 데이터베이스/Redis 공유 여부, 현재 통합 방식과 장기 계획을 명시한다.

## 6. Documented but Not Implemented

### Like 현재 상태 DB 저장소 (`t_like_m01`)

- 분류: 과거 설계가 문서에 남아 있는 것으로 보임
- 근거: `db_architech.sql`과 `docs/db-design.md`는 `t_like_m01`을 현재 상태로 정의한다. 그러나 Like 모듈에는 이 테이블의 Entity/Repository가 없고, `LikeService`는 Redis와 `t_like_h01` 이력만 사용한다.

### Redis→DB Scheduler 동기화 및 DB→Redis 복구

- 분류: 향후 구현 예정으로 보임
- 근거: `docs/redis-like-design.md`가 Scheduler, 정합성 체크, fallback 복구 API를 계획된 구조와 향후 확장으로 표시한다. `@Scheduled`, 복구 API, DB fallback 구현은 검토 범위에서 확인되지 않았다.

### Redis 장애 시 DB Fallback/Degraded Mode

- 분류: 미구현
- 근거: Redis 설계 문서는 장애 시 DB 조회 fallback을 설명하지만 `LikeService`의 조회/변경 경로에는 오류 복구나 DB 조회 분기가 없다.

### Docker 기반 실행 환경과 Redis Pub/Sub 알림

- 분류: 향후 구현 예정으로 보임
- 근거: README와 Architecture Overview의 향후 개선 항목으로 명시되어 있다. Dockerfile, Docker Compose, Pub/Sub 구현은 확인되지 않았다.

### Comment 조건 검색

- 분류: 미구현
- 근거: `CommentService.findAllByCondition`은 `null`을 반환한다.

## 7. Implemented but Not Documented

- 모듈별 보안 적용 범위: Like/Post/Comment SecurityConfig는 GET과 POST 전체를 `permitAll()`로 두고, User만 JWT filter와 세부 권한 matcher를 둔다. 현재 보안 경계를 별도 문서화할 가치가 있다.
- Like API의 사용자 식별자 전달 방식: `LikeRouter`와 `LikeHandler`는 `userSn`을 URL path variable로 받으며, 코드 주석은 향후 SecurityContext 방식으로 변경할 계획을 나타낸다. 현재 권한 위임 위험과 임시 설계를 명시해야 한다.
- Refresh Token 재사용 감지: `AuthService.reissue`는 토큰 저장값이 예상 사용자와 다르면 해당 사용자 refresh key prefix를 삭제한다. 토큰 재사용 대응 정책으로 문서화 가치가 있다.
- Redis Key 규칙: `TokenRedisKeyGenerator`는 `auth:refresh:{userSn}:{jti}`, `auth:blacklist:access:{jti}`를 사용하고, Like Key는 `like:{P|C}:{targetSn}`이다.
- 운영 설정 상태: DB 계정 비밀번호와 JWT secret이 각 `application.yml`에 평문으로 존재한다. 환경변수/secret 관리 정책과 로컬 개발 전제의 문서화가 필요하다.
- 테스트 범위: Context 로드 테스트가 주를 이루고 Like 모듈에는 Redis 연결 테스트가 있다. 인증·인가, Router 등록, Redis 장애·정합성, 토큰 회전의 자동 테스트는 확인되지 않았다.

## 8. Items Requiring Confirmation

### Auth 로그인 SQL의 실행 가능성

- 확인이 필요한 내용: 현재 로그인 기능이 실제 DB에서 성공하는지
- 현재 확인 가능한 사실: `AuthUserRepository.findByLoginId`의 SQL에는 `INNER JOIN` 뒤 대상 테이블과 조인 조건이 없다.
- 왜 판단할 수 없는지: 이 쿼리가 아직 실행되지 않았는지, 실제 DB 스키마/배포 코드가 저장소와 다른지 정적 검토만으로 확정할 수 없다.
- 개발자가 결정해야 할 질문: 현재 배포/로컬 환경에서 Auth 라우트와 로그인 SQL을 실행했는가? 해당 SQL의 의도된 조인 대상은 무엇인가?

### Post/Comment Repository SQL과 DDL의 컬럼명 차이

- 확인이 필요한 내용: `p.red_dt` 사용, 일부 named parameter와 column alias가 실제로 동작하는지
- 현재 확인 가능한 사실: DDL은 `reg_dt`를 정의하지만 Post Repository와 Custom Repository 일부는 `red_dt`를 조회한다. Comment Custom Repository의 SQL은 `:size`를 사용하면서 값은 `limit` 키로 bind한다.
- 왜 판단할 수 없는지: 실제 DB가 DDL과 다른 버전일 수 있고, 해당 메서드가 호출되는 경로가 존재하는지 빌드/실행 없이 확정할 수 없다.
- 개발자가 결정해야 할 질문: DDL이 현재 운영/개발 DB의 기준인가? 각 목록/검색 API를 실제로 통합 테스트했는가?

### Like 이력의 Action Type

- 확인이 필요한 내용: Like 추가 시 `REMOVE("D")`가 저장되는 것이 의도인지
- 현재 확인 가능한 사실: `LikeActionType`은 `ADD("A")`, `REMOVE("D")`를 정의하지만 `LikeService.like`와 `unLike` 모두 `REMOVE`를 저장한다.
- 왜 판단할 수 없는지: 과거 데이터 해석 규칙이나 별도 소비자가 있는지 확인되지 않았다.
- 개발자가 결정해야 할 질문: `t_like_h01.action_type`의 실제 이벤트 의미와 기존 데이터 정합성 기준은 무엇인가?

### Comment 모듈의 컴파일 상태

- 확인이 필요한 내용: `CommentSearchRequestDTO` import가 실제로 해소되는지
- 현재 확인 가능한 사실: `CommentService`는 해당 타입을 import하지만 검토한 소스 목록에서 정의 파일을 찾지 못했다.
- 왜 판단할 수 없는지: 생성 소스, 로컬 미추적 파일, 또는 현재 브랜치 외 의존 여부는 정적 파일만으로 확인할 수 없다.
- 개발자가 결정해야 할 질문: 현 브랜치에서 clean build가 성공하는가? 조건 검색 DTO의 현재 명세는 무엇인가?

## 9. Recommended Actions

### Priority 1

- User SecurityConfig/JWT Authority → Role 검사 표현이 불일치할 수 있음 → `hasRole`/`hasAuthority`와 JWT Authority를 통일하고 인증·인가 테스트를 추가한다.
- Post/Auth Router 및 Auth SQL → 문서상 핵심 API가 노출 또는 로그인되지 않을 수 있음 → Router Bean 등록과 로그인 SQL을 실제 실행 경로에서 검증한다.
- Like 데이터 기준 → Redis 상태만 사용하고 이력 action 값도 의심됨 → Source of Truth와 Like History 이벤트 의미를 확정하고 장애/복구 정책을 결정한다.

### Priority 2

- Security 오류 처리 → 문서의 공통 오류 JSON 계약과 실제 상태 전용 응답이 다름 → Security 실패 응답 정책을 정하고 구현과 문서를 맞춘다.
- Post/Comment Repository SQL → DDL과 컬럼명·binding 불일치 가능성 → 현재 DDL 기준으로 목록/검색 쿼리를 통합 테스트한다.
- Comment 조건 검색 → Service 메서드가 미구현 → 기능 범위를 확정하고 구현 상태를 문서에 반영한다.

### Priority 3

- README/Architecture → 독립 모듈 구성과 버전 차이가 설명되지 않음 → 모듈별 실행·배포 단위와 보안 적용 범위를 추가한다.
- Redis 설계 문서 → 계획과 현재 구현의 구분이 약함 → 현재 구현, 미구현 복구 기능, 장기 계획을 명확히 분리한다.
- 운영/테스트 문서 → 평문 설정과 제한적인 테스트 범위가 드러나지 않음 → 환경변수/secret 정책 및 테스트 범위를 기록한다.

## 10. Suggested Documentation Updates

- README에 네 독립 Spring Boot 모듈의 역할, 실행 명령, 포트, Spring Boot 버전, 공용 DB/Redis 전제를 추가한다.
- Security 문서에 현재 JWT가 User 모듈에만 적용된다는 점과 다른 모듈의 GET/POST 공개 상태를 현재 구현 기준으로 명시한다.
- Redis Like 문서에 현재 상태가 Redis Set에 있고 `t_like_m01`을 코드가 사용하지 않는 현황을 명시하거나, 설계 확정 후 문서를 수정한다.
- Error Handling 문서에 Security 401/403 body의 실제 계약과 Like 예외의 현재 처리 규칙을 반영한다.
- 실행 가이드 문서에 PostgreSQL/Redis 준비, SQL 적용 순서, Docker 미제공 상태, 비밀정보 외부화 필요성을 기록한다.
- 테스트 가이드 문서에 현재 테스트 범위와 우선 추가할 인증/인가·Router·Redis 정합성 테스트를 정리한다.

## 11. Follow-up Review Candidates

1. 인증/인가 코드 리뷰: JWT 유형 검증, Authority/Role 매핑, logout/reissue 보안 경계, 공개 API 범위를 집중 검토한다.
2. Like 데이터 정합성 리뷰: Redis 장애, 중복 요청 경합, History 이벤트 의미, DB 복구와 `t_like_m01`의 존치 여부를 검토한다.
3. 실행 가능성 리뷰: 각 모듈 clean build, Router Bean 등록, R2DBC SQL, 애플리케이션 기동과 API smoke test를 확인한다.
4. 예외 처리 리뷰: 공통 ErrorCode 계약, Security 오류 응답, 모듈 간 예외 클래스 차이를 정리한다.
5. 테스트 설계 리뷰: 핵심 API 동작, 권한 경계, 토큰 회전, Redis/DB 장애 및 Repository SQL을 검증할 테스트 계층을 설계한다.

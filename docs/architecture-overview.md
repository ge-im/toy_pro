# Architecture Overview

## 1. 프로젝트 개요

본 프로젝트는 WebFlux 기반으로 사용자(User), 게시글(Post), 댓글(Comment), 좋아요(Like) 기능을 각각 독립적인 서비스 형태로 구현한 토이 프로젝트입니다.

단순 CRUD 기능 구현과 WebFlux 및 Security(Filter 기반 인증 구조)를 중심으로 개발 경험을 쌓는 것을 주요 목표로 기능 단위 개발을 진행하면서,   
추후 기능 확장이나 공통 기능 정리를 고려하여 
초기 단계부터 패키지 구조는 어느 정도 기준을 잡고 시작했습니다.

특히 Like 기능 개발 시점에서 Redis 연동과 함께 전체 구조를 한 번 정리하게 되었고,
이후 프로젝트에서도 이 구조를 기준으로 점진적으로 맞춰가고 있습니다.

---

## 2. 개발 및 구조 개선 흐름

```
User → Post → Comment → Like -> User(Authentication/Authorization)
```

User
- WebFlux 기반 CRUD 구현
- Controller → Router / Handler 구조 전환 경험
- Paging / 정렬 처리 방식 개선 
- (+) 인증/인가 기능 추가 적용

Post 
- 검색 / Paging / 정렬 기능 확장
- Repository Custom Query 구조 적용 

Comment
- 계층형 조회 쿼리 적용
- 공통 Util / Paging 처리 방식 정리 

Like 
- Redis 연동
- Domain / Infra 분리 구조 정리
- 공통 Exception / ErrorCode 구조 정리
- Reactive Redis Executor 분리

<br/>

도메인 구조적으로 Like 기능 구현 시점에서 구조적으로 가장 많은 정리가 이루어졌으며,
현재는 Like 기준 구조를 기반으로 다른 도메인에도 점진적으로 반영하고 있습니다.

인증/인가 기능은 user 도메인에 한정 적용되었으며, 향후 전 서비스 공통 인증으로 확장 예정입니다.

---

## 3. 전체 아키텍처 방향

### 3.1 기본 구조
본 프로젝트는 다음 구조를 기준으로 구성되어 있습니다.
```
Security Layer (WebFlux Filter) : JWT Token 기반 인가
↓
API Layer (Router / Handler) : 요청 / 응답 처리 및 입력값 검증
↓
Service Layer : 비즈니스 로직 처리 및 여러 도메인/저장소 조합
↓
Domain Layer (Model / Repository) : Entity 및 Repository 인터페이스 정의
↓
Infra Layer (Redis, External Config) : DB, Redis 등 외부 시스템 연동
```

### 3.2 Domain 중심 패키지 구성
```
common
config
infra
각 도메인 패키지 (user / post / comment / like)
```

--- 

## 4. 주요 설계 기준

### 4.1 API Layer – Router / Handler 구조

WebFlux 환경에서 Controller 대신 Router / Handler 구조를 사용했습니다.

목적:
- Reactive 흐름 유지
- 테스트 단위 분리
- Handler 단위 비즈니스 진입 명확화

### 4.2 Domain Layer

각 도메인은 다음 기준으로 구성했습니다.

```
domain
 ├ model
 ├ repository
 └ mapper (필요 시)
```
목적:
- DB 접근 책임 분리
- Service 의존 최소화

### 4.3 Infra Layer

Redis 연동과 같은 외부 시스템을 사용하면서,
도메인 로직과 기술적인 설정 코드가 섞이지 않도록
Infra 패키지를 분리하게 되었습니다.

특히 Like 기능에서 Redis 사용 비중이 커지면서,
Config, Executor, Key 생성 로직을 분리하는 구조로 정리했습니다.

예시:
- Redis Config
- Reactive Redis Executor
- Redis Key Generator

목적:
- 도메인 로직과 외부 기술 분리
- Redis 설정 변경 또는 확장 시 영향 범위를 줄이기 위함

### 4.4 공통 Exception / ErrorCode 구조

초기에는 Custom Exception 위주였지만,
현재는 ErrorCode Enum 중심 구조로 정리했습니다.

목적:
- 에러 관리 기준 통일
- 비즈니스 로직 단순화

### 4.5 Security 구조

본 프로젝트는 WebFlux Security 기반으로
인증(Authentication)과 인가(Authorization)를 처리합니다.  
인증 과정은 Controller 이전 단계에서
Security Filter Chain을 통해 수행됩니다.

#### 인증 처리 흐름

```
Client Request
↓
Security WebFilter Chain
↓
JwtAuthenticationWebFilter
↓
ReactiveAuthenticationManager
↓
Authentication 성공 시 SecurityContext 저장
↓
Handler (비즈니스 로직 진입)
```

인증 실패 시에는 Handler로 진입하지 않고 Security 단계에서 바로 401 응답을 반환합니다.

#### 인가 처리

인가(Authorization)는 인증 이후
SecurityContext에 저장된 사용자 정보를 기반으로 수행됩니다.

권한 검증 실패 시 AccessDeniedException이 발생하며,
403 응답이 반환됩니다.

#### 설계 목적

- 인증/인가 로직과 비즈니스 로직 분리
- Handler / Service 코드 단순화
- 공통 인증 처리 구조 통일

---

## 5. Redis 적용

현재 Redis는 도메인별 역할에 따라 분리하여 사용하고 있습니다.

- Like 서비스 : 현재 상태 조회를 위한 캐시 (Set 기반 구조)
- Auth 서비스 : 인증 상태 관리 (Token Lifecycle 관리)

각 도메인의 Redis 사용 목적이 다르기 때문에
데이터 구조 및 TTL 전략 또한 도메인별로 다르게 설계합니다.

또한 Redis Key Generator를 분리하여 Key 규칙 변경 시 영향 범위를 최소화했습니다.

### 5.1 Like 서비스

Like 서비스에서는 Redis를 다음 목적으로 사용했습니다.
- 좋아요 현재 상태 캐싱
- 빠른 상태 조회 처리

Like 이벤트 히스토리는 DB에 저장하며, Redis는 현재 상태 조회 성능 개선을 위한 용도로 사용했습니다.  
Redis 장애 또는 데이터 유실 상황을 대비하여,
최종 데이터 기준은 DB를 기준으로 설계했습니다.

Like 데이터는 현재 상태를 나타내는 정보이므로 TTL을 설정하지 않고,
Redis 데이터가 곧 현재 상태가 되도록 설계했습니다.

### 5.2 Auth 서비스

Auth 서비스에서는 Redis를 다음 목적으로 사용했습니다.
- Refresh Token 저장
- Access Token Blacklist 저장

해당 Token 데이터는 로그인/로그아웃 상태 유지를 위해 사용됩니다.  
각 Token은 자신의 만료 시간에 맞춰 TTL이 설정되며,
Token의 남은 유효시간과 동일하게 Redis TTL을 설정합니다.

이를 통해 별도의 정리 작업 없이도
Token 만료 시 자동으로 Redis 데이터가 제거되도록 설계했습니다.


---

## 6. 현재 상태와 개선 방향

Like 서비스는 프로젝트 중 가장 마지막에 개발된 기능으로,
이전 도메인에서 겪었던 구조적인 고민들을 반영하여
가장 정리된 형태로 구성되어 있습니다.

현재 기존 User / Post / Comment 서비스는 점진적으로 구조를 맞춰갈 예정입니다.

추가 개선 사항  :
- Spring Cloud 기반 서비스 분리(학습 및 실험 목적)
- 이벤트 기반 구조 도입
- Redis Pub/Sub을 활용한 알림 기능
- Docker 기반 실행 환경 정리

<br/>

본 프로젝트는 처음부터 완벽한 구조 설계를 목표로 하기보다,   
기능 구현 → 구조 개선 → 공통화 과정으로 발전해온 프로젝트입니다.  
기능 완성보다는 구조를 이해하고 개선해 나가는 과정에 중점을 두고 있습니다.

---

## 7. 프로젝트를 통해 얻은 점

기술 경험 :
- WebFlux 구조 이해
- Reactive 환경에서 Repository / Service 분리 경험
- Redis 연동 및 Key 설계 경험
- 공통 Exception 구조 설계 경험
- Infra / Domain 분리 기준 정리 경험

<br/>

느낀 점 :
- 구조가 정리되어 있을수록 기능 추가가 수월함
- 공통 코드가 많아질수록 관리가 어려워짐
- 외부 시스템은 최대한 한 곳에서 관리하는 것이 좋음

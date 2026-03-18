# Security Design (Authentication / Authorization)

## 1. 설계 배경

초기 프로젝트에서는 인증/인가 기능 없이
각 도메인의 CRUD 기능 구현과 WebFlux 기반 개발에 집중했습니다.

이후 프로젝트를 진행하면서, 로그인/로그아웃 기능과 더불어  
WebFlux 환경에서 인증/인가가 어떤 방식으로 처리되는지에 대한 구조적인 이해가 필요하다고 판단했습니다.

특히 기존 Servlet 기반 Security와 달리,
WebFlux 환경에서는 Filter 기반의 비동기 처리 구조를 사용하기 때문에
인증 흐름이 어떻게 동작하는지 직접 설계하고 적용해보는 것을 목표로 했습니다.

이에 따라 본 프로젝트에서는 JWT 기반 인증 방식을 적용하고,
WebFlux Security의 Filter Chain 구조를 활용하여
인증(Authentication)과 인가(Authorization) 흐름을 직접 구현했습니다.

본 설계는 단순 기능 구현을 넘어,
Reactive 환경에서의 인증 처리 흐름과 구조를 이해하는 데 중점을 두고 있습니다.

---

## 2. 전체 구조 개요

현재 인증/인가 로직은 user 도메인을 기준으로 우선 적용되어 있으며,  
전체 서비스에 대한 인증 처리는 향후 Gateway(Spring Cloud Gateway) 또는 공통 인증 모듈로 분리하여 확장할 계획입니다.

본 프로젝트는 WebFlux Security 기반으로
Filter 중심의 인증 구조를 사용합니다.

```
Client Request
↓
Security WebFilter Chain
↓
JwtAuthenticationWebFilter
↓
ReactiveAuthenticationManager
↓
SecurityContext 저장
↓
Handler (비즈니스 로직)
```


인증은 Controller 이전 단계에서 수행되며, 인증 실패 시 Handler로 진입하지 않습니다.

---

## 3. 인증(Authentication) 구조

### 3.1 JWT 기반 인증

본 프로젝트는 Stateless 인증 방식을 위해 JWT를 사용합니다.

구성 요소:

- Access Token
- Refresh Token

Access Token은 요청 인증에 사용되며, Refresh Token은 Access Token 재발급에 사용됩니다.

<br/>

### 3.2 JwtAuthenticationWebFilter

- 요청 Header에서 JWT 추출
- 토큰 유효성 검증 요청
- AuthenticationManager로 전달

역할:
- 인증 진입 지점
- Token → Authentication 변환 시작

#### Filter 기반 인증 처리 이유

WebFlux 환경에서는 요청 처리가 Reactive Stream 기반으로 동작하기 때문에  
Controller 이전 단계에서 인증을 처리하는 것이 자연스럽습니다.

특히 인증 정보는 이후 모든 비즈니스 로직에서 공통적으로 사용되므로,  
Handler 진입 이전에 인증을 완료하는 구조가 필요합니다.

JwtAuthenticationWebFilter는 이러한 구조에서
인증의 시작 지점 역할을 수행하며,
모든 요청에 대해 공통 인증 로직을 적용할 수 있도록 합니다.

<br/>

### 3.3 ReactiveAuthenticationManager

- JWT 검증 (서명, 만료시간)
- Redis Access Token Blacklist 확인
- 인증 객체 생성

검증 실패 시 인증 실패 처리

#### 인증 검증 책임 분리

ReactiveAuthenticationManager는 실제 인증 여부를 판단하는 핵심 컴포넌트입니다.

JwtAuthenticationWebFilter가 Token을 추출하는 역할이라면, AuthenticationManager는 다음과 같은 검증을 수행합니다.

- JWT 서명 검증
- 토큰 만료 여부 확인
- Redis Blacklist 여부 확인

이처럼 인증 관련 검증 로직을 한 곳에 집중시킴으로써 Filter와 인증 로직의 책임을 분리하고, 유지보수성을 높이도록 설계했습니다.

<br/>

### 3.4 SecurityContext 저장

인증 성공 시 Authentication 객체를 생성하여 SecurityContext에 저장합니다.

이후 Handler / Service 계층에서는 SecurityContext를 통해 사용자 정보를 조회할 수 있습니다.

---

## 4. 인가(Authorization) 구조

인가 처리는 인증 이후 수행됩니다.

- 사용자 권한(Role) 기반 접근 제어
- 요청 API별 권한 검증

권한 검증 실패 시 403 응답 반환

---

## 5. Token 관리 전략

### 5.1 Access Token

- 짧은 만료 시간 (30 min) 사용
- Stateless 유지

<br/>

### 5.2 Refresh Token

- Redis에 저장
- 사용자 로그인 상태 유지 (14 days)

<br/>

### 5.3 Access Token Blacklist

로그아웃 시 Access Token을 Redis Blacklist에 저장합니다.

- Key: auth:blacklist:access:{jti}
- TTL: Access Token 남은 만료 시간

이를 통해 이미 발급된 Token이라도 강제로 인증을 무효화할 수 있습니다.

<br/>

### 5.4 Token 전략 설계 이유

Access Token과 Refresh Token을 분리한 이유는 다음과 같습니다.

- Access Token
  - 짧은 만료 시간을 통해 보안성 강화
  - Stateless 인증 유지

- Refresh Token
  - 장기 인증 상태 유지
  - 재로그인 없이 Access Token 재발급 가능

또한 로그아웃 시 Access Token을 즉시 무효화하기 위해 Redis Blacklist 방식을 사용했습니다.

이를 통해 Stateless 구조를 유지하면서도 강제 로그아웃 및 토큰 제어가 가능하도록 설계했습니다.

---

## 6. Redis 활용 (Auth)

Auth 영역에서 Redis는 다음 용도로 사용됩니다.

- Refresh Token 저장
- Access Token Blacklist 관리

모든 Token 데이터는 TTL 기반으로 관리되며, Token 만료 시 자동 삭제됩니다.

---

## 7. Security Error 처리

- 인증 실패 → 401 (Unauthorized)
- 인가 실패 → 403 (Forbidden)

Security Filter 단계에서 1차 처리되며,
응답 형식은 Global Error Handler 기준에 맞춰 통일됩니다.

---

## 8. 설계 의도

- 인증 로직과 비즈니스 로직 분리
- Stateless 기반 확장성 확보
- Redis를 활용한 인증 상태 제어
- WebFlux 환경에 맞는 비동기 인증 처리 구조

---

## 9. 향후 개선 방향

- OAuth2 로그인 연동
- 토큰 재발급 정책 고도화
- Redis 장애 대응 전략 추가
- 인증/인가 MSA 분리
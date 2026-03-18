# Error Handling Design (Draft)

## 1. 설계 배경

초기 프로젝트(User, Post 개발 단계)에서는
각 기능에서 Custom Exception을 직접 정의하여 사용하는 구조를 사용했습니다.

하지만 기능이 늘어나면서 다음과 같은 문제를 예상했습니다.
- 예외 클래스가 계속 증가
- 에러 응답 형식이 서비스별로 조금씩 달라짐
- 공통 에러 관리 기준이 없음

특히 Like 기능 개발 시점에서 
Redis, 외부 시스템, 비즈니스 로직 예외가 함께 발생할 수 있고, 
Security 인증/인가 과정에서 발생하는 예외에 대한 전역 기준이 필요하다고 판단했습니다.

그래서 현재는
- ErrorCode Enum 중심 구조
- WebFlux 전역 Error Handler 구조

로 정리했습니다.

---

## 2. 전체 구조 개요

현재 Error Handling 구조는 다음 기준으로 구성했습니다.

```
common.error
 ├ code
 │  ├ ErrorCode
 │  ├ BusinessErrorCode     (Business + Security)
 │  └ HttpErrorCode         (Http)
 ├ exception
 │  ├ BusinessException
 │  └ HttpException

config.error
 ├ GlobalErrorAttributes
 └ GlobalErrorExceptionHandler
```

---

## 3. ErrorCode Enum 중심 구조

### 3.1 변경 이유

초기:
```
Custom Exception 여러 개 생성
```

현재:
```
ErrorCode Enum → Exception → Global Handler → Response(Map) 생성
```

변경 목적:
- Response Format 통일
- 비즈니스 로직 단순화
- Exception 클래스 증가 방지
- 에러 코드 관리 일원화

### 3.2 ErrorCode 구성 기준

#### Business Error

- 도메인 비즈니스 로직 실패
- 예: 좋아요 중복, 데이터 없음

#### Http Error

- 외부 API / 통신 오류
- HTTP 상태 기반 오류

---

## 4. Exception 구조

현재는 Exception을 ErrorCode 기반으로 크게 두 가지로 나누었습니다.

### 4.1 BusinessException

- 서비스 내부 비즈니스 로직 오류
- 예시 :
    - Object Not Found
    - Invalid State
    - Duplicate Request

### 4.2 HttpException

- 외부 API 호출 실패
- HTTP 상태 코드 기반 처리

---

## 5. WebFlux 전역 Error 처리 구조

WebFlux 환경에서는 기존 @ExceptionHandler 대신
다음 구조를 사용했습니다.
```
GlobalErrorAttributes
GlobalErrorExceptionHandler
```

### 5.1 GlobalErrorAttributes 역할

- Exception → Error Response 데이터 변환
- ErrorCode 기반 Response 구성

### 5.2 GlobalErrorExceptionHandler 역할

- WebFlux 전역 에러 처리
- Response 생성 및 반환

---

## 6. Error Response 설계 기준

에러 응답은 다음 기준으로 통일했습니다.

예시:
```
{
  "code": "LIKE_ALREADY_EXISTS",
  "message": "Like already exists",
  "status": 400,
  "time" : "2026-02-05T18:47:36.965995600",
  "path" : "/likes/P/1"
}
```

목적:
- 클라이언트 처리 단순화
- 추후 MSA 시스템간 에러 코드 공유
- 로그 추적 용이성
- 운영 대응 속도 개선

--- 

## 7. Security Error 처리 방향

Security 관련 에러는 다음 기준으로 처리합니다.

- 인증 실패 → 401 (Unauthorized)
- 인가 실패 → 403 (Forbidden)

WebFlux Security 환경에서는 인증/인가 과정이
Controller 이전 단계인 Security Filter Chain에서 수행됩니다.

따라서 Security 영역에서 1차적으로 예외를 처리하고,
최종적으로는 Global Error Handler와 동일한 응답 구조를 유지하도록 설계했습니다.

---

## 8. Security Error 처리 흐름

WebFlux Security 환경에서는 일반 Controller 예외 처리와 
인증/인가 과정의 예외 처리 흐름이 다르게 동작합니다.  
인증 과정은 Controller 이전 단계인 Security Filter Chain에서 수행됩니다.  
따라서 인증 실패 예외는 일반 Business Exception과
다른 흐름으로 처리됩니다.

### 인증 실패 흐름

```
Request
↓
Security WebFilter Chain
↓
JwtAuthenticationWebFilter
↓
AuthenticationManager
↓
Authentication 실패
↓
AuthenticationEntryPoint
↓
Error Response 생성
```

인증 실패의 경우 401 상태 코드가 반환됩니다.

### 인가 실패 흐름

```
Request
↓
SecurityContext 조회
↓
권한 검증
↓
AccessDeniedException
↓
AccessDeniedHandler
↓
403 Response 반환
```

인가 실패의 경우 403 상태 코드가 반환됩니다.

### Global Error Handler와의 관계

Security Filter 단계에서 처리되지 않은 예외는 기존 WebFlux Global Error Handler로 전달되어 일반 Exception과 동일한 응답 포맷으로 변환됩니다.

이를 통해 시스템 전체에서 동일한 Error Response 구조를 유지할 수 있습니다.

---

## 9. 현재 구조 선택 이유

현재 구조는 다음 기준을 고려했습니다.

- 기능 개발 시 예외 처리 코드 최소화
- ErrorCode 기준으로 에러 관리
- WebFlux 환경과 자연스럽게 연결
- 서비스 간 에러 처리 방식 통일

---

## 10. 적용하면서 느낀 점

- ErrorCode 기준이 있으면 기능 개발이 편해짐
- Exception 클래스는 최소한으로 유지하는 것이 좋음
- 전역 Error Handler는 초기에 잡는 것이 좋음
- Security Error는 일반 Exception과 다르게 접근해야 함

---

## 11. 향후 개선 가능 방향

- 서비스 간 공통 ErrorCode 관리 구조 도입
- ErrorCode → Logging 시스템 연동
- ErrorCode → Monitoring (알람) 연동
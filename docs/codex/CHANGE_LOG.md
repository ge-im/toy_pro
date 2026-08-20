# 변경 로그

## 2026-08-18 20:30 KST — Security 401/403 공통 에러 응답 적용

### 사용자 요청 요약

Security에서 처리되는 401 및 403 응답에도 기존 전역 에러 처리 방식과 동일한 JSON 응답 형식을 적용한다.

### 수정 파일 목록

- `toy_pro_user/src/main/java/com/example/demo/common/error/code/HttpErrorCode.java`
- `toy_pro_user/src/main/java/com/example/demo/config/error/ErrorResponseFactory.java`
- `toy_pro_user/src/main/java/com/example/demo/config/error/GlobalErrorAttributes.java`
- `toy_pro_user/src/main/java/com/example/demo/config/security/SecurityErrorResponseWriter.java`
- `toy_pro_user/src/main/java/com/example/demo/config/security/SecurityConfig.java`

### 수정 내용 요약

- 401 및 403 상태에 대응하는 `UNAUTHORIZED`, `FORBIDDEN` 공통 에러 코드를 추가했다.
- 전역 에러 응답의 `status`, `code`, `message`, `time`, `path` 생성 로직을 `ErrorResponseFactory`로 분리했다.
- `GlobalErrorAttributes`가 공통 생성기를 사용하도록 변경했다.
- Security의 `AuthenticationEntryPoint`와 `AccessDeniedHandler`가 `SecurityErrorResponseWriter`를 통해 동일한 형식의 JSON 응답을 작성하도록 변경했다.

### 테스트 또는 빌드 수행 결과

- `./gradlew.bat compileJava` 성공
- 기존 MapStruct 매핑 경고 4건 및 unchecked operation 안내만 출력되었으며, 이번 변경에 따른 컴파일 오류는 없었다.

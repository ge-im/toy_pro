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

## 2026-08-20 17:18 KST — 공통 ErrorResponseDTO 및 ErrorCode 기반 예외 처리 통일

### 사용자 요청 요약

미사용 `ErrorResponseDTO`를 전역 에러 응답 Map과 동일한 필드 구조로 변경하고 실제 응답 body로 사용한다. User, Post, Comment, Like 네 프로젝트에서 ErrorCode 기반 예외 처리를 적용한다.

### 수정 파일 목록

- `toy_pro_user/src/main/java/com/example/demo/common/dto/ErrorResponseDTO.java`
- `toy_pro_user/src/main/java/com/example/demo/common/error/code/BusinessErrorCode.java`
- `toy_pro_user/src/main/java/com/example/demo/common/error/code/HttpErrorCode.java`
- `toy_pro_user/src/main/java/com/example/demo/common/util/RequestParameterUtil.java`
- `toy_pro_user/src/main/java/com/example/demo/config/error/ErrorResponseFactory.java`
- `toy_pro_user/src/main/java/com/example/demo/config/error/GlobalErrorAttributes.java`
- `toy_pro_user/src/main/java/com/example/demo/config/error/GlobalErrorExceptionHandler.java`
- `toy_pro_user/src/main/java/com/example/demo/config/security/SecurityErrorResponseWriter.java`
- `toy_pro_post/src/main/java/com/example/demo/common/dto/ErrorResponseDTO.java`
- `toy_pro_post/src/main/java/com/example/demo/common/error/code/BusinessErrorCode.java`
- `toy_pro_post/src/main/java/com/example/demo/common/error/code/HttpErrorCode.java`
- `toy_pro_post/src/main/java/com/example/demo/common/util/RequestParameterUtil.java`
- `toy_pro_post/src/main/java/com/example/demo/config/error/GlobalErrorAttributes.java`
- `toy_pro_post/src/main/java/com/example/demo/config/error/GlobalErrorExceptionHandler.java`
- `toy_pro_post/src/main/java/com/example/demo/post/service/PostService.java`
- `toy_pro_comment/src/main/java/com/example/demo/comment/service/CommentService.java`
- `toy_pro_comment/src/main/java/com/example/demo/common/dto/ErrorResponseDTO.java`
- `toy_pro_comment/src/main/java/com/example/demo/common/error/code/BusinessErrorCode.java`
- `toy_pro_comment/src/main/java/com/example/demo/common/error/code/HttpErrorCode.java`
- `toy_pro_comment/src/main/java/com/example/demo/common/util/RequestParameterUtil.java`
- `toy_pro_comment/src/main/java/com/example/demo/config/error/GlobalErrorAttributes.java`
- `toy_pro_comment/src/main/java/com/example/demo/config/error/GlobalErrorExceptionHandler.java`
- `toy_pro_like/src/main/java/com/example/demo/common/dto/ErrorResponseDTO.java`
- `toy_pro_like/src/main/java/com/example/demo/common/error/code/BusinessErrorCode.java`
- `toy_pro_like/src/main/java/com/example/demo/common/error/code/HttpErrorCode.java`
- `toy_pro_like/src/main/java/com/example/demo/common/util/RequestParameterUtil.java`
- `toy_pro_like/src/main/java/com/example/demo/config/error/GlobalErrorAttributes.java`
- `toy_pro_like/src/main/java/com/example/demo/config/error/GlobalErrorExceptionHandler.java`
- `toy_pro_like/src/main/java/com/example/demo/like/enums/TargetType.java`
- `toy_pro_like/src/main/java/com/example/demo/like/service/LikeService.java`

### 수정 내용 요약

- 네 프로젝트의 `ErrorResponseDTO`를 `status`, `code`, `message`, `time`, `path` 필드로 통일하고, 전역 예외 핸들러가 Map에서 DTO를 생성해 JSON body로 반환하도록 변경했다.
- User 프로젝트의 공통 에러 응답 factory와 Security 응답 writer도 DTO를 직접 사용하도록 전환했다. `GlobalErrorAttributes`는 WebFlux `ErrorAttributes` 계약을 유지하기 위해 DTO를 Map으로 변환해 전달한다.
- Comment, Like, Post 서비스의 기존 커스텀 예외와 일반 상태 예외를 `BusinessException(BusinessErrorCode)`로 교체했다.
- 네 프로젝트의 요청 파라미터·날짜 형식·Like 대상 타입 검증 오류도 `INVALID_REQUEST_PARAMETER` ErrorCode로 처리했다.
- `OBJECT_NOT_FOUND`, `LIKE_ALREADY_EXISTS`, `INVALID_STATE`, `INTERNAL_SERVER_ERROR`의 명시적인 에러 코드 값을 적용하고, `HttpException`도 전역 에러 코드 해석 대상에 포함했다.

### 테스트 또는 빌드 수행 결과

- `toy_pro_user`: `./gradlew.bat --offline --no-daemon compileJava` 성공. 기존 MapStruct 경고 4건만 출력되었다.
- `toy_pro_post`: `./gradlew.bat --offline --no-daemon compileJava` 성공.
- `toy_pro_like`: 의존성이 로컬 캐시에 없어 오프라인 빌드는 실패했으며, 일반 빌드 `./gradlew.bat --no-daemon compileJava`에서 의존성 다운로드 후 성공했다.
- `toy_pro_comment`: 기존 `CommentSearchRequestDTO` 타입 누락으로 컴파일이 실패했다. 이번 변경과 무관한 `CommentService`의 기존 import 및 메서드 파라미터 오류이며, 변경된 ErrorCode/DTO 관련 오류는 보고되지 않았다.

## 2026-08-20 18:24 KST — 필수 요청 파라미터 오류 메시지 구체화

### 사용자 요청 요약

`BusinessException`에 메시지 오버로드 생성자를 추가하고, 필수 요청 파라미터가 누락되었을 때 기존 형식인 `name + " is required"` 메시지를 반환한다.

### 수정 파일 목록

- `toy_pro_user/src/main/java/com/example/demo/common/error/exception/BusinessException.java`
- `toy_pro_user/src/main/java/com/example/demo/common/util/RequestParameterUtil.java`
- `toy_pro_post/src/main/java/com/example/demo/common/error/exception/BusinessException.java`
- `toy_pro_post/src/main/java/com/example/demo/common/util/RequestParameterUtil.java`
- `toy_pro_comment/src/main/java/com/example/demo/common/error/exception/BusinessException.java`
- `toy_pro_comment/src/main/java/com/example/demo/common/util/RequestParameterUtil.java`
- `toy_pro_like/src/main/java/com/example/demo/common/error/exception/BusinessException.java`
- `toy_pro_like/src/main/java/com/example/demo/common/util/RequestParameterUtil.java`

### 수정 내용 요약

- `BusinessException(ErrorCode errorCode, String message)` 생성자를 추가해 ErrorCode는 유지하면서 상황별 메시지를 지정할 수 있도록 했다.
- 필수 날짜 및 페이지 파라미터 누락 시 `INVALID_REQUEST_PARAMETER` 코드와 함께 `<파라미터명> is required` 메시지를 반환하도록 변경했다.

### 테스트 또는 빌드 수행 결과

- `git diff --check` 성공.
- User 프로젝트의 `./gradlew.bat --offline --no-daemon compileJava` 재검증은 실행 환경의 시간 제한으로 완료 결과를 수집하지 못했다. 앞선 동일 프로젝트의 컴파일은 성공했으며, 이번 변경은 생성자 오버로드와 해당 호출부 변경으로 한정된다.

## 2026-09-01 19:54 KST — Post 프로젝트 WebFlux Swagger/OpenAPI 적용 예시

### 사용자 요청 요약

Spring Boot/WebFlux 버전과 호환되는 springdoc-openapi를 적용하고, Controller로 전환하지 않은 RouterFunction/HandlerFunction 구조의 `toy_pro_post`에만 Swagger 문서화 예시를 구성한다.

### 수정 파일 목록

- `toy_pro_post/build.gradle`
- `toy_pro_post/src/main/java/com/example/demo/config/openapi/OpenApiConfig.java`
- `toy_pro_post/src/main/java/com/example/demo/post/api/router/PostRouter.java`

### 수정 내용 요약

- Spring Boot 3.5.x와 호환되는 `org.springdoc:springdoc-openapi-starter-webflux-ui:2.8.17` 의존성을 추가했다.
- `OpenApiConfig`에서 API 제목, 버전, 설명을 제공하는 기본 `OpenAPI` Bean을 구성했다.
- 기존 Post RouterFunction의 URI와 HandlerFunction 연결을 유지한 채 `@RouterOperations`와 `@RouterOperation`으로 GET, POST, PUT, DELETE 라우트를 문서화했다.
- 실제 Handler 코드에 근거해 쿼리·경로 파라미터, request body, 응답 상태 및 기존 Post DTO schema를 연결했다. Swagger 전용 DTO나 비즈니스 로직 변경은 추가하지 않았다.

### 테스트 또는 빌드 수행 결과

- `./gradlew.bat --offline --no-daemon compileJava`에서 `compileJava UP-TO-DATE`를 확인했다.
- 로컬 포트 8081에서 애플리케이션을 기동해 `GET /v3/api-docs`와 `GET /swagger-ui/index.html`이 모두 HTTP 200임을 확인했다.
- `/v3/api-docs`에서 Post 라우트의 `findPosts` operationId, `게시글 목록 조회` summary, `PostCreateRequestDTO` 및 `PostResponseDTO` schema 참조를 확인했다.

## 2026-09-03 21:06 KST — User, Comment, Like 프로젝트 Swagger/OpenAPI 적용

### 사용자 요청 요약

게시글 프로젝트의 WebFlux RouterFunction 기반 Swagger/OpenAPI 적용 방식을 기준으로 User, Comment, Like 프로젝트에 동일한 문서화 구조를 적용한다.

### 수정 파일 목록

- `toy_pro_user/build.gradle`
- `toy_pro_user/src/main/java/com/example/demo/config/openapi/OpenApiConfig.java`
- `toy_pro_user/src/main/java/com/example/demo/config/security/SecurityConfig.java`
- `toy_pro_user/src/main/java/com/example/demo/auth/api/router/AuthRouter.java`
- `toy_pro_user/src/main/java/com/example/demo/user/api/router/UserRouter.java`
- `toy_pro_user/src/main/java/com/example/demo/role/api/router/RoleRouter.java`
- `toy_pro_comment/build.gradle`
- `toy_pro_comment/src/main/java/com/example/demo/config/openapi/OpenApiConfig.java`
- `toy_pro_comment/src/main/java/com/example/demo/comment/api/router/CommentRouter.java`
- `toy_pro_like/build.gradle`
- `toy_pro_like/src/main/java/com/example/demo/config/openapi/OpenApiConfig.java`
- `toy_pro_like/src/main/java/com/example/demo/like/api/router/LikeRouter.java`

### 수정 내용 요약

- Spring Boot 3.x User, Comment 프로젝트에는 `springdoc-openapi-starter-webflux-ui:2.8.17`을, Spring Boot 4.x Like 프로젝트에는 `springdoc-openapi-starter-webflux-ui:3.1.0`을 추가했다.
- 각 프로젝트에 API 제목, 버전, 설명을 제공하는 `OpenApiConfig`를 추가했다.
- 기존 RouterFunction과 HandlerFunction 연결을 변경하지 않고 각 Router Bean에 `@RouterOperations` 및 `@RouterOperation`을 적용했다.
- Handler 코드에 근거해 URI, HTTP 메서드, 쿼리/경로 파라미터, request body, response schema, 주요 상태 코드를 문서화했다. 기존 DTO와 기본 타입만 사용했으며 Swagger 전용 DTO는 추가하지 않았다.
- User JWT Security에는 Swagger 문서 경로(`/swagger-ui/**`, `/v3/api-docs/**`)만 `permitAll`로 추가해 문서 UI와 OpenAPI JSON 접근을 허용했다.

### 테스트 또는 빌드 수행 결과

- `toy_pro_user`: `./gradlew.bat --offline --no-daemon compileJava` 성공. 기존 MapStruct 경고 4건만 출력되었다. `test` 태스크는 실행을 시작했지만 실행 환경에서 최종 완료 결과를 수집하지 못했다.
- `toy_pro_like`: `./gradlew.bat --offline --no-daemon compileJava` 성공. 로컬 포트 8085에서 `/v3/api-docs`, `/swagger-ui/index.html`이 모두 HTTP 200이며 Like RouterOperation summary와 operationId를 확인했다.
- `toy_pro_comment`: 기존 `CommentSearchRequestDTO` 타입 누락으로 `compileJava`가 실패했다. Swagger 변경과 무관한 기존 `CommentService` import 및 메서드 파라미터 오류로, Swagger UI/OpenAPI endpoint는 미검증이다.

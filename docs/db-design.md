# DB Design

## 1. 설계 배경

본 프로젝트의 DB 설계는 단순 CRUD 기능 구현을 넘어서 서비스 확장성과 구조적 유연성을 고려하는 것을 목표로 진행했습니다.

초기에는 기능 구현 중심으로 테이블을 구성했지만,  
Like 기능 및 인증/인가 구조를 설계하는 과정에서 데이터 관계와 변경 흐름을 보다 명확하게 관리할 필요성을 느꼈습니다.

특히 다음과 같은 방향을 기준으로 DB 구조를 재정리했습니다.

- 도메인 간 관계 명확화
- 변경 이력 관리 구조 도입
- 확장 가능한 데이터 모델 설계
- 애플리케이션 레벨 제어 중심 설계

---

## 2. 전체 구조 개요

DB는 다음과 같은 도메인 중심 구조로 구성되어 있습니다.

- User / Role : 사용자 및 권한 관리
- Post / Comment : 게시글 및 댓글
- Like : 좋아요 Snapshot, 이벤트 이력 및 복구용 메타데이터 관리

각 도메인은 Master / Relation / History / Snapshot / Metadata 성격에 따라 테이블을 분리했습니다.

---

## 3. 설계 핵심 원칙

### 3.1 도메인 중심 설계

각 테이블은 도메인 단위로 명확하게 분리했습니다.

예시:
- t_user_m01 : 사용자 정보
- t_post_m01 : 게시글 정보
- t_comment_m01 : 댓글 정보

목적:
- 도메인별 책임 분리
- 서비스 로직과 자연스러운 매핑

### 3.2 Snapshot 데이터와 이력 데이터 분리

본 프로젝트에서는 Snapshot 데이터와 이력(History)을 분리하는 구조를 사용했습니다.

예시:
- t_like_snap01 : 특정 Snapshot version의 Redis Like 상태
- t_like_h01 : 실제 Redis 상태 변경 Event 이력
- t_like_snap_meta01 : Snapshot version 및 실행 상태 관리

목적:
- Redis 실시간 상태와 DB 영속 데이터의 역할 분리
- 장애 시 Snapshot + History 기반 복구 가능
- 상태 변경 흐름 추적
- Snapshot 생성 상태 및 version 관리

### 3.3 계층 구조 분리 (Role Hierarchy)

권한 구조는 단순 Role 테이블이 아닌  
계층 구조를 표현할 수 있도록 별도 테이블로 분리했습니다.

- t_role_m01 : Role 정의
- t_role_hierarchy_s01 : Role 간 상속 관계

목적:
- 권한 확장성 확보
- 상위 권한 → 하위 권한 구조 표현

### 3.4 N:M 관계 분리

User와 Role 관계는 다대다(N:M) 구조이므로  
중간 테이블을 통해 관리합니다.

- t_user_role_s01

목적:
- 유연한 권한 부여 구조
- 확장 가능한 사용자-권한 매핑

### 3.5 Soft Delete 전략

일부 테이블에서는 삭제 여부를 물리 삭제 대신  
논리 삭제 방식으로 처리합니다.

예시:
- del_yn 컬럼 사용

목적:
- 데이터 복구 가능성 확보
- 이력 관리와의 정합성 유지

### 3.6 애플리케이션 중심 제어

본 프로젝트에서는 외래키(FK), Unique 제약 등을 논리적으로는 유지하지만, 물리적으로는 최소화했습니다. 
(+) 분산 처리/재처리 과정에서 데이터 정합성을 보장하기 위해 필요한 기술적 제약은 선택적으로 적용합니다.

목적:
- 개발 유연성 확보
- MSA 전환 시 서비스 간 의존성 최소화
- 애플리케이션 레벨에서 데이터 정합성 제어

---

## 4. Like 도메인 설계 특징

Like 기능은 다음과 같은 특징을 가집니다.

### 4.1 단일 테이블 다형성 구조

Like는 게시글(Post), 댓글(Comment) 모두에 적용되므로  
다형성 구조를 사용했습니다.

- target_type : 대상 타입 (P / C)
- target_sn : 대상 식별자

목적:
- 테이블 분리 없이 확장 가능
- 구조 단순화

### 4.2 Snapshot + Event History 구조

- 실시간 상태 : Redis Set
- 복구용 Snapshot : t_like_snap01
- 이벤트 이력 : t_like_h01
- Snapshot 실행 메타데이터 : t_like_snap_meta01

목적:
- 실시간 상태와 복구용 영속 데이터 분리
- Redis 장애 시 Snapshot + History 기반 재구성
- Snapshot version별 상태 관리
- Retry 및 Recovery 확장 가능성 확보

### 4.3 Like History 식별자 및 시간 정보

```
t_like_h01
- event_id
- event_dt
- reg_dt
- ...
```

역할:
```
event_id
→ 실제 Like 상태 변경 Event 식별자
→ Retry 멱등성 보장
→ UNIQUE 적용

event_dt
→ Redis에서 실제 상태 변경이 발생한 시각

reg_dt
→ History가 DB에 등록된 시각
```

### 4.4 Snapshot Version / Metadata

```
t_like_snap_meta01
- snapshot_version
- status
- ...
```

- status :**RUNNING / COMPLETE / FAILED**
- snapshot_version은 DB Sequence 기반 발급
- COMPLETE snapshot_version만 복구 데이터로 사용
- FAILED/RUNNING의 부분 Snapshot 데이터는 cleanup 대상


---

## 5. 인증/인가 관련 데이터 구조

### 5.1 Role 기반 권한 관리

- Role 단위 권한 정의
- User-Role 매핑 구조

### 5.2 계층형 권한 구조

- Role Hierarchy 테이블을 통해  
  상속 기반 권한 구조 표현

예시:  
ADMIN → USER

목적:
- 권한 관리 단순화
- 확장 가능한 권한 구조

---

## 6. 설계 의도

본 DB 구조는 다음을 목표로 설계되었습니다.

- 단순 CRUD를 넘는 구조적 설계 경험 확보
- 실시간 상태와 복구용 Snapshot/History 데이터의 역할 분리
- 장애 복구와 재처리를 고려한 데이터 모델 설계
- 확장 가능한 권한 및 관계 구조 설계
- 애플리케이션 중심 데이터 제어

---

## 7. 향후 개선 방향

- 물리적 제약 조건(FK, Index) 재정비
- 대용량 데이터 대응을 위한 파티셔닝 전략
- History 기반 Incremental Snapshot 방식 검토
- 이벤트 기반 아키텍처 / 외부 Message Broker 전환 고려
- 정합성 검증 및 부분 복구 기능 확장
- CQRS 구조 적용 검토
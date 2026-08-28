# Redis Like Design

## 1. 설계 배경

해당 기능은 일반적인 SNS의 좋아요 기능을 벤치마킹하여 구성한 기능입니다. Like 기능은 사용자 트래픽이 많아질 경우
조회 성능과 동시성 처리 비용이 크게 증가할 수 있는 영역이라 판단되었습니다.

- 빈번한 Like 상태 조회/변경을 Redis에서 처리
- DB는 History와 복구용 Snapshot을 담당
- Redis와 DB의 역할을 분리

이러한 특성을 고려하여,
Like 서비스에서는 DB 단독 처리 구조가 아닌 Redis 실시간 Like 상태 저장소 역할를 적용했습니다.  
또한 추가로 Authentication 영역에서도 Redis를 Token Blacklist 관리 용도로 사용하고 있습니다.  

해당 문서는 Like 기능 영역의 Redis 설계 설명을 중심으로 작성되었습니다.  
Authentication 영역에서도 Redis를 사용되어 간략한 설명이 추가되나, 자세한 내용은 Security 문서에서 별도로 설명합니다.

---

## 2. Redis 사용 영역 (Like 기준)

현재 시스템에서는 Redis를 두 가지 목적으로 사용합니다.

1. Like 실시간 상태 관리
2. JWT Token 상태 관리

두 기능은 서로 다른 목적을 가지며, Redis Key Prefix를 기준으로 Namespace를 분리하여 관리합니다. 

- Like 기능
  - 사용자 Like 상태 조회
  - 게시글 Like Count 조회

Authentication 영역의 구조는 __Security__ 문서에서 별도로 설명합니다.

---

## 3. 데이터 기준 (Source of Truth)

본 구조에서는 다음 기준을 사용합니다.

| 저장소 | 역할  |
|---|---|
| Redis Set |  실시간 Like 현재 상태 |
| t_like_h01 |	실제 상태 변경 History |
| t_like_snap01 |	특정 Snapshot 작업 시점의 Redis 복구용 논리 Snapshot |
| t_like_snap_meta01 |	Snapshot version 및 실행 상태 관리 |
| Redis Stream |	History 저장 실패 Event Retry Queue |

- 실시간 서비스 판단 기준: Redis
- 변경 이력의 영속 기록: t_like_h01
- Redis 전체 복구 기준점: COMPLETE Snapshot + 이후 History

---

## 4. History-Snapshot-Recovery 전략
### 현재 구조

현재는 Redis 기반 캐시 구조만 적용되어 있으며,
DB와의 동기화 Batch는 아직 구현되지 않았습니다.

### 계획된 구조

Redis에 반영된 Like 상태를 DB에 최종 반영하기 위해
Scheduler 기반 Batch 동기화 구조를 적용할 예정입니다.

- Scheduler 기반 주기적 동기화
- Batch 단위 DB 반영

### 4.1 Like 상태 변경과 History 기록

```
SISMEMBER
→ 사전 상태 확인

SADD/SREM 반환값
→ 실제 상태 변경 판단

SADD/SREM = 1
→ eventId + event_dt 생성
→ t_like_h01 INSERT

History 실패
→ Retry Stream 저장
```

### 4.2 History Retry 전략

- Redis Stream 사용
- Consumer Group / Pending / ACK
- eventId UNIQUE + 멱등성
- Retry Count Redis Hash
- 개별 실패 최대 5회
- DB 시스템 장애는 retryCount 증가하지 않음
- 5회 실패 시 DLQ
- DLQ 이후 운영자 확인

자세한 구조와 설명은 [like-history-retry-strategy](./docs/feature_like/like-history-retry-strategy.md) 파일 참조

### 4.3 Snapshot 생성 전략

목적: Redis 장애 / Flush / 데이터 유실 대응

```
t_like_snap01
→ version 기반 Snapshot

t_like_snap_meta01
→ RUNNING / COMPLETE / FAILED
```

- DB Sequence로 snapshot_version 발급
- Redis SCAN
- Chunk Bulk Insert
- 전체 성공 후 COMPLETE
- 3시간 주기
- COMPLETE가 아닌 version은 복구에 사용하지 않음

SCAN 기반 Snapshot은 엄밀한 point-in-time 물리 Snapshot이 아니라 애플리케이션 레벨의 복구용 논리 Snapshot이다.

### 4.4 Snapshot Cleanup

- Snapshot Create와 별도 Job
- 3시간 주기
- Create Job 약 2시간 후 실행
- 최근 COMPLETE 2개 유지
- FAILED 부분 데이터 삭제
- stale RUNNING → FAILED 변경 후 삭제
- Cleanup 실패는 ERROR 로그 및 운영 확인

### 4.5 Recovery 전략

Recovery Service: 별도 내부 운영 서비스로 api 호출로 Recovery 동작

동작 흐름:
1. Like Service → RECOVERY
2. 조회/Like/Unlike 차단
3. Scheduler Retry Drain API 호출
4. Retry Stream/Pending History 반영
5. 최신 COMPLETE Snapshot 조회
6. Redis Like 데이터 초기화
7. Snapshot 적재
8. snapshot.started_at 이후 History 조회
9. targetType + targetSn + userSn별 최신 Event 추출
10. ADD → SADD / DELETE → SREM
11. 성공 → NORMAL
12. 실패 → UNAVAILABLE

---

## 5. 장애 대응 전략

Redis 장애 상황에서는 다음 기준으로 동작합니다.

- Redis 장애/복구 중
    - LikeService RECOVERY 또는 UNAVAILABLE 상태 변경
    - 조회/Like/Unlike 모두 차단
    - Recovery 실패시 UNAVAILABLE 상태 유지

- Redis Like 데이터 + Retry Stream이 동시에 유실될 경우
    - DB History에 아직 반영되지 않은 Event는 완전 복구 보장 불가

Redis persistence는 현재 적용하지 않습니다.

---

## 6. Redis 데이터 구조 및 Key 설계

Like 기능에서는 Redis Set 자료구조를 사용하여
게시글 기준의 Like 상태를 관리합니다.  
Redis는 Like의 실시간 현재 상태를 관리하며, DB는 History와 복구용 Snapshot을 담당한다.
따라서 Redis에는 Like 이벤트의 최종 데이터가 아닌
**현재 상태 조회에 필요한 최소 데이터만 저장**하는 방향으로 설계했습니다.

### 6.1 Redis 자료구조 선택

Like 기능에서는 Redis **Set** 자료구조를 사용합니다.

Key 구조:
```
like:{targetType}:{postSn}
```

예시:
```
like:post:100
```

Set 내부에는 해당 게시글에 Like를 누른 **사용자 식별자(userSn)** 가 저장됩니다.

예시:
```
SADD like:post:100 23
SADD like:post:100 45
SADD like:post:100 78
```

### 6.2 사용자 Like 여부 조회

특정 사용자가 특정 게시글에 Like를 눌렀는지 확인할 때는  
Redis `SISMEMBER` 명령을 사용합니다.

예시:
```
SISMEMBER like:post:100 23
```

해당 명령을 통해 **O(1) 시간 복잡도로 Like 여부를 확인**할 수 있습니다.

### 6.3 게시글 Like Count 조회

게시글의 Like 수는 별도의 Count 값을 저장하지 않고  
Redis Set의 Size를 사용하여 계산합니다.

예시:
```
SCARD like:post:100
```

이를 통해 별도의 Count Key를 관리하지 않아도 되며,
데이터 정합성 관리 복잡도를 줄일 수 있습니다.

### 6.4 Key 설계 원칙

Redis Key 설계 시 다음 기준을 적용했습니다.

- **Domain Prefix 사용**

도메인 단위 Prefix를 사용하여 Key Namespace를 분리했습니다.

예시:
```
like:*
auth:*
```

- **의미 기반 Key 구조**

Key 이름만으로도 데이터 의미를 파악할 수 있도록 설계했습니다.

예시:
```
like:{targetType}:{postSn}
```

- **확장 가능성 고려**

`targetType` 을 Key 구조에 포함시켜  
향후 다양한 Like 대상에 대해 확장 가능하도록 설계했습니다.

예시:
```
like:post:100
like:comment:55
```

이를 통해 게시글뿐 아니라 댓글, 기타 콘텐츠 등 다양한 도메인 객체에
동일한 Like 구조를 적용할 수 있습니다.

### 6.5 설계 시 고려 사항

Redis Set 구조를 선택한 이유는 다음과 같습니다.

- 사용자 Like 중복을 자연스럽게 방지할 수 있음 (Set 특성)
- 특정 사용자의 Like 여부 조회가 빠름 (SISMEMBER)
- 게시글 Like Count 조회가 빠름 (SCARD)
- 별도의 Count Key를 관리하지 않아도 되어 데이터 정합성 관리가 단순해짐

이러한 이유로 Like 기능에서는 Redis Set 기반 구조를 채택했습니다.

---

## 7. Infra 구조 설계 방향

Redis 관련 코드는 각 도메인의 외부 의존성으로 취급하며,
도메인 로직과 기술 구현을 분리하는 방향으로 구성했습니다.

Infra 영역에서 보조적으로 사용되며,
각 도메인에서 서비스의 기본 구조는 도메인 중심으로 유지하며 필요한 기능 단위로 분리하여 관리합니다.

예시 구조:
```
domain
 ├ like
 |   ├ model
 |   ├ repository
 |   └ service
 └ auth
     ├ model
     ├ repository
     ├ service
     └ infra
         └ redis
             └ repository
infra
 └ redis
     └ like
         ├ config
         ├ executor
```
- Domain
    - Like 비즈니스 로직
    - Redis / DB 접근에 대한 직접 의존 없음

- Infra (Redis)
    - Redis 설정 및 실행 책임
    - Scheduler 기반 동기화 처리
    - Redis 접근 로직 공통화

(추가) 도메인 설명:
```
Like Service
- 실시간 Like 처리
- Redis 접근
- History 기록
- Retry Event 발행
- 상태 제어

Scheduler Service
- History Retry
- Snapshot Create
- Snapshot Cleanup

Recovery Service
- 전체 Redis Like Recovery
```

Redis 장애 대응이나 데이터 복구를 위한 Fallback 로직은
Like 서비스 내부에서 필요 시 호출하는 보조 기능으로 두며,
기존 비즈니스 흐름을 침범하지 않도록 설계했습니다.

---

## 8. 향후 확장 방향

현재:
- History Retry Scheduler
- Snapshot Create Scheduler
- Snapshot Cleanup Scheduler
- Recovery Service

향후 확장 가능:
- 주기적 Redis 정합성 체크
- target 단위 부분 복구
- 관리자 운영 UI
- DLQ 수동 Retry
- Redis AOF/RDB persistence
- Kafka / RabbitMQ
- CDC

---

## 9. 설계 시 고려했던 점

Like 기능에 Redis를 적용하면서 다음과 같은 부분을 우선적으로 고민했습니다.

- 실시간 동기화 구조까지는 도입하지 않더라도 서비스 동작에는 문제가 없도록 만들기
- Redis 데이터가 신뢰되지 않는 상황에서는 잘못된 Like 상태를 노출하기보다 Like 기능을 일시 차단
- 이후 구조 확장이 가능하도록, 현재 구조는 최대한 단순하게 유지하기
- Redis/PostgreSQL Dual Write 실패
- Retry 멱등성
- Snapshot 부분 생성 실패
- 복구 중 사용자 요청 차단
- Redis Stream 동시 유실 한계
- 개인 프로젝트 범위와 운영 확장성 사이의 균형

토이 프로젝트이지만 실제 서비스에서 발생할 수 있는 상황을
가능한 범위 내에서 고려하며 설계를 진행했습니다.

---

## 10. 적용하면서 느낀 점

Redis 기반 Like 구조를 적용하면서 다음과 같은 점을 경험했습니다.

- Domain / Infra 역할을 나누는 기준을 잡을 수 있었음
- 실시간 상태, 변경 이력, Snapshot, Retry Queue의 역할을 분리하는 것이 중요하다고 느낌
- 장애 상황을 고려한 구조가 생각보다 중요하다는 것을 느낌

현재 구조가 완벽하다고 생각하지는 않지만, 기능 구현 이후 구조를 개선하는 과정에서 구조 설계 경험을 쌓을 수 있었다고 생각합니다.

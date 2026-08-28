# Like History Retry Strategy

## 1. Overview

Like 기능은 Redis를 실시간 상태 저장소로 사용하고 PostgreSQL의 `t_like_h01`에 실제 상태 변경 History를 기록한다.

Redis와 PostgreSQL은 하나의 Transaction으로 묶을 수 없기 때문에 다음 상황을 별도로 처리해야 한다.

```text
Redis 상태 변경 성공
-> History INSERT 실패
```

이 경우 Redis Stream을 Retry Queue로 사용해 History 저장을 비동기적으로 재처리한다.

---

## 2. History 기록 기준

History는 API 요청 로그가 아니라 실제 Redis 상태 변경 이력이다.

```text
SADD = 1 -> ADD History 기록
SADD = 0 -> History 기록하지 않음

SREM = 1 -> DELETE History 기록
SREM = 0 -> History 기록하지 않음
```

`SISMEMBER`는 사전 상태 검증에 사용하며 실제 변경 여부는 `SADD` / `SREM` 반환값으로 판단한다.

---

## 3. Retry Flow

```text
Like / Unlike
    |
    v
Redis SADD / SREM
    |
    +-- 실패 -> API 실패
    |
    +-- 상태 변경 없음 -> History 기록 X
    |
    +-- 상태 변경 성공
            |
            v
         eventId 생성
            |
            v
      t_like_h01 INSERT
            |
       +----+----+
       |         |
       v         v
     성공       실패
                 |
                 v
          Redis Retry Stream
                 |
                 v
          Scheduler Consumer
                 |
                 v
          History 재처리
```

API 요청 내부에서 반복 DB Retry를 수행하지 않는다.

DB 장애 시 API latency 증가 및 Retry Storm이 발생할 수 있기 때문이다.

---

## 4. eventId

History Retry 과정에서 동일 Event가 중복 저장되는 것을 방지하기 위해 상태 변경 Event마다 `eventId`를 생성한다.

```text
like_history_sn
-> DB Row 식별자

event_id
-> 논리적인 Like 상태 변경 Event 식별자
```

`t_like_h01.event_id`에는 UNIQUE 제약을 적용한다.

Retry INSERT는 PostgreSQL의 `ON CONFLICT DO NOTHING`을 사용한다.

```sql
INSERT INTO t_like_h01 (...)
VALUES (...)
ON CONFLICT (event_id) DO NOTHING;
```

따라서 다음 두 경우 모두 처리 완료로 판단한다.

- 신규 Event INSERT 성공
- 동일 `eventId`가 이미 존재

---

## 5. Redis Stream Consumer

Retry Stream은 Consumer Group으로 처리한다.

```text
Stream Event Read
-> Pending
-> History INSERT
-> XACK
```

ACK 정책:

| History 처리 결과 | Stream 처리 |
|---|---|
| INSERT 성공 | ACK |
| 동일 eventId 존재 | ACK |
| DB 처리 실패 | ACK 하지 않음 |

Scheduler가 처리 중 종료된 경우 ACK되지 않은 Event는 Pending에 남는다.

Retry Scheduler는 오래된 Pending을 먼저 회수한 뒤 신규 Event를 처리한다.

---

## 6. Retry Scheduler

현재 프로젝트의 초기 구현값은 다음과 같다.

```text
Scheduler interval
= 10초

Normal batch
= 100건

Backlog batch
= 최대 500건 단위

Stale Pending
= 약 30초 이상 idle
```

실제 운영 트래픽을 기반으로 산정한 수치가 아니라 개인 프로젝트 초기 구현을 위한 값이다.

---

## 7. Retry Count

Redis Stream의 Delivery Count는 실제 History INSERT 실패 횟수와 다를 수 있으므로 DLQ 판단에 직접 사용하지 않는다.

Retry Count는 Redis Hash로 별도 관리한다.

```text
Key
like:history:retry:count

Field / Value
eventId-A -> 1
eventId-B -> 3
```

개별 실패 시:

```text
HINCRBY like:history:retry:count {eventId} 1
```

처리 완료 또는 DLQ 이동 후:

```text
HDEL like:history:retry:count {eventId}
```

최대 Retry 횟수:

```text
MAX_RETRY_COUNT = 5
```

---

## 8. DB 장애와 개별 Event 실패 구분

### DB 연결 / 시스템 장애

```text
DB Connection Error
-> Retry Count 증가 X
-> Pending 유지
-> 현재 Scheduler cycle 중단
```

DB 전체 장애로 인해 정상 Event가 DLQ로 이동하는 것을 방지한다.

### 개별 Event 처리 실패

```text
DB는 정상
-> 특정 Event INSERT 실패
-> Retry Count + 1

Retry Count < 5
-> Pending 유지

Retry Count >= 5
-> DLQ 이동
```

---

## 9. Dead Letter Queue

최대 Retry 횟수를 초과한 Event는 별도 Redis Stream DLQ로 이동한다.

```text
Retry Stream
-> 5회 개별 처리 실패
-> DLQ Stream
```

DLQ에는 원 Event를 재구성할 수 있는 정보와 실패 원인을 저장한다.

예:

```text
eventId
targetType
targetSn
userSn
actionType
originalStreamId
errorType
errorMessage
failedAt
```

DLQ 이동 순서:

```text
DLQ XADD
-> 저장 성공
-> 원 Retry Event XACK
-> Retry Count HDEL
-> ERROR 로그
```

원 Event를 먼저 ACK하지 않는다.  
DLQ 저장 실패 시 Event 유실 가능성이 있기 때문이다.

---

## 10. DLQ 이후 처리

DLQ Event는 현재 Scheduler에서 자동 재처리하지 않는다.

```text
Retry Stream
-> 자동 복구 영역

DLQ
-> 운영자 / 개발자 확인 영역
```

현재 구현 범위:

- DLQ 저장
- ERROR 로그
- 운영자 또는 개발자가 필요 시 확인

향후 서비스 확장 시 다음 기능을 추가할 수 있다.

- DLQ 조회 API
- 관리자 UI
- 수동 Retry
- Retry Stream 재투입
- Monitoring / Alert 연계

---

## 11. Current Scope

현재 Retry 설계에서 구현할 범위는 다음과 같다.

- Redis 상태 변경 기준 History 기록
- `eventId` 기반 멱등성
- Redis Stream Retry Queue
- Consumer Group
- Pending Recovery
- ACK 정책
- Redis Hash 기반 Retry Count
- 최대 Retry 5회
- DB 시스템 장애와 개별 Event 오류 구분
- DLQ 격리
- ERROR 로그

다음 항목은 별도 설계에서 다룬다.

- `t_like_m01` Snapshot
- Snapshot 동기화 Scheduler
- Snapshot checkpoint
- Redis 전체 장애 및 복구
- 최종 Source of Truth

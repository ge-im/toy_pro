# Codex 참고 - Scheduler Service 구현 가이드

## 목적

현재 Scheduler Service에 다음 기능을 추가/보완한다.

```text
1. Like History Retry
2. Snapshot Create Job
3. Snapshot Cleanup Job
4. Recovery Service에서 호출할 History Retry Drain 내부 API
```

기존 코드/SQL/문서의 실제 구조를 우선 확인하고, 아래 설계 의도를 유지한 상태에서 프로젝트 네이밍과 패턴에 맞게 구현한다.

---

## 1. Snapshot 구조

기존 `t_like_m01`은 Snapshot 역할로 변경 예정이다.

```text
t_like_m01 → t_like_snap01
```

`t_like_snap01`에는 `snapshot_version`을 포함한다.

별도 staging table은 사용하지 않는다.

신규 metadata 테이블 개념:

```text
t_like_snap_meta01
- snapshot_version
- status
- started_at
- completed_at
- error_type/error_code
- error_message
```

status:

```text
RUNNING
COMPLETE
FAILED
```

Version은 PostgreSQL Sequence/BIGSERIAL 등 DB에서 발급한다.

Redis에 active/max version 캐시는 두지 않는다.

---

## 2. Snapshot Create Job

주기:

```text
3시간
```

처리 순서:

```text
1. metadata RUNNING INSERT
2. DB에서 snapshot_version 반환
3. Redis Like key SCAN
4. 조회 데이터를 Chunk 단위 Bulk Insert
5. 전체 Redis 조회 및 DB Insert 완료
6. metadata RUNNING → COMPLETE
```

Transaction:

```text
metadata INSERT → commit
chunk bulk insert → chunk별 commit
metadata COMPLETE update → commit
```

전체 Snapshot을 하나의 긴 transaction으로 묶지 않는다.

---

## 3. Snapshot Create 실패

Redis 오류 분류:

```text
SNAPSHOT_REDIS_CONNECTION_ERROR
SNAPSHOT_REDIS_SCAN_ERROR
SNAPSHOT_REDIS_READ_ERROR
```

DB Chunk 오류:

```text
SNAPSHOT_BULK_INSERT_ERROR
```

Metadata Update 오류:

```text
SNAPSHOT_META_UPDATE_ERROR
```

정책:

```text
오류 발생
→ 해당 version COMPLETE 처리 금지
→ metadata FAILED update 시도
→ Snapshot 즉시 Retry하지 않음
→ 다음 Scheduler 주기에 새 version 발급 후 재실행
```

모든 Snapshot row 저장 후 `RUNNING → COMPLETE` 변경만 실패하면 짧은 제한 Retry를 적용한다.

초기 권장:

```text
최대 3회
```

끝까지 실패하면 ERROR 로그 후 해당 version은 사용하지 않는다.

---

## 4. Snapshot Cleanup Job

Snapshot Create와 별도 Job으로 구현한다.

```text
주기: 3시간
실행: Create Job 시작 약 2시간 후
```

예:

```text
00:00 Create
02:00 Cleanup
03:00 Create
05:00 Cleanup
```

### COMPLETE

최신 COMPLETE 2개 version만 유지한다.

이전 COMPLETE version의 `t_like_snap01` row 삭제.

Metadata는 유지.

### FAILED

FAILED version에 남은 `t_like_snap01` 부분 row 삭제.

Metadata는 유지.

### stale RUNNING

Cleanup 시점까지 RUNNING인 이전 version은 stale로 간주한다.

```text
RUNNING → FAILED update
↓ 성공
해당 version Snapshot row 삭제
```

FAILED update 실패:

```text
Snapshot row 삭제 X
→ SNAPSHOT_CLEANUP_ERROR
→ ERROR 로그
→ 운영 확인
```

---

## 5. History event_dt

`t_like_h01`에 실제 Redis 상태 변경시각 `event_dt` 추가가 예정되어 있다.

```text
event_dt → SADD/SREM 실제 상태 변경시각
reg_dt   → DB 등록시각
```

History Retry Stream Event도 `event_dt`를 보존해야 한다.

기존 Retry 구현과 연결할 때 값이 누락되지 않도록 한다.

---

## 6. Recovery용 Retry Drain 내부 API

Recovery Service가 Retry 로직을 중복 구현하지 않도록 Scheduler Service 내부 API를 제공한다.

목적:

```text
Recovery 시작 전
→ Retry Stream 신규 Event + Pending 처리
→ 가능한 Event를 History에 반영
→ 완료/실패 여부 응답
```

Recovery Service는 Drain 성공 후에만 Snapshot + History Recovery를 진행한다.

외부 사용자용 API가 아닌 내부 운영 API로 둔다.

---

## 7. 기존 Retry / DLQ 정책 유지

```text
History INSERT 실패
→ Retry Stream

Scheduler
→ Pending / 신규 Event 처리

History 성공 또는 eventId 중복
→ ACK

DB 시스템 장애
→ retryCount 증가 X

DB 정상 + 개별 Event 실패
→ Redis Hash retryCount + 1

retryCount >= 5
→ DLQ 이동
→ 원 Event ACK
→ retryCount Hash 삭제
→ ERROR 로그
```

Retry Count는 Redis Stream delivery count가 아니라 별도 Redis Hash를 사용한다.

---

## 8. 구현 범위에서 제외

```text
- 주기적 Redis 정합성 체크
- target 단위 Recovery
- 관리자 UI
- Redis AOF/RDB 설정
- Kafka/RabbitMQ 전환
- DLQ 자동 재처리
```

---

## 9. 구현 후 확인사항

```text
- t_like_m01 → t_like_snap01 변경
- snapshot_version 컬럼
- metadata 테이블/status
- DB sequence 기반 version 발급
- Snapshot Create 3시간 주기
- Cleanup 3시간 주기 + 2시간 offset
- COMPLETE 최근 2개 유지
- FAILED/stale RUNNING cleanup
- event_dt History/Retry 전달
- Recovery용 Retry Drain 내부 API
- 기존 Retry/DLQ 정책 유지
```

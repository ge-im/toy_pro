# Like Snapshot & Recovery Design

## 1. 개요

Redis를 실시간 Like 상태 저장소로 사용하면서 장애/유실에 대비해 DB에 복구용 논리 Snapshot을 생성하고, 필요 시 Snapshot + History로 Redis Like 데이터를 재구성한다.

```text
Redis           → 실시간 Like 상태
t_like_h01      → 실제 상태 변경 History
t_like_snap01   → version 기반 복구용 Snapshot
t_like_snap_meta01 → Snapshot 실행 상태/이력
```

---

## 2. Snapshot

기존 `t_like_m01`은 Master가 아니라 Snapshot으로 역할을 변경한다.

```text
t_like_m01 → t_like_snap01
```

Snapshot row에는 `snapshot_version`을 포함하며 별도 staging table은 사용하지 않는다.

Metadata status:

```text
RUNNING
COMPLETE
FAILED
```

Version은 Redis가 아니라 DB Sequence로 발급한다.

정상 복구 데이터로 사용할 수 있는 Snapshot은 `COMPLETE` 상태뿐이다.

---

## 3. Snapshot Create Job

초기 주기:

```text
3시간
```

처리:

```text
metadata RUNNING 등록
→ Redis SCAN
→ Chunk Bulk Insert
→ 전체 성공
→ metadata COMPLETE
```

전체 작업을 하나의 긴 transaction으로 묶지 않고 metadata 및 Chunk 단위로 commit한다.

주요 오류:

```text
SNAPSHOT_REDIS_CONNECTION_ERROR
SNAPSHOT_REDIS_SCAN_ERROR
SNAPSHOT_REDIS_READ_ERROR
SNAPSHOT_BULK_INSERT_ERROR
SNAPSHOT_META_UPDATE_ERROR
```

실패한 Snapshot은 즉시 재시도하지 않고 다음 주기에 새 version으로 다시 생성한다.

---

## 4. Snapshot Cleanup Job

Cleanup은 Snapshot 생성과 별도 Job으로 분리한다.

```text
Snapshot Create  → 3시간 주기
Snapshot Cleanup → 3시간 주기, Create 시작 약 2시간 후
```

Cleanup 대상:

- 최근 COMPLETE 2개를 제외한 오래된 COMPLETE Snapshot 데이터
- FAILED version의 부분 Snapshot 데이터
- stale RUNNING version

stale RUNNING은 먼저 FAILED로 변경한 뒤 실제 Snapshot row를 삭제한다.

상태 변경 실패 시 데이터는 삭제하지 않고 ERROR 로그 후 운영 확인 대상으로 둔다.

```text
SNAPSHOT_CLEANUP_ERROR
```

Metadata 이력은 유지한다.

---

## 5. History event_dt

History Retry로 DB 등록시각과 실제 Like 변경시각이 달라질 수 있으므로 `t_like_h01`에 `event_dt`를 추가한다.

```text
event_dt → 실제 Redis SADD/SREM 상태 변경 시각
reg_dt   → DB History 등록 시각
```

Retry Stream에도 동일 `event_dt`를 전달한다.

---

## 6. Recovery Service

전체 Redis Like 복구는 Scheduler와 분리한 별도 Recovery Service 내부 API로 제공한다.

```text
Like Service      → 실시간 API
Scheduler Service → Retry / Snapshot / Cleanup
Recovery Service  → 전체 Redis Like 복구
```

Like Service는 application 내부 상태를 사용한다.

```text
NORMAL
RECOVERY
UNAVAILABLE
```

`RECOVERY`, `UNAVAILABLE`에서는 조회/Like/Unlike 모두 차단한다.

---

## 7. Recovery Flow

```text
Recovery 요청
→ Like Service RECOVERY
→ 모든 Like API 차단
→ Scheduler Retry Drain 호출
→ Retry Stream/Pending History 반영
→ 최신 COMPLETE Snapshot 조회
→ Redis Like 데이터 초기화
→ Snapshot 적재
→ Snapshot started_at 이후 History 조회
→ target_type + target_sn + user_sn별 최신 Event 적용
→ 성공: NORMAL
→ 실패: UNAVAILABLE + ERROR 로그
```

History 최종상태 기준:

```text
ORDER BY event_dt DESC, like_history_sn DESC
```

최신 Event가 ADD면 `SADD`, DELETE면 `SREM`한다.

---

## 8. Recovery 보장 범위

Redis Stream도 Redis 데이터이므로 persistence 설정 없이 Redis 전체가 유실되면 Retry Stream도 함께 유실될 수 있다.

현재 프로젝트에서는 AOF/RDB 설정을 추가하지 않는다.

따라서:

> Redis Like 데이터와 Retry Stream이 동시에 유실되면 아직 DB History에 반영되지 않은 Event까지 완전 복구하는 것은 보장하지 않는다.

---

## 9. Future

현재 구현 범위에서 제외한다.

```text
- 주기적 Redis 정합성 체크
- target 단위 부분 복구
- 관리자 UI
- DLQ 수동 재처리
- Redis AOF/RDB
- 운영 상태 외부 영속화
```

# Redis Like Design

## 1. 설계 배경

해당 기능은 일반적인 SNS의 좋아요 기능을 벤치마킹하여 구성한 기능입니다. Like 기능은 사용자 트래픽이 많아질 경우
조회 성능과 동시성 처리 비용이 크게 증가할 수 있는 영역이라 판단되었습니다.

특히 다음과 같은 특징이 존재합니다.

- 특정 게시글에 Like 요청이 집중될 수 있음
- 현재 Like 상태 조회 요청이 매우 빈번함
- Master Table의 DB Write 부하가 누적될 가능성이 높음

이러한 특성을 고려하여,
Like 서비스에서는 DB 단독 처리 구조가 아닌 Redis 기반 캐싱 구조를 적용했습니다.  
또한 추가로 Authentication 영역에서도 Redis를 Token Blacklist 관리 용도로 사용하고 있습니다.  

해당 문서는 Like 기능 영역의 Redis 설계 설명을 중심으로 작성되었습니다.  
Authentication 영역에서도 Redis를 사용되어 간략한 설명이 추가되나, 자세한 내용은 Security 문서에서 별도로 설명합니다.

---

## 2. Redis 사용 영역 (Like 기준)

현재 시스템에서는 Redis를 두 가지 목적으로 사용합니다.

1. Like 기능 캐싱
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
| DB |  최종 데이터 기준 (Master Data) |
| Redis |	조회 성능 개선용 캐시 + 임시 상태 저장 + 일부 상태 관리 |

Like 이벤트 히스토리를 포함한 최종 데이터는 **DB에 저장**하며,   
Redis는 **현재 상태 조회 성능 개선 + 일부 상태 관리 용도**로 사용합니다.

---

## 4. 데이터 동기화 전략 
### 현재 구조

현재는 Redis 기반 캐시 구조만 적용되어 있으며,
DB와의 동기화 Batch는 아직 구현되지 않았습니다.

### 계획된 구조

Redis에 반영된 Like 상태를 DB에 최종 반영하기 위해
Scheduler 기반 Batch 동기화 구조를 적용할 예정입니다.

- Scheduler 기반 주기적 동기화
- Batch 단위 DB 반영

<br/>

### 4.1 Redis → DB 동기화

목적:

- Redis에 반영된 Like 상태를 DB에 최종 반영

방식:

- Scheduler 기반 주기적 동기화 (예: 2시간 주기)
- Batch 단위 반영

선택 이유:

- MQ 기반 이벤트 구조 대비 구현 복잡도 감소
- 토이 프로젝트 범위에서 운영 가능 수준 유지
- 향후 이벤트 기반 구조로 확장 가능

<br/>

### 4.2 DB → Redis 복구 전략

목적:
- Redis 장애 / Flush / 데이터 유실 대응
- 캐시 Warm-up 및 데이터 정합성 확보

구성:

① Redis Alive 체크
- 주기적 상태 확인
- 장애 감지 용도

② Redis Cache Data 정합성 체크
- 주기적 데이터 정합성 체크

③ Fallback 복구 요청 API
- Like 서비스 내부에서 호출 가능
- 특정 도메인 단위 복구 가능

---

## 5. 장애 대응 전략

Redis 장애 상황에서는 다음 기준으로 동작합니다.


- Redis 장애 시
    - Redis 조회 실패 → DB 조회 Fallback
    - Like 기능은 Fail 대신 Degraded Mode로 동작

- Redis 데이터 유실 시
    - Scheduler 기반 재동기화
    - 필요 시 Fallback 복구 API 호출

---

## 6. Redis 데이터 구조 및 Key 설계

Like 기능에서는 Redis Set 자료구조를 사용하여
게시글 기준의 Like 상태를 관리합니다.  
Redis는 DB의 Master Data를 대체하는 저장소가 아니라
조회 성능 개선과 상태 관리를 위한 캐시 계층으로 사용됩니다.  
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
<br/>

### 6.2 사용자 Like 여부 조회

특정 사용자가 특정 게시글에 Like를 눌렀는지 확인할 때는  
Redis `SISMEMBER` 명령을 사용합니다.

예시:
```
SISMEMBER like:post:100 23
```

해당 명령을 통해 **O(1) 시간 복잡도로 Like 여부를 확인**할 수 있습니다.

<br/>

### 6.3 게시글 Like Count 조회

게시글의 Like 수는 별도의 Count 값을 저장하지 않고  
Redis Set의 Size를 사용하여 계산합니다.

예시:
```
SCARD like:post:100
```

이를 통해 별도의 Count Key를 관리하지 않아도 되며,
데이터 정합성 관리 복잡도를 줄일 수 있습니다.

<br/>

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

<br/>

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
         ├ scheduling
```
- Domain
    - Like 비즈니스 로직
    - Redis / DB 접근에 대한 직접 의존 없음

- Infra (Redis)
    - Redis 설정 및 실행 책임
    - Scheduler 기반 동기화 처리
    - Redis 접근 로직 공통화

<br/>

Redis 장애 대응이나 데이터 복구를 위한 Fallback 로직은
Like 서비스 내부에서 필요 시 호출하는 보조 기능으로 두며,
기존 비즈니스 흐름을 침범하지 않도록 설계했습니다.

---

## 8. 향후 확장 방향

현재:
- Redis 기반 캐시 구조

향후 확장 가능:
- Scheduler 기반 동기화 구조
- Redis Pub/Sub 기반 이벤트 처리
- Kafka / RabbitMQ 기반 이벤트 구조
- CDC 기반 동기화

---

## 9. 설계 시 고려했던 점

Like 기능에 Redis를 적용하면서 다음과 같은 부분을 우선적으로 고민했습니다.

- 실시간 동기화 구조까지는 도입하지 않더라도 서비스 동작에는 문제가 없도록 만들기
- Redis 장애 상황에서도 기능이 완전히 멈추지 않도록 하기
- 이후 구조 확장이 가능하도록, 현재 구조는 최대한 단순하게 유지하기

토이 프로젝트이지만 실제 서비스에서 발생할 수 있는 상황을
가능한 범위 내에서 고려하며 설계를 진행했습니다.

---

## 10. 적용하면서 느낀 점

Redis 기반 Like 구조를 적용하면서 다음과 같은 점을 경험했습니다.

- Domain / Infra 역할을 나누는 기준을 잡을 수 있었음
- 캐시와 DB 역할을 명확히 나누는 것이 중요하다고 느낌
- 장애 상황을 고려한 구조가 생각보다 중요하다는 것을 느낌

현재 구조가 완벽하다고 생각하지는 않지만, 기능 구현 이후 구조를 개선하는 과정에서 구조 설계 경험을 쌓을 수 있었다고 생각합니다.

## Branch / Merge 전략

- master / dev / feature 브랜치 전략 사용
  - Pull Request 기반으로 feature 브랜치를 dev 브랜치에 병합
    - 기능 단위 개발 완료 및 정상 동작 확인 후 merge 진행
  - dev의 기능이 오류가 없고 프로그램 배포 수준이 된다면 master 브랜치에 병합
- 기능 단위 commit 이력을 유지하여 변경 흐름을 추적 가능하도록 관리
- 병합 시 merge commit 방식을 사용

<br/>

## branch 목록 및 설명

#### Master 용도 Branch

| branch | 역할 |
| ------ | ------ |
| master | 최종 배포 버전 관리 | 
| dev | 개발 통합 브랜치 (모든 기능 병합) |

#### Supporting Branch

| branch | 역할 |
| ------ | ------ |
| db_set | DB 초기 설정 및 데이터 관련 작업 브랜치 |
| docs | 문서 작업 전용 브랜치 (README, 설계 문서 등) |

#### Feaure (기능) Branch

| branch | 역할 |
| ------ | ------ |
| back_common | 공통 기능(페이징, common 패키지 구조 설계, ...) 개발 |  
| back_user | 사용자 기능 개발 |
| back_post | 게시글 기능 개발 | 
| back_comment | 댓글 기능 개발 | 
| back_like | 좋아요 기능 개발 |
| back_auth | 인증/인가 및 로그인 기능 개발 |

---

## Commit message 규칙

- prefix : feat / fix / docs
- 초기 commit 메시지는 자유롭게 작성되었으며, 이후 규칙을 정리하여 일관되게 적용

예시: 
```
feat: 누락 기능 추가 개발

- search기능 CustomRepository 추가
- 조회수 컬럼 및 증가 기능 추가
```

# Oracle, Tibero 전환 메모

WorkBridge는 MySQL 8.4에서만 실행하고 테스트했습니다. 이 문서는 Oracle이나 Tibero 지원을 의미하지 않으며, DB를 바꿀 때 먼저 확인할 부분만 적어 둔 메모입니다.

## 확인할 차이

| 항목 | MySQL | Oracle, Tibero 확인 사항 |
|---|---|---|
| PK 생성 | `AUTO_INCREMENT` | Sequence와 Trigger 또는 Identity |
| 페이징 | `LIMIT`, `OFFSET` | `OFFSET ... FETCH` 또는 버전별 대안 |
| Boolean | `BOOLEAN`, `TINYINT` 계열 | `NUMBER(1)` 또는 문자 코드 |
| 빈 문자열 | 빈 문자열 유지 | Oracle은 빈 문자열을 NULL로 처리 |
| UPSERT | `INSERT ... ON DUPLICATE KEY` | `MERGE` |
| JSON | MySQL JSON 함수 | CLOB 또는 DB별 JSON 기능 |
| 날짜 함수 | MySQL 함수 | `SYSDATE`, `SYSTIMESTAMP` 등 |
| CLOB | TEXT 계열 | CLOB 바인딩 확인 |

## MyBatis 분리

DB별 차이가 생기는 SQL은 Mapper를 분리하는 편이 안전합니다.

- PK 생성
- 페이징
- UPSERT
- 날짜 함수
- JSON과 CLOB
- Boolean 표현

공통 조회와 상태 조건 UPDATE는 가능한 범위에서 유지하고, DB 전용 문법만 별도 Mapper나 DatabaseId로 나눕니다.

## Flyway

기존 MySQL 마이그레이션을 그대로 실행하면 안 됩니다. Oracle과 Tibero용 스크립트를 별도 경로로 두고, DDL, 인덱스, CHECK, 초기 데이터, Sequence를 다시 작성해야 합니다.

## 전환 순서

1. 대상 DB 버전과 JDBC 드라이버를 정합니다.
2. Flyway 스크립트를 변환합니다.
3. MyBatis SQL을 분리합니다.
4. 빈 문자열, 날짜, 숫자 변환을 확인합니다.
5. 결재 중복 처리와 이관 중복 방지 쿼리를 먼저 테스트합니다.
6. 인덱스와 실행 계획을 대상 DB에서 다시 확인합니다.

실제 DB에서 설치와 통합 테스트를 마치기 전에는 지원 완료로 표기하지 않습니다.

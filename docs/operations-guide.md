# 운영 메모

## 실행 구성

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\initialize-env.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\start.ps1 -Rebuild
powershell -ExecutionPolicy Bypass -File .\scripts\verify-running.ps1
```

서비스:

```text
workbridge-mysql     MySQL 8.4
workbridge-mock-erp  Mock ERP 8081
workbridge-server    JSP/API 8080
```

## 상태 확인

```powershell
docker compose --env-file infra/.env -f infra/docker-compose.yml ps
docker compose --env-file infra/.env -f infra/docker-compose.yml logs -f server
docker compose --env-file infra/.env -f infra/docker-compose.yml logs -f mock-erp
```

```text
GET /api/v1/system/ping
GET /actuator/health
GET http://localhost:8081/actuator/health
```

Actuator는 `health`, `info`만 공개하고 상세 상태는 노출하지 않습니다.

## 운영 화면

```text
/web/admin/operations
/web/admin/interfaces
/web/admin/migrations
```

운영 요약은 서버, DB 상태, 최근 로그인 실패, 최근 결재, 인터페이스 성공, 실패, 재처리 대상, 최근 이관, API 오류를 표시합니다.

## Trace ID 추적

클라이언트가 안전한 `X-Trace-Id`를 보내면 같은 값을 응답 헤더와 JSON에 반환합니다. 없거나 허용 형식이 아니면 서버가 생성합니다.

```text
브라우저/API 오류의 traceId 확인
→ 운영 오류 검색
→ audit_log / system_error_log / external_interface_log 검색
→ 서버 로그의 MDC traceId 검색
```

요청 종료 후 MDC를 정리해 다음 요청으로 Trace ID가 누출되지 않도록 합니다.

## 오류 분류

- 400: 입력, 검증, 영구 ERP 오류
- 401: 세션, 토큰 인증 실패
- 403: 역할, 메뉴, 업무 소유권 부족
- 409: 상태 전이, 중복, 낙관적 잠금, 재처리 충돌
- 503: DB 연결 또는 의존 서비스 사용 불가

전역 예외 처리기는 사용자 응답에 Stack Trace를 포함하지 않고 안전한 오류만 반환합니다. 서버 오류는 별도 새 트랜잭션으로 `system_error_log`에 기록해 원래 트랜잭션 롤백과 분리합니다.

## 계정 운영

- 계정 잠금: 기본 5회 실패, 15분
- 관리자 수동 잠금 해제 가능
- 비활성화 후 기존 세션 보호 요청 차단
- 마지막 활성 ADMIN 보호
- 모바일 Refresh Token은 회전, 폐기 이력 보존

## 인터페이스 장애

1. 상태와 최근 attempt 확인
2. HTTP 상태, 오류 코드, Trace ID, 전문 마스킹 확인
3. 영구 400은 데이터 수정 후 업무 버전을 변경해 새 전송
4. 500, Timeout은 같은 키로 재처리
5. 업무 버전이 달라졌다면 기존 인터페이스 재처리를 차단하고 새 스냅샷 전송

## 이관 장애

- `COMPLETED_WITH_ERRORS`: 오류 CSV로 입력 데이터 수정
- `DUPLICATE_MIGRATION_FILE`: 동일 완료 파일 정책 확인
- `FAILED`: 서버 로그와 Trace ID 확인, DB 기술 오류 복구 후 수정 파일로 재실행
- 개발 초기화가 필요하면 `stop.ps1 -RemoveData` 사용

## 백업과 복구 범위

Docker 개발 DB는 named volume을 사용합니다. `stop.ps1`은 볼륨을 유지하고 `-RemoveData`만 삭제합니다. 실제 운영에서는 MySQL 정기 백업, 복구 훈련, Flyway 백업 정책, 인터페이스, 감사 로그 보존주기를 별도로 정의해야 합니다.

## 외부 Tomcat

- Java 17, Tomcat 10.1
- DB/JWT/ERP 환경변수 설정
- `server/target/workbridge.war`를 `webapps`에 복사
- `/workbridge/login` 확인
- 컨텍스트 경로는 JSP `contextPath`를 사용해 API 경로에 반영
- Tomcat 포트는 `server.xml`에서 관리

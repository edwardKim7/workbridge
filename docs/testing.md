# 테스트

## 테스트 범위

### WorkBridge Server

- 애플리케이션 Context와 Flyway 스키마
- 공통 API 응답과 Trace ID
- 세션 로그인, 계정 잠금, 비활성 계정 처리
- 역할, 메뉴 권한과 업무 소유권
- 고객, 영업기회, 영업활동 등록, 검색, 수정
- 낙관적 잠금 충돌
- 전자결재 상태 전이와 중복 처리 차단
- JWT 서명, 만료, 변조와 Refresh Token 회전
- Mock ERP 전송, 실패 분류, 수동 재처리
- RFC 4180 CSV 파싱과 행 단위 검증
- 고객 이관 배치와 오류 응답
- 감사 마스킹과 전역 예외 계약

### Mock ERP

- 정상 요청 저장
- 같은 Idempotency Key 재호출 시 중복 생성 차단
- 오류, Timeout 시뮬레이션

### Flutter

- 로그인 응답 파싱
- Access Token 만료 후 Refresh와 원 요청 재시도
- 결재 목록 로딩 상태
- 처리 중 승인 버튼 중복 실행 차단
- API 오류 화면
- 로그인 화면 Widget

## 실행 명령

Docker의 Maven 이미지를 사용하는 Backend 테스트:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\test-server.ps1 -UseDocker
powershell -ExecutionPolicy Bypass -File .\scripts\test-mock-erp.ps1 -UseDocker
```

로컬 Java 17과 Maven을 사용하는 경우 `-UseDocker`를 생략합니다.

Flutter 분석과 테스트:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\bootstrap-mobile.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\test-mobile.ps1
```

전체 테스트:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\test-all.ps1 -UseDocker
```

## 실행 중인 시스템 확인

서버와 DB 상태, Trace ID 전달:

```powershell
powershell -ExecutionPolicy Bypass `
  -File .\scripts\verify-running.ps1 `
  -Port 8080
```

세션 로그인, CSRF, ERP 전송, 이관 조회, JWT 로그인, Refresh Token 회전을 포함한 스모크 테스트:

```powershell
$adminPassword = Read-Host 'admin 비밀번호' -AsSecureString
$approverPassword = Read-Host 'approver 비밀번호' -AsSecureString

powershell -ExecutionPolicy Bypass `
  -File .\scripts\smoke-test.ps1 `
  -Port 8080 `
  -AdminPassword $adminPassword `
  -ApproverPassword $approverPassword
```

PowerShell 7 미만에서는 `Invoke-RestMethod -Form`을 사용할 수 없어 CSV 업로드 부분만 건너뜁니다. 이 경우 웹 이관 화면에서 `sample-data/customers-valid.csv`를 업로드해 확인합니다.

## CI

- `backend-ci`: Maven 테스트, WAR, Mock ERP JAR 패키징
- `mobile-ci`: Flutter 프로젝트 생성, 패키지 설치, analyze, test
- `repository-checks`: 커밋된 파일의 고신뢰 비밀값 패턴 검사

CI 결과는 GitHub Actions 실행 기록을 기준으로 확인합니다. 테스트 수나 성공률은 소스 파일 개수로 추정하지 않고 실제 실행 결과를 사용합니다.

## MySQL에서 추가로 확인할 항목

H2의 MySQL 호환 모드는 빠른 통합 테스트에 유용하지만 다음 항목까지 대신하지는 않습니다.

- Flyway V1~V8의 MySQL 적용
- CHECK, UNIQUE, FK 동작
- 조건부 UPDATE의 영향 행 수
- 대량 목록의 실행계획과 인덱스 사용
- 문자 정렬 규칙과 대소문자 비교
- 실제 Lock 대기와 동시 요청 처리

Docker Compose 기동 후 중요 검색 SQL은 MySQL `EXPLAIN`으로 확인하고, 동시성 시나리오는 별도 요청으로 재현합니다.

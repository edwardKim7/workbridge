# WorkBridge

WorkBridge는 영업지원 업무를 결재와 ERP 전송까지 연결한 웹, 모바일 프로젝트입니다. 서버는 Spring Boot와 JSP로 구성했고, 승인자는 Flutter 앱에서도 결재를 처리할 수 있습니다.

실제 회사 데이터와 상용 ERP는 사용하지 않습니다. `mock-erp`는 정상 응답, 오류, 재시도, 타임아웃, 중복 전송을 확인하기 위한 로컬 서비스입니다.

## 구현 기능

- 직원, 부서, 역할, 메뉴 권한 관리
- 세션 로그인, CSRF, BCrypt 비밀번호, 로그인 실패 잠금
- 고객, 영업기회, 영업활동 관리
- 결재 작성, 요청, 승인, 반려, 취소, 처리 이력
- Flutter용 Access Token과 Refresh Token 갱신
- 승인 완료 영업기회의 Mock ERP 전송
- Idempotency Key와 호출 시도 이력
- CSV 고객 이관, 행별 검증, 오류 CSV
- 감사 로그, 시스템 오류, Trace ID 조회

## 사용 기술

| 영역 | 기술 |
|---|---|
| Server | Java 17, Spring Boot 3.5, Spring MVC, Spring Security |
| Web | JSP, JSTL, JavaScript, Fetch API, CSS |
| Data | MyBatis, MySQL 8.4, Flyway |
| Mobile | Flutter, Dart, `flutter_secure_storage` |
| Build | Maven multi-module, Docker Compose |
| Test | JUnit 5, MockMvc, Spring Security Test, Flutter Test |

## 저장소 구조

```text
workbridge/
├─ server/                 Spring Boot 서버, JSP, REST API, Flyway, 테스트
├─ mobile/                 승인자용 Flutter 앱
├─ mock-erp/               ERP 연동 확인용 Spring Boot 서비스
├─ infra/                  Docker Compose와 환경변수 예시
├─ sample-data/            고객 이관 예제 CSV
├─ scripts/                Windows 실행, 테스트 스크립트
├─ docs/                   설계와 운영 메모
├─ .github/workflows/      서버, 모바일, 비밀값 검사 CI
└─ pom.xml                 Maven 부모 프로젝트
```

## 구현 메모

### 웹과 모바일 인증

JSP 화면은 세션과 CSRF를 사용합니다. Flutter 앱은 Bearer Access Token과 Refresh Token을 사용합니다. Refresh Token 원문은 DB에 저장하지 않고 SHA-256 해시만 저장하며, 갱신할 때 기존 토큰을 폐기합니다.

### 권한 검사

메뉴가 보이지 않는 것만으로 접근을 막지 않습니다. URL과 API 권한을 확인한 뒤 Service에서 담당 고객, 요청자, 지정 승인자 같은 업무 범위를 다시 검사합니다.

### 동시 처리

고객, 영업기회, 결재 수정에는 `version`을 사용합니다. 승인과 반려는 상태, 승인자, 버전을 조건으로 갱신해 같은 요청이 두 번 처리되지 않도록 했습니다.

### ERP 연동

ERP 전송은 업무 버전이 포함된 Idempotency Key를 사용합니다.

```text
WB:ERP_SALES:OPPORTUNITY:{businessId}:v{businessVersion}
```

같은 업무 버전을 다시 보내면 같은 키를 사용합니다. Mock ERP가 이미 처리한 키라면 기존 결과를 반환합니다.

### CSV 이관

CSV는 고객 테이블에 바로 넣지 않고 Staging에 저장한 뒤 검증합니다. 정상 행만 반영하고 오류 행은 별도 CSV로 내려받을 수 있습니다. 같은 파일의 반복 반영은 SHA-256으로 막습니다.

## 로컬 실행

### 준비 사항

- Git
- Docker Desktop과 Docker Compose
- Windows PowerShell 5.1 이상

Flutter 앱을 실행하려면 Flutter SDK와 Android SDK 또는 에뮬레이터가 추가로 필요합니다.

### 환경 확인

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\check-environment.ps1
```

### 로컬 비밀값 생성

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\initialize-env.ps1
```

`infra/.env`에 DB 비밀번호와 JWT 서명 키를 생성합니다. 이 파일은 Git에 포함되지 않습니다.

### 실행

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start.ps1 -Rebuild
```

소스를 바꾸지 않고 다시 실행할 때는 `-Rebuild`를 빼도 됩니다.

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start.ps1
```

### 상태 확인

```powershell
powershell -ExecutionPolicy Bypass `
  -File .\scripts\verify-running.ps1 `
  -Port 8080
```

### 종료

```powershell
# DB 데이터 유지
powershell -ExecutionPolicy Bypass -File .\scripts\stop.ps1

# DB 볼륨까지 삭제
powershell -ExecutionPolicy Bypass -File .\scripts\stop.ps1 -RemoveData
```

## 접속 주소

| 화면 | 주소 |
|---|---|
| 로그인 | `http://localhost:8080/login` |
| 대시보드 | `http://localhost:8080/web/dashboard` |
| 전자결재 | `http://localhost:8080/web/approvals` |
| 인터페이스 운영 | `http://localhost:8080/web/admin/interfaces` |
| 데이터 이관 | `http://localhost:8080/web/admin/migrations` |
| 시스템 운영 | `http://localhost:8080/web/admin/operations` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| Health | `http://localhost:8080/actuator/health` |

Swagger UI는 로그인 후 직접 주소로 접근할 수 있습니다.

## 개발용 계정

| 로그인 ID | 역할 | 비밀번호 |
|---|---|---|
| `admin` | ADMIN | `Admin1234!` |
| `sales` | SALES | `Sales1234!` |
| `approver` | APPROVER | `Approve1234!` |
| `viewer` | VIEWER | `Viewer1234!` |

위 계정은 로컬 확인용입니다. 외부 환경에서는 샘플 계정을 제거하거나 비활성화해야 합니다.

## 테스트

서버와 Mock ERP 테스트:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\test-server.ps1 -UseDocker
powershell -ExecutionPolicy Bypass -File .\scripts\test-mock-erp.ps1 -UseDocker
```

Flutter 분석과 테스트:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\bootstrap-mobile.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\test-mobile.ps1
```

실행 중인 구성의 기본 흐름 확인:

```powershell
$adminPassword = Read-Host 'admin 비밀번호' -AsSecureString
$approverPassword = Read-Host 'approver 비밀번호' -AsSecureString

powershell -ExecutionPolicy Bypass `
  -File .\scripts\smoke-test.ps1 `
  -Port 8080 `
  -AdminPassword $adminPassword `
  -ApproverPassword $approverPassword
```

테스트 범위는 [`docs/testing.md`](docs/testing.md)에 정리되어 있습니다.

## Flutter 실행

Android 호스트 프로젝트가 없다면 먼저 생성합니다.

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\bootstrap-mobile.ps1
```

Android 에뮬레이터에서는 호스트 PC 주소로 `10.0.2.2`를 사용합니다.

```powershell
powershell -ExecutionPolicy Bypass `
  -File .\scripts\run-mobile.ps1 `
  -ApiBaseUrl http://10.0.2.2:8080
```

실기기에서 연결하려면 같은 네트워크의 PC 주소를 사용해야 합니다. 이때 `BIND_ADDRESS=0.0.0.0`은 신뢰할 수 있는 로컬 네트워크에서만 잠시 사용하고, 확인 후 `127.0.0.1`로 되돌리는 편이 안전합니다.

## 보안 메모

기본 Docker 설정은 서버를 `127.0.0.1`에만 열고 MySQL과 Mock ERP 포트는 호스트에 공개하지 않습니다. 애플리케이션 컨테이너는 비루트 사용자와 읽기 전용 파일시스템을 사용합니다.

이 설정은 로컬 실행 기준입니다. 인터넷에 배포하려면 HTTPS, 샘플 계정 제거, Secret Manager, 방화벽, 요청 제한, 로그 보관, 백업 정책을 별도로 적용해야 합니다. 자세한 내용은 [`SECURITY.md`](SECURITY.md)를 참고하세요.

공개 저장소에 올리기 전에는 아래 스크립트와 `git status`를 함께 확인합니다.

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\check-secrets.ps1
```

## 문서

| 문서 | 내용 |
|---|---|
| [`docs/architecture.md`](docs/architecture.md) | 서버와 Docker 구성 |
| [`docs/data-model.md`](docs/data-model.md) | ERD, 테이블, 인덱스 |
| [`docs/api-reference.md`](docs/api-reference.md) | REST API |
| [`docs/business-rules.md`](docs/business-rules.md) | 상태와 검증 규칙 |
| [`docs/authentication-and-authorization.md`](docs/authentication-and-authorization.md) | 세션, 토큰, 권한 |
| [`docs/external-interface-spec.md`](docs/external-interface-spec.md) | Mock ERP와 재처리 |
| [`docs/data-migration-design.md`](docs/data-migration-design.md) | CSV 이관 |
| [`docs/operations-guide.md`](docs/operations-guide.md) | 실행 상태와 장애 확인 |
| [`docs/technology-decisions.md`](docs/technology-decisions.md) | 기술 선택 메모 |

## 제한 사항

- ERP 전송은 현재 동기 HTTP 호출입니다. 처리량이 커지면 Outbox나 메시지 큐가 필요합니다.
- Mock ERP는 인메모리 저장소라 재시작하면 데이터가 사라집니다.
- Access Token 즉시 폐기 목록은 두지 않습니다.
- CSV 파일은 5MB로 제한합니다.
- Oracle과 Tibero에서는 실제 실행하지 않았습니다.

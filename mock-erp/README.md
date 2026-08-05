# WorkBridge Mock ERP

WorkBridge의 ERP 연동을 로컬에서 확인하기 위한 Spring Boot 서비스입니다. 실제 SAP나 상용 ERP가 아닙니다.

## 전송 API

```http
POST /api/v1/erp/sales
```

요청 헤더:

```text
Idempotency-Key
X-Trace-Id
X-Mock-Mode
```

같은 Idempotency Key가 다시 들어오면 새 데이터를 만들지 않고 기존 결과를 반환합니다.

## 모드

| 값 | 동작 |
|---|---|
| `NORMAL` | 정상 저장 후 200 |
| `VALIDATION_400` | 저장하지 않고 400 |
| `HTTP_500` | 저장하지 않고 500 |
| `TIMEOUT_BEFORE_COMMIT` | 저장 전에 응답 지연 |
| `TIMEOUT_AFTER_COMMIT` | 저장 후 응답 지연 |

## 관리 API

```http
GET    /api/v1/admin/records
DELETE /api/v1/admin/records
```

관리 API는 테스트 확인용이며 인증이 없습니다. Docker Compose 내부 네트워크에서만 사용해야 합니다.

## 테스트

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\test-mock-erp.ps1 -UseDocker
```

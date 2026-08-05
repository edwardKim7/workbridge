# REST API

기본 경로는 `/api/v1`입니다. JSP 웹 요청은 세션과 CSRF를 사용하고 Flutter 요청은 Bearer Access Token을 사용합니다. Entity나 DB Row를 직접 반환하지 않고 DTO로 응답합니다.

## 공통 계약

### 성공 응답

```json
{
  "success": true,
  "data": {},
  "message": "요청을 정상적으로 처리했습니다.",
  "traceId": "요청 추적 ID",
  "timestamp": "2026-08-05T21:00:00"
}
```

### 오류 응답

```json
{
  "timestamp": "2026-08-05T21:00:00",
  "status": 400,
  "code": "INVALID_REQUEST",
  "message": "요청값을 확인해 주세요.",
  "fieldErrors": [],
  "traceId": "요청 추적 ID",
  "path": "/api/v1/..."
}
```

### 페이징

- `page`: 0부터 시작
- `size`: 1~100
- 응답: `items`, `page`, `size`, `totalElements`, `totalPages`, `first`, `last`

### 상태코드

| HTTP | 기준 |
|---:|---|
| 200 | 조회, 수정, 업무 처리 성공 |
| 201 | 등록 성공 |
| 400 | 형식, Bean Validation, 입력 업무규칙 오류 |
| 401 | 세션, 토큰 인증 없음 또는 무효 |
| 403 | 역할, 메뉴, 업무 소유권 부족 |
| 404 | 리소스 없음 |
| 409 | 중복, 상태 전이, 낙관적 잠금, 재처리 충돌 |
| 413 | 업로드 제한 초과 |
| 415 | 지원하지 않는 Content-Type |
| 500 | 예상하지 못한 서버 오류 |
| 503 | DB 또는 의존 서비스 사용 불가 |

## 인증

| Method | Path | 인증 | 설명 |
|---|---|---|---|
| GET | `/auth/me` | 세션 또는 Bearer | 현재 사용자, 역할, 메뉴 |
| POST | `/auth/login` | 공개 | Flutter JSON 로그인, Access, Refresh 발급 |
| POST | `/auth/refresh` | 공개 | Refresh Token 잠금, 회전 |
| POST | `/auth/logout` | Bearer | 해당 Refresh Token 폐기 |

웹 폼 로그인과 로그아웃은 `POST /login`, `POST /logout`을 사용합니다.

## 시스템, 대시보드

| Method | Path | 권한 | 설명 |
|---|---|---|---|
| GET | `/system/ping` | 공개 | 앱 버전, 구현 단계, DB 상태, Trace ID |
| GET | `/dashboard` | `DASHBOARD` 읽기 | 조직, 계정 요약 |
| GET | `/sales/dashboard` | `DASHBOARD` 읽기 | 고객, 기회, 파이프라인, 단계 집계 |

## 조직, 권한

### 부서

```text
GET  /departments
GET  /departments/options
GET  /departments/{departmentId}
POST /departments
PUT  /departments/{departmentId}
```

### 직원

```text
GET  /employees
GET  /employees/{employeeId}
POST /employees
PUT  /employees/{employeeId}
PUT  /employees/{employeeId}/roles
POST /employees/{employeeId}/activate
POST /employees/{employeeId}/deactivate
POST /employees/{employeeId}/unlock
```

### 역할, 메뉴

```text
GET /roles
GET /menus
GET /roles/{roleId}/menus
PUT /roles/{roleId}/menus
```

조직, 권한 변경에는 `EMPLOYEE_ROLE` 쓰기 권한이 필요합니다. 마지막 활성 ADMIN과 핵심 관리권한 제거는 `409`로 차단합니다.

## 고객

```text
GET  /customers
GET  /customers/options
GET  /customers/{customerId}
POST /customers
PUT  /customers/{customerId}
```

검색조건:

```text
customerName, businessRegistrationNo, contactName,
assignedSalesId, status, active, page, size
```

등록, 수정은 `CUSTOMER` 쓰기 권한과 담당자 소유권을 함께 검증합니다. 사업자번호는 정규화 후 UNIQUE KEY를 적용합니다.

## 영업기회

```text
GET  /opportunities
GET  /opportunities/options?customerId=
GET  /opportunities/{opportunityId}
POST /opportunities
PUT  /opportunities/{opportunityId}
```

검색조건:

```text
keyword, customerId, stage, status, ownerEmployeeId,
closeDateFrom, closeDateTo, page, size
```

단계에서 상태를 파생하고, `WON=100`, `LOST=0` 규칙을 검증합니다. 수정 요청은 최신 `version`을 포함해야 합니다.

## 영업활동

```text
GET  /activities
GET  /activities/{activityId}
POST /activities
PUT  /activities/{activityId}
```

검색조건:

```text
keyword, customerId, opportunityId, employeeId,
activityType, from, to, page, size
```

기회가 연결된 활동은 활동의 고객과 기회의 고객이 같아야 합니다.

## 기준 선택 API

```text
GET /sales/employees/options
GET /customers/options
GET /opportunities/options
```

화면 선택에 필요한 최소 필드만 반환하여 상세 개인정보 우회 조회를 막습니다.

## 전자결재

| Method | Path | 주요 행위자 | 설명 |
|---|---|---|---|
| GET | `/approvals` | 로그인 사용자 | 요청/승인/전체 범위 검색, 페이징 |
| POST | `/approvals` | SALES, ADMIN | DRAFT 생성 |
| GET | `/approvals/approvers` | 요청 가능 사용자 | 활성 승인자 선택 목록 |
| GET | `/approvals/{id}` | 요청자, 승인자, 조회 역할 | 상세, 이력, 가능 행위 |
| PUT | `/approvals/{id}` | 요청자 | DRAFT 수정 |
| POST | `/approvals/{id}/request` | 요청자 | DRAFT → REQUESTED |
| POST | `/approvals/{id}/approve` | 지정 승인자 | REQUESTED → APPROVED |
| POST | `/approvals/{id}/reject` | 지정 승인자 | REQUESTED → REJECTED |
| POST | `/approvals/{id}/cancel` | 요청자 | DRAFT → CANCELED |

목록 범위:

```text
view=MY_REQUESTS | MY_APPROVALS | ALL
status=DRAFT | REQUESTED | APPROVED | REJECTED | CANCELED
type=SALES_EXPENSE | VISIT_PLAN | DISCOUNT_EXCEPTION
keyword, page, size
```

처리 요청 예시:

```json
{
  "opinion": "내용과 증빙을 확인했습니다.",
  "version": 1
}
```

승인, 반려는 DB UPDATE 조건에 `status='REQUESTED'`, `approver_id`, `version`을 포함하여 중복 처리를 차단합니다.

## 외부 인터페이스

| Method | Path | 권한 | 설명 |
|---|---|---|---|
| GET | `/interfaces` | `INTERFACE_OPERATION` 읽기 | 상태, Trace ID 검색, 페이징 |
| GET | `/interfaces/{id}` | 읽기 | 전문, 오류, 호출 시도 상세 |
| POST | `/interfaces/erp/{businessId}/send` | 쓰기 | 승인 완료 영업기회 전송 |
| POST | `/interfaces/{id}/retry` | 쓰기 | 실패 인터페이스 수동 재처리 |

전송, 재처리 요청:

```json
{
  "simulationMode": "NORMAL"
}
```

사용 가능한 모드:

```text
NORMAL, HTTP_500, TIMEOUT_BEFORE_COMMIT,
TIMEOUT_AFTER_COMMIT, VALIDATION_400
```

## 고객 CSV 이관

| Method | Path | 권한 | 설명 |
|---|---|---|---|
| GET | `/migrations` | `DATA_MIGRATION` 읽기 | 배치 검색, 페이징 |
| POST | `/migrations/customers` | 쓰기 | `multipart/form-data` CSV 업로드 |
| GET | `/migrations/{id}` | 읽기 | 배치 집계, 오류 상세 |
| GET | `/migrations/{id}/errors` | 읽기 | 오류 JSON |
| GET | `/migrations/{id}/errors.csv` | 읽기 | 오류 CSV 다운로드 |

업로드 필드명은 `file`입니다. 허용 헤더:

```text
business_registration_no,customer_name,representative_name,
contact_name,contact_email,contact_phone,established_date,
assigned_sales_employee_no
```

## 운영

| Method | Path | 권한 | 설명 |
|---|---|---|---|
| GET | `/operations/summary` | `SYSTEM_OPERATION` 읽기 | 서버, DB, 로그인, 결재, 연동, 이관 요약 |
| GET | `/operations/audit-logs` | 읽기 | 행위, 대상, Trace ID 조건 감사 로그 |
| GET | `/operations/errors` | 읽기 | HTTP 상태, 오류 코드, Trace ID 시스템 오류 |

Actuator는 별도 경로 `/actuator/health`, `/actuator/info`이며 전체 엔드포인트를 노출하지 않습니다.

## 인증 채널 구분

### JSP 세션 요청

- `JSESSIONID`
- 변경 요청은 CSRF 토큰 필요
- 권한 부족 웹 요청은 `/access-denied`
- API 권한 부족은 JSON 403

### Flutter Bearer 요청

```http
Authorization: Bearer <access-token>
```

Bearer 요청은 CSRF 대상이 아니며 Access Token 서명, issuer, type, 만료와 현재 계정, 역할 상태를 확인합니다. Access Token이 만료되면 `/auth/refresh`에서 Refresh Token을 회전하고 기존 토큰은 재사용할 수 없습니다.

# 업무 규칙

## 사용자와 권한

- 비활성 직원은 로그인할 수 없습니다.
- 비밀번호는 BCrypt로 저장합니다.
- 연속 로그인 실패가 기준을 넘으면 일정 시간 계정을 잠급니다.
- 신규 비밀번호는 UTF-8 기준 72바이트 이하로 제한합니다.
- 역할과 메뉴 권한을 변경할 때 마지막 활성 ADMIN은 제거할 수 없습니다.
- 메뉴 표시 여부와 별개로 API와 Service에서 권한을 다시 확인합니다.

## 고객

- 사업자번호는 숫자, 공백, 하이픈만 받고 숫자 10자리로 정규화합니다.
- 사업자번호는 중복 등록할 수 없습니다.
- `SALES` 사용자는 자신이 담당한 고객만 수정할 수 있습니다.
- 비활성 고객은 과거 조회는 가능하지만 신규 업무 연결과 수정은 막습니다.
- 수정 요청의 `version`이 DB와 다르면 `409 Conflict`를 반환합니다.

## 영업기회

- 영업기회 번호는 중복되지 않아야 합니다.
- 수주 단계의 확률은 100%, 실주 단계의 확률은 0%입니다.
- 종결된 영업기회는 다시 진행 단계로 돌릴 수 없습니다.
- 담당 영업사원과 관리자만 수정할 수 있습니다.
- 상태와 단계 조합은 Java 규칙과 DB CHECK로 확인합니다.

## 영업활동

- 활동은 고객에 연결되며 필요하면 영업기회에도 연결합니다.
- 활동 담당자는 활성 직원이어야 합니다.
- 방문계획과 영업비용 결재는 요청 시점의 활동 값을 결재 데이터에 복사합니다.
- 결재 진행 중 원본 활동이 바뀌더라도 승인 화면은 요청 시점 값을 사용합니다.

## 전자결재

### 상태

```text
DRAFT -> REQUESTED -> APPROVED
                   -> REJECTED
DRAFT -> CANCELED
```

- `DRAFT`만 수정, 요청, 취소할 수 있습니다.
- `REQUESTED`만 지정 승인자가 승인하거나 반려할 수 있습니다.
- 요청자와 승인자는 같을 수 없습니다.
- 반려 의견은 필수입니다.
- 승인이나 반려 시 상태, 승인자, 버전을 조건으로 UPDATE합니다.
- 처리 이력은 상태 변경과 같은 트랜잭션에서 저장합니다.

### 중복 처리

Flutter는 요청 중 버튼을 비활성화합니다. 서버는 UI 상태를 신뢰하지 않고 DB의 상태와 버전을 최종 기준으로 사용합니다. 같은 승인 요청이 동시에 들어오면 한 건만 성공하고 나머지는 `409`를 반환합니다.

## ERP 연동

### 전송 조건

- 대상 업무는 현재 `OPPORTUNITY`입니다.
- 연결된 결재가 `APPROVED` 상태여야 합니다.
- 비활성 고객이나 종결 조건에 맞지 않는 기회는 전송하지 않습니다.

### Idempotency Key

```text
WB:ERP_SALES:OPPORTUNITY:{businessId}:v{businessVersion}
```

같은 업무 버전의 자동 재시도와 수동 재처리는 같은 키를 사용합니다. 업무가 수정되어 버전이 올라갈 때만 새 키를 만듭니다.

### 호출 처리

1. 전송 조건을 확인합니다.
2. 인터페이스 원장을 `PENDING`으로 저장합니다.
3. 조건부 UPDATE로 `PROCESSING` 상태를 선점합니다.
4. DB 트랜잭션 밖에서 HTTP를 호출합니다.
5. 결과와 시도 이력을 저장합니다.

| 결과 | 상태 | 처리 |
|---|---|---|
| 2xx | `SUCCESS` | 종료 |
| 400, 404, 422 | `PERMANENT_FAILED` | 자동 재시도 안 함 |
| 408, 429, 5xx | `TEMPORARY_FAILED` | 제한 횟수 재시도 |
| 연결 또는 읽기 타임아웃 | `TEMPORARY_FAILED` | 제한 횟수 재시도 |

저장 후 응답이 늦어 타임아웃이 난 경우에도 같은 Idempotency Key로 다시 호출합니다. Mock ERP가 이미 처리했다면 새 데이터를 만들지 않고 기존 결과를 반환합니다.

### 로그

비밀번호와 토큰 원문은 저장하지 않습니다. 전화번호, 이메일, 사업자번호는 필요한 범위만 남기고 마스킹합니다. 요청과 응답 본문은 길이를 제한합니다.

## CSV 고객 이관

### 파일

- 확장자 `.csv`
- UTF-8 또는 UTF-8 BOM
- 최대 5MB
- 헤더와 데이터 행 필수
- RFC 4180 인용 규칙 사용

### 필수 헤더

```text
business_registration_no,customer_name,representative_name,
contact_name,contact_email,contact_phone,established_date,
assigned_sales_employee_no
```

### 검증

- 사업자번호 형식과 파일 내부 중복
- 고객명 필수값과 길이
- 이메일 형식
- 날짜 `yyyy-MM-dd`
- 담당 영업사원의 활성 상태와 `SALES` 역할
- 열 개수 8개

한 행에서 여러 오류가 발생하면 모두 저장합니다.

### 반영

- 모든 행을 Staging에 먼저 저장합니다.
- 오류 행은 고객 테이블에 반영하지 않습니다.
- 정상 행 전체는 하나의 트랜잭션으로 INSERT 또는 UPDATE합니다.
- 정상 행 적용 중 오류가 나면 정상 집합을 롤백하고 배치를 `FAILED`로 기록합니다.
- 오류가 일부 있더라도 정상 행 반영이 끝나면 `COMPLETED_WITH_ERRORS`로 기록합니다.
- 완료된 파일과 같은 SHA-256 값은 다시 반영하지 않습니다.

오류 CSV의 셀 값이 `=`, `+`, `-`, `@`로 시작하면 작은따옴표를 앞에 붙여 스프레드시트 수식 실행을 막습니다.

## 감사와 운영 로그

다음 변경은 감사 로그에 남깁니다.

- 사용자와 권한 변경
- 고객, 영업기회, 영업활동 변경
- 결재 요청, 승인, 반려, 취소
- ERP 재처리
- CSV 이관 실행

비밀번호, Access Token, Refresh Token 원문은 기록하지 않습니다. 운영 화면에는 Trace ID, 경로, 오류 코드, 발생 시각, 안전한 메시지만 표시합니다.

## 연결된 테스트

| 규칙 | 테스트 |
|---|---|
| 결재 상태와 중복 처리 | `ApprovalApiIntegrationTest` |
| 로그인과 잠금 | `AuthenticationIntegrationTest` |
| 조직과 메뉴 권한 | `OrganizationApiIntegrationTest`, `RoleMenuServiceTest` |
| 영업 데이터 소유권과 버전 | `SalesSupportApiIntegrationTest` |
| ERP 변환과 재시도 | `ErpSalesPayloadFactoryTest`, `ExternalInterfaceApiIntegrationTest` |
| CSV 파싱과 검증 | `Rfc4180CsvParserTest`, `CustomerMigrationValidatorTest` |
| JWT 서명과 만료 | `JwtTokenServiceTest` |

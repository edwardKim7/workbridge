# Mock ERP 연동

`mock-erp`는 실제 SAP나 상용 ERP가 아닙니다. WorkBridge의 HTTP 연동, 재시도, 타임아웃, 중복 전송 처리를 로컬에서 확인하기 위한 서비스입니다.

## 전송 대상

- 업무 유형: `OPPORTUNITY`
- 전송 조건: 연결된 결재가 `APPROVED`
- 데이터: 결재번호, 기회번호, 고객번호, 사업자번호, 영업단계, 예상매출, 담당자, 업무 버전, 승인시각
- 고객 연락처와 이메일은 보내지 않음

## 요청

```http
POST {MOCK_ERP_BASE_URL}/api/v1/erp/sales
Content-Type: application/json
Idempotency-Key: WB:ERP_SALES:OPPORTUNITY:2001:v3
X-Trace-Id: trace-id
X-Mock-Mode: NORMAL
```

응답에는 `erpDocumentNo`, `duplicate`, `processedAt`이 포함됩니다. 같은 키가 이미 처리된 경우 기존 결과를 반환하고 `duplicate=true`로 표시합니다.

## 상태

```text
PENDING -> PROCESSING -> SUCCESS
                      -> TEMPORARY_FAILED
                      -> PERMANENT_FAILED
```

`external_interface_log`는 업무별 최종 상태를 저장하고, `external_interface_attempt`는 각 HTTP 호출 결과를 저장합니다.

## Idempotency Key

```text
WB:{interfaceType}:{businessType}:{businessId}:v{businessVersion}
```

같은 업무 버전은 같은 키를 사용합니다. 업무가 수정되어 버전이 증가하면 새 키를 만듭니다. DB UNIQUE KEY가 동시 중복 생성을 막습니다.

## 실패 모드

| 모드 | Mock ERP 동작 | WorkBridge 처리 |
|---|---|---|
| `NORMAL` | 저장 후 200 | `SUCCESS` |
| `VALIDATION_400` | 저장하지 않고 400 | `PERMANENT_FAILED` |
| `HTTP_500` | 저장하지 않고 500 | `TEMPORARY_FAILED` |
| `TIMEOUT_BEFORE_COMMIT` | 저장 전 응답 지연 | 재시도 가능 |
| `TIMEOUT_AFTER_COMMIT` | 저장 후 응답 지연 | 같은 키로 재조회 |

4xx 업무 오류는 자동 재시도하지 않습니다. 408, 429, 5xx, 연결 타임아웃, 읽기 타임아웃은 설정된 횟수만큼 재시도합니다.

## 저장 후 타임아웃

ERP가 저장을 끝냈지만 응답이 늦으면 WorkBridge는 결과를 알 수 없습니다. 이 경우 같은 Idempotency Key로 다시 요청합니다. Mock ERP가 기존 처리 결과를 반환하면 WorkBridge 상태를 `SUCCESS`로 변경합니다.

## 로그

- 비밀번호와 토큰 원문은 저장하지 않습니다.
- 요청과 응답은 길이를 제한합니다.
- 전화번호, 이메일, 사업자번호는 필요한 범위에서 마스킹합니다.
- Trace ID를 인터페이스 원장과 호출 이력에 함께 저장합니다.

## 관리 API

Mock ERP 관리 API는 테스트 확인용이며 인증이 없습니다. 기본 Compose에서는 호스트 포트를 열지 않으므로 Docker 내부에서만 접근합니다.

## 제한

- 인메모리 저장소라 재시작하면 데이터가 초기화됩니다.
- 실제 ERP 인증, 인증서, 메시지 큐, 전문 암호화는 포함하지 않습니다.

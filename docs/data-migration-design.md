# CSV 고객 이관

## 입력 파일

- 확장자: `.csv`
- 인코딩: UTF-8, BOM 허용
- 최대 크기: 5MB
- 형식: RFC 4180
- 헤더와 데이터 행 필수

```text
business_registration_no,customer_name,representative_name,
contact_name,contact_email,contact_phone,established_date,
assigned_sales_employee_no
```

## 처리 순서

1. 파일 크기, UTF-8, 헤더를 확인합니다.
2. 파일 바이트의 SHA-256을 계산합니다.
3. 완료된 같은 파일이 있는지 확인합니다.
4. `migration_batch`를 생성합니다.
5. CSV를 읽어 500행 단위로 Staging에 저장합니다.
6. 행별 정규화, 검증, 기존 고객 비교를 수행합니다.
7. 오류를 `migration_error`에 저장합니다.
8. 정상 행만 고객 테이블에 반영합니다.
9. 결과를 `COMPLETED`, `COMPLETED_WITH_ERRORS`, `FAILED` 중 하나로 기록합니다.

## 검증 항목

- 사업자번호: 숫자, 공백, 하이픈만 허용하고 숫자 10자리로 정규화
- 고객명: 필수값과 길이
- 이메일: 형식과 길이
- 날짜: `yyyy-MM-dd`
- 담당 영업사번: 활성 직원이며 `SALES` 역할 보유
- 파일 안의 사업자번호 중복
- 열 개수: 8개

한 행에 오류가 여러 개 있으면 모두 저장합니다.

## Staging

Staging에는 원본 문자열, 정규화 값, 기존 고객 ID, 담당자 ID, 검증 상태와 반영 유형을 저장합니다.

```text
validation_status = VALID | INVALID
operation_type    = INSERT | UPDATE | NONE
```

## 반영 규칙

- 오류 행은 반영하지 않습니다.
- 정상 행은 하나의 트랜잭션에서 INSERT 또는 UPDATE합니다.
- 정상 행 적용 중 오류가 나면 정상 집합 전체를 롤백합니다.
- 오류가 일부 있어도 정상 반영이 끝나면 `COMPLETED_WITH_ERRORS`입니다.

## 동일 파일

파일 SHA-256을 `dedup_key`로 사용합니다. 같은 내용의 완료 파일은 `DUPLICATE_MIGRATION_FILE`로 차단합니다. 동시 요청은 DB UNIQUE KEY가 한 번 더 막습니다.

## 오류 CSV

```text
row_no,field_name,error_code,error_message,raw_value
```

큰따옴표를 이스케이프하고 `raw_value`가 `=`, `+`, `-`, `@`로 시작하면 작은따옴표를 붙입니다.

## 샘플

```text
sample-data/customers-valid.csv
sample-data/customers-invalid.csv
```

오류 샘플에는 중복, 필수값 누락, 잘못된 날짜, 존재하지 않는 담당자가 포함되어 있습니다.

현재 크기 제한은 5MB입니다. 더 큰 파일이 필요하면 업로드와 실행을 분리하고 비동기 배치로 바꾸는 편이 적합합니다.

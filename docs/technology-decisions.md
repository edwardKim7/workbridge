# 기술 선택 메모

## Java 17과 Spring Boot

서버는 Java 17과 Spring Boot 3을 사용합니다. MVC, Security, Validation, Actuator를 한 애플리케이션 안에서 관리할 수 있고 WAR 패키징도 지원하기 때문입니다.

`server`는 실행 가능한 WAR로 빌드합니다. Docker에서는 `java -jar`로 실행하고, 필요하면 Tomcat 10.1에 배포할 수 있습니다.

## JSP와 Fetch API

화면은 목록, 검색, 상세, 상태 변경이 대부분이라 JSP를 사용했습니다. 초기 HTML과 권한별 메뉴는 서버에서 만들고, 목록 갱신이나 상태 변경이 필요한 곳만 Fetch API를 사용합니다.

메뉴 숨김은 보안 처리로 보지 않습니다. Controller, REST API, Service에서 권한을 다시 확인합니다.

## MyBatis

다음 SQL은 조건이 업무 규칙과 직접 연결되어 있어 MyBatis로 명시했습니다.

- 상태와 버전을 함께 확인하는 UPDATE
- 역할과 메뉴 권한 조회
- 조건 검색과 페이징
- ERP 시도 이력 저장
- CSV Staging 배치 INSERT
- 운영 화면 집계

Mapper에는 SQL을 두고 상태 전이와 소유권 판단은 Service에 둡니다. 정렬 컬럼은 화이트리스트로 제한하고 `SELECT *`는 사용하지 않습니다.

## MySQL과 Flyway

기본 DB는 MySQL 8.4입니다. 로컬 환경은 Docker Compose로 맞추고, 스키마와 기준 데이터는 Flyway로 관리합니다.

Oracle과 Tibero는 실제 실행 대상이 아닙니다. Sequence, 페이징, 빈 문자열, Boolean, JSON, UPSERT, 날짜 함수는 DB별로 다시 확인해야 합니다. 관련 메모는 [`oracle-tibero-compatibility-review.md`](oracle-tibero-compatibility-review.md)에 있습니다.

## 인증 방식

- JSP: `HttpSession`, CSRF, 세션 고정 방지
- Flutter: Bearer Access Token, 회전형 Refresh Token

JSP는 세션을 사용하는 편이 단순했고, Flutter는 토큰 만료와 갱신 흐름이 필요했습니다. Refresh Token은 난수로 발급하고 DB에는 SHA-256 해시만 저장합니다.

## Flutter 범위

모바일은 승인자가 자주 사용하는 기능만 포함합니다.

- 로그인과 세션 복원
- 결재 대기 목록과 상세
- 승인과 반려
- 처리 이력
- 영업기회 요약

조직, 고객, 운영 관리 기능은 웹에 남겨 두었습니다. 상태 관리는 현재 화면 규모에 맞춰 `ChangeNotifier`를 사용합니다.

## Mock ERP

실제 ERP 대신 별도 Spring Boot 서비스를 사용합니다. 정상 처리, 400, 500, 저장 전 타임아웃, 저장 후 타임아웃, 동일 키 재전송을 확인할 수 있습니다.

Mock ERP는 인메모리 저장소를 사용하며 운영 대체 서비스가 아닙니다.

## Docker Compose

Compose는 MySQL, Mock ERP, WorkBridge 서버를 로컬에서 같은 설정으로 실행하기 위한 용도입니다.

- 서버는 `127.0.0.1`에만 바인딩
- MySQL과 Mock ERP는 호스트 포트 미공개
- 애플리케이션은 비루트 사용자로 실행
- 읽기 전용 파일시스템과 임시 `/tmp` 사용
- Linux capability 제거와 `no-new-privileges` 적용

외부 배포에서는 HTTPS, Reverse Proxy, Secret Manager, 방화벽, 로그와 백업 정책이 추가로 필요합니다.

## OpenAPI, Actuator, 테스트

Swagger UI는 API 요청과 응답 형식을 확인하는 용도로 사용하며 로그인 후 접근합니다. Actuator는 `health`, `info`만 노출합니다.

서버 테스트는 단위 테스트, MockMvc 통합 테스트, Flyway 확인, Mock ERP 테스트로 나눴습니다. H2 MySQL 모드만 믿지 않고 Docker MySQL에서도 마이그레이션과 실행 상태를 확인합니다.

## 현재 사용하지 않는 기술

| 기술 | 이유 |
|---|---|
| React, Vue | 현재 화면은 JSP와 Flutter로 처리 가능 |
| Redis | 분산 세션이나 캐시가 아직 필요하지 않음 |
| Kafka, RabbitMQ | 현재 ERP 전송량에서는 동기 호출과 제한 재시도로 처리 가능 |
| Kubernetes | 로컬 실행 범위에 비해 운영 구성이 큼 |
| 실제 ERP Connector | 실제 연동 계약과 테스트 환경이 없음 |

ERP 전송량이 늘어나면 Outbox와 메시지 큐를 우선 검토합니다.

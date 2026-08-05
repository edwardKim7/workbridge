# ADR-001: 웹 세션과 모바일 토큰 인증 분리

- 상태: 승인

## 배경

JSP 화면은 서버 렌더링과 세션 기반 메뉴가 중심이고, Flutter 앱은 REST API만 사용합니다. 두 채널에 같은 인증 방식을 억지로 적용할 필요가 없었습니다.

## 결정

- JSP 웹: Spring Security `HttpSession`과 CSRF
- Flutter 앱: JWT Access Token과 opaque Refresh Token
- Refresh Token: 원문 대신 SHA-256 해시 저장
- 갱신: 기존 Refresh Token 폐기 후 새 토큰 발급
- 사용자와 역할 모델: `EmployeePrincipal`, `ADMIN`, `SALES`, `APPROVER`, `VIEWER`
- 최종 업무 권한: Service에서 역할, 소유권, 지정 승인자 검사

## 이유

JSP는 Spring Security의 세션과 CSRF 흐름을 그대로 사용하는 편이 단순합니다. Flutter는 토큰 만료와 갱신을 앱에서 제어해야 하므로 별도 토큰 API가 필요합니다. 인증 방식은 다르지만 사용자와 권한 데이터는 공유합니다.

## 대안

- 전 채널 JWT: JSP에서 토큰 저장 위치와 CSRF, XSS 대응이 더 복잡해집니다.
- 전 채널 세션: 모바일에서 쿠키와 CSRF를 계속 관리해야 합니다.

## 영향

- 웹 요청은 CSRF 토큰을 포함해야 합니다.
- 모바일 Bearer API는 인증 실패와 권한 부족을 각각 401, 403으로 반환합니다.
- Access Token 즉시 폐기 목록은 두지 않고 짧은 만료시간과 Refresh Token 폐기로 관리합니다.

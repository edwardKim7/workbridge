# WorkBridge Mobile

승인자가 결재 목록을 확인하고 승인하거나 반려할 수 있는 Flutter 앱입니다. 서버의 모바일 REST API를 사용합니다.

## 포함 기능

- 로그인과 토큰 복원
- 결재 대기 목록
- 결재 상세와 처리 이력
- 승인과 반려
- 처리 완료 목록
- 영업기회 요약

## 준비

Android 호스트 프로젝트가 없다면 먼저 생성합니다.

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\bootstrap-mobile.ps1
```

이 스크립트는 `flutter create`를 실행하고 패키지를 설치합니다. 저장소에는 직접 작성한 `lib`, `test`, `pubspec.yaml`을 중심으로 관리합니다.

## 실행

Android 에뮬레이터:

```powershell
powershell -ExecutionPolicy Bypass `
  -File .\scripts\run-mobile.ps1 `
  -ApiBaseUrl http://10.0.2.2:8080
```

Windows 데스크톱으로 확인할 때는 `http://localhost:8080`을 사용할 수 있습니다.

실기기는 같은 네트워크의 개발 PC 주소를 사용해야 합니다. 서버를 `0.0.0.0`에 바인딩할 때는 Windows 방화벽 범위를 로컬 네트워크로 제한합니다.

## 인증

- Access Token은 API 요청에 사용합니다.
- Refresh Token은 `flutter_secure_storage`에 보관합니다.
- 401 응답이 오면 한 번 갱신한 뒤 원래 요청을 다시 시도합니다.
- 갱신이 실패하면 토큰을 지우고 로그인 화면으로 이동합니다.

## 테스트

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\test-mobile.ps1
```

JSON 파싱, 토큰 갱신, 중복 탭 방지, 오류 화면, 로그인 화면을 확인합니다.

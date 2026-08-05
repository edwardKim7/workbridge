# Windows에서 실행하기

## 준비 사항

웹 서버, MySQL, Mock ERP를 Docker로 실행하려면 다음 프로그램이 필요합니다.

- Git
- Docker Desktop
- Docker Compose 플러그인
- Windows PowerShell 5.1 이상

Flutter 앱을 실행하려면 Flutter SDK와 Android SDK 또는 에뮬레이터가 추가로 필요합니다.

## 저장소 준비

GitHub에서 내려받은 경우:

```powershell
git clone <repository-url>
Set-Location .\workbridge
```

ZIP으로 받은 경우 압축을 푼 뒤 `pom.xml`, `server`, `infra`, `scripts`가 보이는 폴더로 이동합니다.

## 실행 환경 확인

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\check-environment.ps1
```

현재 PowerShell의 실행 정책 때문에 스크립트가 막히면 현재 프로세스에서만 우회할 수 있습니다.

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
```

## 로컬 비밀값 생성

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\initialize-env.ps1
```

`infra/.env`에 DB 비밀번호와 JWT 서명 키가 임의 생성됩니다. 기존 파일을 다시 만들려면 `-Force`를 사용합니다.

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\initialize-env.ps1 -Force
```

## Docker 실행

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start.ps1 -Rebuild
```

컨테이너 상태 확인:

```powershell
docker compose --env-file .\infra\.env -f .\infra\docker-compose.yml ps
```

서버 상태 확인:

```powershell
powershell -ExecutionPolicy Bypass `
  -File .\scripts\verify-running.ps1 `
  -Port 8080
```

브라우저에서 `http://localhost:8080/login`으로 접속합니다.

## 테스트

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\test-all.ps1 -UseDocker
```

실행 중인 구성의 주요 흐름을 확인하려면 다음 명령을 사용합니다.

```powershell
$adminPassword = Read-Host 'admin 비밀번호' -AsSecureString
$approverPassword = Read-Host 'approver 비밀번호' -AsSecureString

powershell -ExecutionPolicy Bypass `
  -File .\scripts\smoke-test.ps1 `
  -AdminPassword $adminPassword `
  -ApproverPassword $approverPassword
```

## Flutter

최초 한 번 Android 호스트 프로젝트를 생성합니다.

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\bootstrap-mobile.ps1
```

분석과 테스트:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\test-mobile.ps1
```

Android 에뮬레이터 실행:

```powershell
powershell -ExecutionPolicy Bypass `
  -File .\scripts\run-mobile.ps1 `
  -ApiBaseUrl http://10.0.2.2:8080
```

실기기에서 접근할 때는 `infra/.env`의 `BIND_ADDRESS=0.0.0.0`을 신뢰할 수 있는 로컬 네트워크에서만 사용하고, Windows 방화벽의 원격 주소 범위를 제한합니다.

## WAR 생성

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-war.ps1 -UseDocker
```

생성 결과는 `server\target\workbridge.war`입니다.

## 종료와 초기화

```powershell
# DB 볼륨 유지
powershell -ExecutionPolicy Bypass -File .\scripts\stop.ps1

# DB 볼륨 삭제
powershell -ExecutionPolicy Bypass -File .\scripts\stop.ps1 -RemoveData
```

## 자주 확인할 오류

### Docker 엔진에 연결할 수 없음

Docker Desktop이 실행 중인지 확인한 뒤 `docker info`를 실행합니다.

### JWT 키 길이 오류

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\initialize-env.ps1 -Force
```

### 기존 DB 때문에 마이그레이션 결과가 다름

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\stop.ps1 -RemoveData
powershell -ExecutionPolicy Bypass -File .\scripts\start.ps1 -Rebuild
```

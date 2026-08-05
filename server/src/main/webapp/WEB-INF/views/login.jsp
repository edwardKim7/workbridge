<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <title>WorkBridge | 로그인</title>
    <%@ include file="fragments/head.jspf" %>
</head>
<body class="login-page" data-context-path="${pageContext.request.contextPath}">
<div class="login-shell">
    <section class="login-brand-panel">
        <div>
            <span class="brand-mark brand-mark-large">WB</span>
            <p class="eyebrow">SALES · APPROVAL · INTEGRATION</p>
            <h1>사내 영업지원과<br>승인 업무를 연결합니다.</h1>
            <p class="login-description">
                Java·Spring 기반 JSP 업무시스템에 세션 인증, 역할·메뉴 권한,
                조직 관리와 운영 추적 기능을 하나의 흐름으로 연결했습니다.
            </p>
        </div>
        <div class="login-security-list">
            <div><span>01</span><p><strong>BCrypt 비밀번호</strong><br>원문 저장·응답·로그 출력을 차단합니다.</p></div>
            <div><span>02</span><p><strong>실패 계정 잠금</strong><br>기본 5회 실패 시 15분간 로그인을 제한합니다.</p></div>
            <div><span>03</span><p><strong>세션·CSRF 보호</strong><br>JSP 웹 특성에 맞는 표준 인증 흐름을 사용합니다.</p></div>
        </div>
    </section>

    <main class="login-card">
        <div class="login-card-heading">
            <span class="chip">Secure session</span>
            <h2>WorkBridge 로그인</h2>
            <p>개발 확인용 계정은 README의 샘플 계정 표를 확인해 주세요.</p>
        </div>

        <c:if test="${not empty param.error}">
            <div class="alert alert-danger" role="alert">
                아이디 또는 비밀번호가 올바르지 않거나 계정이 비활성·잠금 상태입니다.
            </div>
        </c:if>
        <c:if test="${not empty param.logout}">
            <div class="alert alert-success" role="status">안전하게 로그아웃되었습니다.</div>
        </c:if>
        <c:if test="${not empty param.inactive}">
            <div class="alert alert-warning" role="alert">관리자에 의해 계정이 비활성화되어 세션이 종료되었습니다.</div>
        </c:if>

        <form class="login-form" action="<c:url value='/login'/>" method="post" autocomplete="on">
            <input type="hidden" name="<c:out value='${_csrf.parameterName}'/>" value="<c:out value='${_csrf.token}'/>">
            <label class="field-label" for="login-id">로그인 ID</label>
            <input class="field-control" id="login-id" name="loginId" type="text" maxlength="50"
                   autocomplete="username" required autofocus placeholder="로그인 ID를 입력하세요">

            <label class="field-label" for="password">비밀번호</label>
            <input class="field-control" id="password" name="password" type="password" maxlength="72"
                   autocomplete="current-password" required placeholder="비밀번호를 입력하세요">

            <button class="button button-primary login-button" type="submit">로그인</button>
        </form>

        <div class="login-policy">
            <strong>로그인 실패 정책</strong>
            <p>사용자 존재 여부를 구분하지 않는 공통 메시지를 표시하고, 실패 이력에는 비밀번호를 저장하지 않습니다.</p>
        </div>
    </main>
</div>
</body>
</html>

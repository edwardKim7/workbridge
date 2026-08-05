<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <title>WorkBridge | 오류</title>
    <%@ include file="fragments/head.jspf" %>
</head>
<body class="error-page">
<main class="error-card">
    <span class="brand-mark" aria-hidden="true">WB</span>
    <p class="eyebrow">REQUEST COULD NOT BE COMPLETED</p>
    <h1>요청을 처리하지 못했습니다.</h1>
    <p>잠시 후 다시 시도해 주세요. 문제가 반복되면 아래 Trace ID를 확인해 운영 담당자에게 전달해 주세요.</p>
    <dl>
        <div><dt>HTTP 상태</dt><dd><c:out value="${status}" default="500"/></dd></div>
        <div><dt>요청 경로</dt><dd><c:out value="${path}" default="-"/></dd></div>
        <div><dt>Trace ID</dt><dd><c:out value="${requestScope['workbridge.traceId']}" default="unavailable"/></dd></div>
    </dl>
    <a class="button button-primary" href="<c:url value='/'/>">기반 화면으로 돌아가기</a>
</main>
</body>
</html>

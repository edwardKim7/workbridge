<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <title>WorkBridge | 접근 권한 없음</title>
    <%@ include file="fragments/head.jspf" %>
</head>
<body data-context-path="${pageContext.request.contextPath}">
<div class="app-shell">
    <%@ include file="fragments/header.jspf" %>
    <%@ include file="fragments/sidebar.jspf" %>
    <main class="content">
        <section class="empty-state-card access-denied-card">
            <span class="empty-state-icon">403</span>
            <p class="eyebrow">ACCESS DENIED</p>
            <h1>이 메뉴를 사용할 권한이 없습니다.</h1>
            <p>메뉴 노출 여부와 별도로 서버에서 권한을 다시 확인했습니다. 필요한 경우 관리자에게 역할과 메뉴 권한을 확인해 주세요.</p>
            <a class="button button-primary" href="<c:url value='/web/dashboard'/>">대시보드로 이동</a>
        </section>
        <%@ include file="fragments/footer.jspf" %>
    </main>
</div>
</body>
</html>

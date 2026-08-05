<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <title>WorkBridge | 인터페이스 상세</title>
    <%@ include file="../fragments/head.jspf" %>
</head>
<body data-context-path="${pageContext.request.contextPath}" data-interface-id="${interfaceId}">
<div class="app-shell">
    <%@ include file="../fragments/header.jspf" %>
    <%@ include file="../fragments/sidebar.jspf" %>

    <main class="content">
        <section class="page-heading">
            <div>
                <p class="eyebrow">INTERFACE TRACE</p>
                <h1>인터페이스 상세</h1>
                <p>전문은 개인정보를 마스킹하고 길이 제한 후 저장합니다.</p>
            </div>
            <a class="button button-secondary-light" href="<c:url value='/web/admin/interfaces'/>">목록</a>
        </section>

        <section class="detail-grid">
            <article class="panel">
                <div class="panel-heading">
                    <h2>요청·응답</h2>
                    <span id="interface-detail-status" class="status-badge"></span>
                </div>
                <dl id="interface-detail-list" class="detail-list"></dl>

                <h3>요청 전문</h3>
                <pre id="interface-request" class="code-block"></pre>

                <h3>응답 전문</h3>
                <pre id="interface-response" class="code-block"></pre>

                <div id="interface-retry-controls" class="form-actions">
                    <select id="interface-mode" class="field-control compact-control">
                        <option>NORMAL</option>
                        <option>HTTP_500</option>
                        <option>TIMEOUT_BEFORE_COMMIT</option>
                        <option>TIMEOUT_AFTER_COMMIT</option>
                        <option>VALIDATION_400</option>
                    </select>
                    <button id="interface-retry" class="button button-primary">재처리</button>
                </div>
            </article>

            <aside class="panel">
                <div class="panel-heading">
                    <h2>시도 이력</h2>
                </div>
                <div id="interface-attempts" class="activity-timeline"></div>
            </aside>
        </section>

        <%@ include file="../fragments/footer.jspf" %>
    </main>
</div>

<div id="toast-region" class="toast-region"></div>
<script src="<c:url value='/js/common.js'/>"></script>
<script src="<c:url value='/js/interface-detail.js'/>"></script>
</body>
</html>

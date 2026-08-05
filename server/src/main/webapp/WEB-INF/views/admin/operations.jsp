<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <title>WorkBridge | 시스템 운영</title>
    <%@ include file="../fragments/head.jspf" %>
</head>
<body data-context-path="${pageContext.request.contextPath}">
<div class="app-shell">
    <%@ include file="../fragments/header.jspf" %>
    <%@ include file="../fragments/sidebar.jspf" %>

    <main class="content">
        <section class="page-heading">
            <div>
                <p class="eyebrow">SYSTEM OPERATIONS</p>
                <h1>시스템 운영 현황</h1>
                <p>서버·DB 상태, 결재·인터페이스·이관 결과와 Trace ID 기반 오류를 함께 조회합니다.</p>
            </div>
            <button id="operations-refresh" class="button button-primary">새로고침</button>
        </section>

        <section id="operations-metrics" class="metric-grid compact-metric-grid"></section>

        <section class="split-panel-grid section-panel">
            <article class="panel">
                <div class="panel-heading">
                    <h2>최근 인터페이스</h2>
                </div>
                <div id="operations-interfaces" class="operation-list"></div>
            </article>
            <article class="panel">
                <div class="panel-heading">
                    <h2>최근 이관</h2>
                </div>
                <div id="operations-migrations" class="operation-list"></div>
            </article>
        </section>

        <section class="split-panel-grid section-panel">
            <article class="panel">
                <div class="panel-heading">
                    <h2>감사 로그 검색</h2>
                </div>
                <form id="audit-search" class="filter-grid compact-filter">
                    <label>
                        Action
                        <input id="audit-action" class="field-control">
                    </label>
                    <label>
                        Trace ID
                        <input id="audit-trace" class="field-control">
                    </label>
                    <button class="button button-primary">검색</button>
                </form>
                <div id="audit-results" class="operation-list"></div>
            </article>

            <article class="panel">
                <div class="panel-heading">
                    <h2>API 오류 검색</h2>
                </div>
                <form id="error-search" class="filter-grid compact-filter">
                    <label>
                        HTTP
                        <input id="error-status" type="number" class="field-control">
                    </label>
                    <label>
                        Trace ID
                        <input id="error-trace" class="field-control">
                    </label>
                    <button class="button button-primary">검색</button>
                </form>
                <div id="error-results" class="operation-list"></div>
            </article>
        </section>

        <%@ include file="../fragments/footer.jspf" %>
    </main>
</div>

<div id="toast-region" class="toast-region"></div>
<script src="<c:url value='/js/common.js'/>"></script>
<script src="<c:url value='/js/operations.js'/>"></script>
</body>
</html>

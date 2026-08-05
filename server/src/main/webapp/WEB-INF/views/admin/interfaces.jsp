<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <title>WorkBridge | 인터페이스 운영</title>
    <%@ include file="../fragments/head.jspf" %>
</head>
<body data-context-path="${pageContext.request.contextPath}">
<div class="app-shell">
    <%@ include file="../fragments/header.jspf" %>
    <%@ include file="../fragments/sidebar.jspf" %>

    <main class="content">
        <section class="page-heading">
            <div>
                <p class="eyebrow">ERP INTERFACE OPERATIONS</p>
                <h1>외부 인터페이스 처리 이력</h1>
                <p>Mock ERP 요청·응답, 실패 유형, Idempotency Key와 Trace ID를 확인하고 재처리합니다.</p>
            </div>
        </section>

        <section class="filter-panel">
            <form id="interface-search" class="filter-grid compact-filter">
                <label>
                    상태
                    <select id="interface-status" class="field-control">
                        <option value="">전체</option>
                        <option>SUCCESS</option>
                        <option>TEMPORARY_FAILED</option>
                        <option>PERMANENT_FAILED</option>
                    </select>
                </label>
                <label>
                    Trace ID
                    <input id="interface-trace" class="field-control">
                </label>
                <button class="button button-primary">검색</button>
            </form>
        </section>

        <section class="panel table-panel">
            <div class="panel-heading">
                <h2>처리 목록</h2>
                <span id="interface-count" class="result-count"></span>
            </div>
            <div class="data-table-wrap">
                <table class="data-table">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>업무</th>
                        <th>상태</th>
                        <th>HTTP</th>
                        <th>재시도</th>
                        <th>Trace ID</th>
                        <th>처리시각</th>
                    </tr>
                    </thead>
                    <tbody id="interface-body"></tbody>
                </table>
            </div>
            <div id="interface-pagination" class="pagination"></div>
        </section>

        <%@ include file="../fragments/footer.jspf" %>
    </main>
</div>

<div id="toast-region" class="toast-region"></div>
<script src="<c:url value='/js/common.js'/>"></script>
<script src="<c:url value='/js/interfaces.js'/>"></script>
</body>
</html>

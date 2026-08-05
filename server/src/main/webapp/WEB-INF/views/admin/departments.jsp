<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <title>WorkBridge | 부서 관리</title>
    <%@ include file="../fragments/head.jspf" %>
</head>
<body data-context-path="${pageContext.request.contextPath}" data-can-write="${canWriteAdmin}">
<div class="app-shell">
    <%@ include file="../fragments/header.jspf" %>
    <%@ include file="../fragments/sidebar.jspf" %>

    <main class="content">
        <section class="page-heading">
            <div><p class="eyebrow">ADMINISTRATION</p><h1>부서 관리</h1><p>상·하위 조직 관계와 활성 상태를 관리하고, 순환 구조와 사용 중 비활성화를 차단합니다.</p></div>
            <button class="button button-primary" id="new-department-button" type="button" <c:if test="${not canWriteAdmin}">disabled</c:if>>신규 부서</button>
        </section>
        <%@ include file="../fragments/admin-tabs.jspf" %>

        <section class="filter-panel">
            <div class="filter-grid department-filter-grid">
                <label><span>부서 검색</span><input class="field-control" id="department-keyword" type="search" placeholder="부서코드·부서명"></label>
                <label><span>상태</span><select class="field-control" id="department-active-filter"><option value="">전체</option><option value="true">활성</option><option value="false">비활성</option></select></label>
                <button class="button button-primary" id="department-search-button" type="button">조회</button>
            </div>
        </section>

        <section class="workspace-grid">
            <article class="panel table-panel">
                <div class="panel-heading"><div><p class="eyebrow">DEPARTMENT LIST</p><h2>부서 목록</h2></div><span class="result-count" id="department-result-count">0건</span></div>
                <div class="data-table-wrap">
                    <table class="data-table">
                        <thead><tr><th>부서코드</th><th>부서명</th><th>상위 부서</th><th>상태</th><th>수정 버전</th></tr></thead>
                        <tbody id="department-table-body"><tr><td colspan="5" class="table-empty">부서 정보를 불러오는 중입니다.</td></tr></tbody>
                    </table>
                </div>
                <div class="pagination" id="department-pagination"></div>
            </article>

            <aside class="panel editor-panel">
                <div class="panel-heading"><div><p class="eyebrow" id="department-form-eyebrow">NEW DEPARTMENT</p><h2 id="department-form-title">부서 등록</h2></div><button class="icon-button" id="department-form-reset" type="button">↺</button></div>
                <form id="department-form" novalidate>
                    <input type="hidden" id="department-id">
                    <input type="hidden" id="department-version" value="0">
                    <label><span>부서코드 *</span><input class="field-control" id="department-code" maxlength="30" placeholder="SERVICE_OPS" required></label>
                    <label><span>부서명 *</span><input class="field-control" id="department-name" maxlength="100" required></label>
                    <label><span>상위 부서</span><select class="field-control" id="parent-department"><option value="">최상위 부서</option></select></label>
                    <label class="checkbox-line"><input id="department-active" type="checkbox" checked><span>활성 부서로 사용</span></label>
                    <div class="form-actions"><button class="button button-primary" id="department-save-button" type="submit" <c:if test="${not canWriteAdmin}">disabled</c:if>>부서 등록</button></div>
                </form>
                <div class="form-message" id="department-form-message" role="status"></div>
                <div class="policy-note"><strong>비활성화 기준</strong><p>활성 직원 또는 활성 하위 부서가 남아 있으면 정합성을 위해 비활성화를 거부합니다.</p></div>
            </aside>
        </section>

        <%@ include file="../fragments/footer.jspf" %>
    </main>
</div>
<div id="toast-region" class="toast-region" aria-live="polite"></div>
<script src="<c:url value='/js/common.js'/>"></script>
<script src="<c:url value='/js/departments.js'/>"></script>
</body>
</html>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <title>WorkBridge | 역할·메뉴 권한</title>
    <%@ include file="../fragments/head.jspf" %>
</head>
<body data-context-path="${pageContext.request.contextPath}" data-can-write="${canWriteAdmin}">
<div class="app-shell">
    <%@ include file="../fragments/header.jspf" %>
    <%@ include file="../fragments/sidebar.jspf" %>

    <main class="content">
        <section class="page-heading">
            <div><p class="eyebrow">ADMINISTRATION</p><h1>역할·메뉴 권한</h1><p>고정 역할 네 종류에 대해 읽기·쓰기 메뉴 권한을 관리하고 API에서도 같은 기준을 재검증합니다.</p></div>
            <button class="button button-primary" id="role-permission-save" type="button" <c:if test="${not canWriteAdmin}">disabled</c:if>>권한 저장</button>
        </section>
        <%@ include file="../fragments/admin-tabs.jspf" %>

        <section class="role-workspace">
            <aside class="panel role-list-panel">
                <div class="panel-heading"><div><p class="eyebrow">REFERENCE ROLES</p><h2>역할</h2></div><span class="chip">Fixed codes</span></div>
                <div class="role-select-list" id="role-select-list"><p class="table-empty">역할을 불러오는 중입니다.</p></div>
            </aside>

            <article class="panel permission-panel">
                <div class="panel-heading">
                    <div><p class="eyebrow" id="selected-role-code">ROLE</p><h2 id="selected-role-name">역할을 선택하세요</h2><p id="selected-role-description" class="panel-description"></p></div>
                    <span class="version-badge" id="selected-role-version">v-</span>
                </div>
                <div class="permission-table-wrap">
                    <table class="data-table permission-table">
                        <thead><tr><th>메뉴</th><th>경로</th><th>읽기</th><th>쓰기</th></tr></thead>
                        <tbody id="permission-table-body"><tr><td colspan="4" class="table-empty">왼쪽에서 역할을 선택해 주세요.</td></tr></tbody>
                    </table>
                </div>
                <div class="policy-note"><strong>권한 규칙</strong><p>쓰기 권한에는 읽기 권한이 필요하며, 하위 메뉴를 허용하면 상위 메뉴의 읽기 권한을 자동 보완합니다. 모든 역할의 대시보드 읽기와 ADMIN의 사용자·권한 쓰기는 접근 경로 유실을 막기 위해 유지합니다.</p></div>
                <div class="form-message" id="role-form-message" role="status"></div>
            </article>
        </section>

        <%@ include file="../fragments/footer.jspf" %>
    </main>
</div>
<div id="toast-region" class="toast-region" aria-live="polite"></div>
<script src="<c:url value='/js/common.js'/>"></script>
<script src="<c:url value='/js/roles.js'/>"></script>
</body>
</html>

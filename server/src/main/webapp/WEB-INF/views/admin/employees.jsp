<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <title>WorkBridge | 직원 관리</title>
    <%@ include file="../fragments/head.jspf" %>
</head>
<body data-context-path="${pageContext.request.contextPath}" data-can-write="${canWriteAdmin}">
<div class="app-shell">
    <%@ include file="../fragments/header.jspf" %>
    <%@ include file="../fragments/sidebar.jspf" %>

    <main class="content">
        <section class="page-heading">
            <div><p class="eyebrow">ADMINISTRATION</p><h1>직원·계정 관리</h1><p>직원 기본정보, 활성 상태, 로그인 잠금과 역할을 분리해 관리합니다.</p></div>
            <button class="button button-primary" id="new-employee-button" type="button" <c:if test="${not canWriteAdmin}">disabled</c:if>>신규 직원</button>
        </section>
        <%@ include file="../fragments/admin-tabs.jspf" %>

        <section class="filter-panel">
            <div class="filter-grid employee-filter-grid">
                <label><span>통합 검색</span><input class="field-control" id="employee-keyword" type="search" placeholder="사번·ID·이름·이메일"></label>
                <label><span>부서</span><select class="field-control" id="employee-department-filter"><option value="">전체 부서</option></select></label>
                <label><span>상태</span><select class="field-control" id="employee-active-filter"><option value="">전체</option><option value="true">활성</option><option value="false">비활성</option></select></label>
                <button class="button button-primary" id="employee-search-button" type="button">조회</button>
            </div>
        </section>

        <section class="workspace-grid">
            <article class="panel table-panel">
                <div class="panel-heading">
                    <div><p class="eyebrow">EMPLOYEE LIST</p><h2>직원 목록</h2></div>
                    <span class="result-count" id="employee-result-count">0건</span>
                </div>
                <div class="data-table-wrap">
                    <table class="data-table">
                        <thead><tr><th>사번 / 로그인 ID</th><th>직원명</th><th>부서</th><th>역할</th><th>계정 상태</th><th>최근 로그인</th></tr></thead>
                        <tbody id="employee-table-body"><tr><td colspan="6" class="table-empty">직원 정보를 불러오는 중입니다.</td></tr></tbody>
                    </table>
                </div>
                <div class="pagination" id="employee-pagination"></div>
            </article>

            <aside class="panel editor-panel">
                <div class="panel-heading">
                    <div><p class="eyebrow" id="employee-form-eyebrow">NEW EMPLOYEE</p><h2 id="employee-form-title">직원 등록</h2></div>
                    <button class="icon-button" id="employee-form-reset" type="button" aria-label="입력 초기화">↺</button>
                </div>
                <form id="employee-form" novalidate>
                    <input type="hidden" id="employee-id">
                    <input type="hidden" id="employee-version" value="0">
                    <div class="form-grid two-fields">
                        <label><span>사번 *</span><input class="field-control" id="employee-no" maxlength="30" placeholder="WB-4001" required></label>
                        <label><span>로그인 ID *</span><input class="field-control" id="employee-login-id" maxlength="50" placeholder="new.user" required></label>
                    </div>
                    <label><span>직원명 *</span><input class="field-control" id="employee-name" maxlength="100" required></label>
                    <label><span>이메일 *</span><input class="field-control" id="employee-email" type="email" maxlength="200" required></label>
                    <label><span>부서 *</span><select class="field-control" id="employee-department" required><option value="">부서를 선택하세요</option></select></label>
                    <label id="password-field"><span>초기 비밀번호 *</span><input class="field-control" id="employee-password" type="password" maxlength="72" autocomplete="new-password" placeholder="대·소문자/숫자/특수문자 포함 10자 이상"></label>
                    <label class="checkbox-line" id="create-active-field"><input id="employee-active" type="checkbox" checked><span>등록 즉시 계정 활성화</span></label>

                    <fieldset class="role-fieldset">
                        <legend>역할 *</legend>
                        <div class="role-check-grid" id="employee-role-options"></div>
                    </fieldset>

                    <div class="form-actions">
                        <button class="button button-primary" id="employee-save-button" type="submit" <c:if test="${not canWriteAdmin}">disabled</c:if>>직원 등록</button>
                        <button class="button button-secondary-light hidden" id="employee-role-save-button" type="button" <c:if test="${not canWriteAdmin}">disabled</c:if>>역할 저장</button>
                    </div>
                </form>

                <div class="account-actions hidden" id="employee-account-actions">
                    <div><strong>계정 운영</strong><p id="employee-account-state">선택한 직원의 계정 상태를 확인합니다.</p></div>
                    <div class="button-row">
                        <button class="button button-warning" id="employee-state-button" type="button" <c:if test="${not canWriteAdmin}">disabled</c:if>>비활성화</button>
                        <button class="button button-secondary-light" id="employee-unlock-button" type="button" <c:if test="${not canWriteAdmin}">disabled</c:if>>잠금 해제</button>
                    </div>
                </div>
                <div class="form-message" id="employee-form-message" role="status"></div>
            </aside>
        </section>

        <%@ include file="../fragments/footer.jspf" %>
    </main>
</div>
<div id="toast-region" class="toast-region" aria-live="polite"></div>
<script src="<c:url value='/js/common.js'/>"></script>
<script src="<c:url value='/js/employees.js'/>"></script>
</body>
</html>

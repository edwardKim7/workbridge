<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <title>WorkBridge | 대시보드</title>
    <%@ include file="fragments/head.jspf" %>
</head>
<body data-context-path="${pageContext.request.contextPath}">
<div class="app-shell">
    <%@ include file="fragments/header.jspf" %>
    <%@ include file="fragments/sidebar.jspf" %>

    <main class="content">
        <section class="page-hero compact-hero">
            <div>
                <p class="eyebrow">SALES SUPPORT</p>
                <h1><c:out value="${currentUser.employeeName}"/>님, 업무를 시작하세요.</h1>
                <p>조직 권한과 업무 소유권을 기준으로 고객·영업기회·활동 현황을 조회합니다.</p>
            </div>
            <div class="identity-card">
                <span class="status-dot up"></span>
                <div><small>AUTHENTICATED SESSION</small><strong><c:out value="${currentUser.loginId}"/></strong><span><c:out value="${currentUser.departmentName}"/></span></div>
            </div>
        </section>

        <section class="metric-grid" aria-label="조직·권한 현황">
            <article class="metric-card"><span class="metric-label">Active employees</span><strong id="active-employees">-</strong><p id="employee-total">전체 직원 확인 중</p></article>
            <article class="metric-card"><span class="metric-label">Active departments</span><strong id="active-departments">-</strong><p id="department-total">전체 부서 확인 중</p></article>
            <article class="metric-card"><span class="metric-label">Locked accounts</span><strong id="locked-employees">-</strong><p>현재 시각 기준 로그인 잠금</p></article>
            <article class="metric-card"><span class="metric-label">Reference roles</span><strong id="active-roles">-</strong><p>ADMIN · SALES · APPROVER · VIEWER</p></article>
        </section>

        <section class="panel sales-dashboard-panel" aria-label="영업 현황">
            <div class="panel-heading">
                <div><p class="eyebrow">SALES PIPELINE</p><h2>영업 현황</h2><p class="panel-description">SALES 역할은 본인 담당 데이터, ADMIN·조회 역할은 전체 데이터를 기준으로 집계합니다.</p></div>
                <span id="sales-dashboard-scope" class="chip chip-neutral">범위 확인 중</span>
            </div>
            <div class="metric-grid compact-metric-grid sales-metric-grid">
                <article class="metric-card"><span class="metric-label">Customers</span><strong id="sales-total-customers">-</strong><p id="sales-customer-detail">활성·잠재 고객 확인 중</p></article>
                <article class="metric-card"><span class="metric-label">Open opportunities</span><strong id="sales-open-opportunities">-</strong><p id="sales-close-detail">이번 달 종료 예정 확인 중</p></article>
                <article class="metric-card"><span class="metric-label">Open pipeline</span><strong id="sales-open-pipeline" class="metric-text">-</strong><p>진행 중 예상매출 합계</p></article>
                <article class="metric-card"><span class="metric-label">Weighted pipeline</span><strong id="sales-weighted-pipeline" class="metric-text">-</strong><p>예상매출 × 수주확률</p></article>
            </div>
            <div id="sales-stage-counts" class="stage-count-grid" aria-label="영업단계별 건수"></div>
            <div class="form-actions dashboard-shortcuts">
                <c:if test="${canReadCustomer}"><a class="button button-secondary-light" href="<c:url value='/web/customers'/>">고객 관리</a></c:if>
                <c:if test="${canReadOpportunity}"><a class="button button-secondary-light" href="<c:url value='/web/opportunities'/>">영업기회</a></c:if>
                <c:if test="${canReadActivity}"><a class="button button-secondary-light" href="<c:url value='/web/activities'/>">영업활동</a></c:if>
            </div>
        </section>

        <section class="two-column-grid">
            <article class="panel">
                <div class="panel-heading"><div><p class="eyebrow">CURRENT IDENTITY</p><h2>로그인 사용자</h2></div><span class="chip">HttpSession</span></div>
                <dl class="detail-list">
                    <div><dt>사번</dt><dd><c:out value="${currentUser.employeeNo}"/></dd></div>
                    <div><dt>로그인 ID</dt><dd><c:out value="${currentUser.loginId}"/></dd></div>
                    <div><dt>이메일</dt><dd><c:out value="${currentUser.email}"/></dd></div>
                    <div><dt>부서</dt><dd><c:out value="${currentUser.departmentName}"/></dd></div>
                </dl>
                <div class="role-chip-list" id="current-role-list" aria-label="현재 역할"></div>
            </article>

            <article class="panel">
                <div class="panel-heading"><div><p class="eyebrow">SECURITY FLOW</p><h2>웹 권한 검증</h2></div><span class="chip chip-neutral">Server enforced</span></div>
                <ol class="flow-list compact-flow">
                    <li><span>1</span><div><strong>BCrypt 인증</strong><p>DB 해시와 입력 비밀번호를 비교합니다.</p></div></li>
                    <li><span>2</span><div><strong>세션 고정 방지</strong><p>로그인 성공 시 세션 식별자를 변경합니다.</p></div></li>
                    <li><span>3</span><div><strong>메뉴 권한 조회</strong><p>역할별 읽기 권한으로 JSP 사이드 메뉴를 구성합니다.</p></div></li>
                    <li><span>4</span><div><strong>API 재검증</strong><p>URL 직접 호출도 메서드 권한에서 다시 차단합니다.</p></div></li>
                </ol>
            </article>
        </section>

        <section class="panel endpoints-panel">
            <div class="panel-heading"><div><p class="eyebrow">AUTHORIZED ENDPOINTS</p><h2>주요 조회 API</h2></div><span class="chip">CSRF protected writes</span></div>
            <div class="endpoint-table" role="table">
                <div class="endpoint-row endpoint-head"><span>Method</span><span>Path</span><span>Purpose</span></div>
                <div class="endpoint-row"><span class="method get">GET</span><code>/api/v1/sales/dashboard</code><span>역할 범위를 적용한 고객·파이프라인 집계</span></div>
                <div class="endpoint-row"><span class="method get">GET</span><code>/api/v1/customers</code><span>고객 조건 검색과 페이징 조회</span></div>
                <div class="endpoint-row"><span class="method get">GET</span><code>/api/v1/opportunities</code><span>단계·상태·담당자별 영업기회 조회</span></div>
                <div class="endpoint-row"><span class="method get">GET</span><code>/api/v1/activities</code><span>고객·기회에 연결된 영업활동 이력 조회</span></div>
            </div>
        </section>

        <%@ include file="fragments/footer.jspf" %>
    </main>
</div>
<div id="toast-region" class="toast-region" aria-live="polite"></div>
<script src="<c:url value='/js/common.js'/>"></script>
<script src="<c:url value='/js/sales-common.js'/>"></script>
<script src="<c:url value='/js/dashboard.js'/>"></script>
</body>
</html>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <title>WorkBridge | 영업활동</title>
    <%@ include file="../fragments/head.jspf" %>
</head>
<body data-context-path="${pageContext.request.contextPath}" data-can-write-activity="${canWriteActivity}">
<div class="app-shell">
    <%@ include file="../fragments/header.jspf" %>
    <%@ include file="../fragments/sidebar.jspf" %>

    <main class="content">
        <section class="page-heading">
            <div><p class="eyebrow">SALES SUPPORT · ACTIVITY</p><h1>영업활동</h1><p>고객 방문, 회의, 전화, 이메일 이력을 고객 및 영업기회와 연결해 기록합니다.</p></div>
            <button class="button button-primary" id="new-activity-button" type="button" <c:if test="${not canWriteSales}">disabled</c:if>>영업활동 등록</button>
        </section>

        <section class="filter-panel">
            <form class="filter-grid activity-filter-grid" id="activity-search-form">
                <label><span>통합 검색</span><input class="field-control" id="activity-keyword" type="search" placeholder="제목·내용·고객명"></label>
                <label><span>고객</span><select class="field-control" id="activity-customer-filter"><option value="">전체 고객</option></select></label>
                <label><span>활동 유형</span><select class="field-control" id="activity-type-filter"><option value="">전체 유형</option><option value="VISIT">고객 방문</option><option value="CALL">전화</option><option value="MEETING">회의</option><option value="EMAIL">이메일</option><option value="OTHER">기타</option></select></label>
                <label><span>담당자</span><select class="field-control" id="activity-employee-filter"><option value="">전체 담당자</option></select></label>
                <label><span>시작일</span><input class="field-control" id="activity-from-date" type="date"></label>
                <label><span>종료일</span><input class="field-control" id="activity-to-date" type="date"></label>
                <button class="button button-primary" type="submit">조회</button>
            </form>
        </section>

        <section class="workspace-grid sales-workspace-grid activity-workspace-grid">
            <article class="panel table-panel">
                <div class="panel-heading"><div><p class="eyebrow">ACTIVITY HISTORY</p><h2>영업활동 이력</h2></div><span class="result-count" id="activity-result-count">0건</span></div>
                <div class="data-table-wrap"><table class="data-table sales-table activity-table"><thead><tr><th>일시 / 유형</th><th>고객</th><th>활동 제목</th><th>관련 영업기회</th><th>담당자</th><th>장소</th></tr></thead><tbody id="activity-table-body"><tr><td colspan="6" class="table-empty">영업활동을 불러오는 중입니다.</td></tr></tbody></table></div>
                <div class="pagination" id="activity-pagination"></div>
            </article>

            <aside class="panel editor-panel" id="activity-editor-panel">
                <div class="panel-heading"><div><p class="eyebrow">NEW ACTIVITY</p><h2>영업활동 등록</h2></div><button class="icon-button" id="activity-form-reset" type="button">↺</button></div>
                <form id="activity-form" novalidate>
                    <label><span>고객 *</span><select class="field-control" id="activity-customer" required><option value="">고객을 선택하세요</option></select></label>
                    <label><span>관련 영업기회</span><select class="field-control" id="activity-opportunity"><option value="">연결하지 않음</option></select></label>
                    <div class="form-grid two-fields">
                        <label><span>활동 유형 *</span><select class="field-control" id="activity-type"><option value="VISIT">고객 방문</option><option value="CALL">전화</option><option value="MEETING">회의</option><option value="EMAIL">이메일</option><option value="OTHER">기타</option></select></label>
                        <label><span>활동 일시 *</span><input class="field-control" id="activity-at" type="datetime-local" required></label>
                    </div>
                    <label><span>활동 담당자 *</span><select class="field-control" id="activity-employee" required><option value="">담당자를 선택하세요</option></select></label>
                    <label><span>활동 제목 *</span><input class="field-control" id="activity-subject" maxlength="200" required></label>
                    <label><span>장소</span><input class="field-control" id="activity-location" maxlength="300"></label>
                    <label><span>활동 내용</span><textarea class="field-control field-textarea" id="activity-content" maxlength="4000" rows="6"></textarea></label>
                    <div class="form-actions"><button class="button button-primary" id="activity-save-button" type="submit" <c:if test="${not canWriteSales}">disabled</c:if>>활동 등록</button></div>
                </form>
                <div class="policy-note"><strong>고객-영업기회 연결 검증</strong><p>선택한 영업기회가 다른 고객에 속하면 저장하지 않고 400 오류로 처리합니다.</p></div>
                <div class="form-message" id="activity-form-message" role="status"></div>
            </aside>
        </section>

        <%@ include file="../fragments/footer.jspf" %>
    </main>
</div>
<div id="toast-region" class="toast-region" aria-live="polite"></div>
<script src="<c:url value='/js/common.js'/>"></script>
<script src="<c:url value='/js/sales-common.js'/>"></script>
<script src="<c:url value='/js/activities.js'/>"></script>
</body>
</html>

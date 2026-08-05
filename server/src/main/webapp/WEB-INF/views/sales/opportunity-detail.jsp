<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <title>WorkBridge | 영업기회 상세</title>
    <%@ include file="../fragments/head.jspf" %>
</head>
<body data-context-path="${pageContext.request.contextPath}"
      data-opportunity-id="${opportunityId}"
      data-can-write-opportunity="${canWriteOpportunity}"
      data-can-read-customer="${canReadCustomer}"
      data-can-read-activity="${canReadActivity}"
      data-can-write-activity="${canWriteActivity}">
<div class="app-shell">
    <%@ include file="../fragments/header.jspf" %>
    <%@ include file="../fragments/sidebar.jspf" %>

    <main class="content">
        <section class="page-heading">
            <div><p class="eyebrow">OPPORTUNITY · DETAIL</p><h1 id="opportunity-detail-title">영업기회 상세</h1><p id="opportunity-detail-subtitle">영업단계와 예상매출, 고객 활동 이력을 확인합니다.</p></div>
            <a class="button button-secondary-light" href="<c:url value='/web/opportunities'/>">영업기회 목록</a>
        </section>

        <section class="metric-grid compact-metric-grid">
            <article class="metric-card"><span class="metric-label">Opportunity No.</span><strong id="opportunity-summary-no" class="metric-code">-</strong><p id="opportunity-summary-status">상태 확인 중</p></article>
            <article class="metric-card"><span class="metric-label">Stage</span><strong id="opportunity-summary-stage" class="metric-text">-</strong><p id="opportunity-summary-probability">수주확률</p></article>
            <article class="metric-card"><span class="metric-label">Expected revenue</span><strong id="opportunity-summary-revenue" class="metric-text">-</strong><p>등록된 예상매출</p></article>
            <article class="metric-card"><span class="metric-label">Expected close</span><strong id="opportunity-summary-close" class="metric-text">-</strong><p id="opportunity-summary-owner">담당자 확인 중</p></article>
        </section>

        <section class="detail-grid opportunity-detail-grid">
            <article class="panel">
                <div class="panel-heading"><div><p class="eyebrow">OPPORTUNITY MASTER</p><h2>기본정보</h2></div><span id="opportunity-version-badge" class="version-badge">v-</span></div>
                <form id="opportunity-update-form" class="detail-form">
                    <div class="form-grid two-fields">
                        <label>기회번호<input id="detail-opportunity-no" class="field-control" readonly></label>
                        <label>고객<input id="detail-opportunity-customer" class="field-control" readonly></label>
                    </div>
                    <label>영업기회명<input id="detail-opportunity-name" class="field-control" maxlength="200" required></label>
                    <div class="form-grid three-fields">
                        <label>단계<select id="detail-opportunity-stage" class="field-control"><option value="LEAD">리드</option><option value="QUALIFIED">유효 기회</option><option value="PROPOSAL">제안</option><option value="NEGOTIATION">협상</option><option value="WON">수주</option><option value="LOST">실주</option></select></label>
                        <label>수주확률(%)<input id="detail-opportunity-probability" class="field-control" type="number" min="0" max="100" required></label>
                        <label>종료 예정일<input id="detail-opportunity-close-date" class="field-control" type="date" required></label>
                    </div>
                    <p id="detail-opportunity-probability-hint" class="field-hint"></p>
                    <div class="form-grid two-fields">
                        <label>예상매출<input id="detail-opportunity-revenue" class="field-control" type="number" min="0" step="0.01" required></label>
                        <label>영업 담당자<select id="detail-opportunity-owner" class="field-control" required></select></label>
                    </div>
                    <label>설명<textarea id="detail-opportunity-description" class="field-control textarea-control" maxlength="2000" rows="5"></textarea></label>
                    <div class="form-actions"><button class="button button-primary" type="submit">변경사항 저장</button><button id="opportunity-detail-reload" class="button button-secondary-light" type="button">다시 불러오기</button></div>
                </form>
                <p id="opportunity-update-message" class="form-message"></p>
            </article>

            <article class="panel governance-panel">
                <div class="panel-heading"><div><p class="eyebrow">BUSINESS RULE</p><h2>상태 관리 기준</h2></div><span class="chip chip-neutral">Domain rule</span></div>
                <ol class="flow-list compact-flow">
                    <li><span>1</span><div><strong>상태 자동 산정</strong><p>LEAD~NEGOTIATION은 OPEN, WON·LOST는 CLOSED로 서버가 계산합니다.</p></div></li>
                    <li><span>2</span><div><strong>종결 단계 보호</strong><p>WON 또는 LOST로 종결된 기회는 다른 단계로 되돌릴 수 없습니다.</p></div></li>
                    <li><span>3</span><div><strong>확률 정합성</strong><p>WON은 100%, LOST는 0%, 진행 단계는 0~99%만 허용합니다.</p></div></li>
                    <li><span>4</span><div><strong>담당자 검증</strong><p>활성 SALES 역할 직원만 소유자로 지정할 수 있습니다.</p></div></li>
                </ol>
                <dl class="detail-list detail-meta-list">
                    <div><dt>고객 사업자번호</dt><dd id="opportunity-customer-business-no">-</dd></div>
                    <div><dt>생성일시</dt><dd id="opportunity-created-at">-</dd></div>
                    <div><dt>수정일시</dt><dd id="opportunity-updated-at">-</dd></div>
                </dl>
                <a id="opportunity-customer-link" class="button button-secondary-light full-width-button" href="#">고객 상세로 이동</a>
            </article>
        </section>

        <section id="opportunity-activity-section" class="panel section-panel">
            <div class="panel-heading"><div><p class="eyebrow">ACTIVITY TIMELINE</p><h2>연결 영업활동</h2></div><span id="opportunity-activity-count" class="result-count">조회 중</span></div>
            <div class="split-panel-grid activity-split-grid">
                <div><div id="opportunity-activity-list" class="activity-timeline"><p class="table-empty">영업활동을 불러오는 중입니다.</p></div><div id="opportunity-activity-pagination" class="pagination"></div></div>
                <form id="opportunity-activity-create-form" class="inline-editor-form">
                    <h3>영업활동 등록</h3>
                    <div class="form-grid two-fields">
                        <label>활동 유형<select id="opportunity-activity-type" class="field-control"><option value="VISIT">방문</option><option value="CALL">통화</option><option value="MEETING">회의</option><option value="EMAIL">이메일</option><option value="OTHER">기타</option></select></label>
                        <label>활동 일시<input id="opportunity-activity-at" class="field-control" type="datetime-local" required></label>
                    </div>
                    <label>담당 영업사원<select id="opportunity-activity-employee" class="field-control" required></select></label>
                    <label>제목<input id="opportunity-activity-subject" class="field-control" maxlength="200" required></label>
                    <label>장소<input id="opportunity-activity-location" class="field-control" maxlength="300"></label>
                    <label>내용<textarea id="opportunity-activity-content" class="field-control textarea-control" maxlength="4000" rows="4"></textarea></label>
                    <button class="button button-primary" type="submit">활동 저장</button>
                    <p id="opportunity-activity-message" class="form-message"></p>
                </form>
            </div>
        </section>

        <%@ include file="../fragments/footer.jspf" %>
    </main>
</div>
<div id="toast-region" class="toast-region" aria-live="polite"></div>
<script src="<c:url value='/js/common.js'/>"></script>
<script src="<c:url value='/js/sales-common.js'/>"></script>
<script src="<c:url value='/js/opportunity-detail.js'/>"></script>
</body>
</html>

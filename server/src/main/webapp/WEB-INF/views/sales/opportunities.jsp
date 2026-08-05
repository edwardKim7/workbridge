<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <title>WorkBridge | 영업기회</title>
    <%@ include file="../fragments/head.jspf" %>
</head>
<body data-context-path="${pageContext.request.contextPath}"
      data-can-write-opportunity="${canWriteOpportunity}">
<div class="app-shell">
    <%@ include file="../fragments/header.jspf" %>
    <%@ include file="../fragments/sidebar.jspf" %>

    <main class="content">
        <section class="page-heading">
            <div>
                <p class="eyebrow">SALES SUPPORT · OPPORTUNITY PIPELINE</p>
                <h1>영업기회</h1>
                <p>고객·단계·상태·담당자·종료 예정일 조건으로 파이프라인을 조회하고 예상매출과 수주확률을 관리합니다.</p>
            </div>
            <a class="button button-secondary-light" href="<c:url value='/web/customers'/>">고객 관리</a>
        </section>

        <section class="filter-panel" aria-label="영업기회 검색 조건">
            <form id="opportunity-search-form" class="filter-grid opportunity-filter-grid">
                <label>통합 검색<input id="search-opportunity-keyword" class="field-control" placeholder="기회번호·기회명·고객명"></label>
                <label>단계<select id="search-opportunity-stage" class="field-control"><option value="">전체</option><option value="LEAD">리드</option><option value="QUALIFIED">유효 기회</option><option value="PROPOSAL">제안</option><option value="NEGOTIATION">협상</option><option value="WON">수주</option><option value="LOST">실주</option></select></label>
                <label>상태<select id="search-opportunity-status" class="field-control"><option value="">전체</option><option value="OPEN">진행 중</option><option value="CLOSED">종결</option></select></label>
                <label>영업 담당자<select id="search-opportunity-owner" class="field-control"><option value="">전체</option></select></label>
                <label>종료 예정 시작<input id="search-close-from" class="field-control" type="date"></label>
                <label>종료 예정 종료<input id="search-close-to" class="field-control" type="date"></label>
                <button class="button button-primary" type="submit">검색</button>
            </form>
        </section>

        <section class="workspace-grid opportunity-workspace-grid">
            <article class="panel table-panel">
                <div class="panel-heading"><div><p class="eyebrow">PIPELINE LIST</p><h2>검색 결과</h2></div><span id="opportunity-result-count" class="result-count">조회 중</span></div>
                <div class="data-table-wrap">
                    <table class="data-table opportunity-table">
                        <thead><tr><th>기회번호·명</th><th>고객</th><th>단계</th><th>예상매출</th><th>확률</th><th>종료 예정</th><th>담당자</th></tr></thead>
                        <tbody id="opportunity-table-body"><tr><td colspan="7" class="table-empty">영업기회를 불러오는 중입니다.</td></tr></tbody>
                    </table>
                </div>
                <div id="opportunity-pagination" class="pagination"></div>
            </article>

            <aside class="panel editor-panel">
                <div class="panel-heading"><div><p class="eyebrow">NEW OPPORTUNITY</p><h2>영업기회 등록</h2></div><span class="chip">상태 자동 산정</span></div>
                <form id="opportunity-create-form">
                    <label>고객 <span class="required-mark">*</span><select id="create-opportunity-customer" class="field-control" required></select></label>
                    <label>영업기회명 <span class="required-mark">*</span><input id="create-opportunity-name" class="field-control" maxlength="200" required></label>
                    <div class="form-grid two-fields">
                        <label>단계<select id="create-opportunity-stage" class="field-control"><option value="LEAD">리드</option><option value="QUALIFIED">유효 기회</option><option value="PROPOSAL">제안</option><option value="NEGOTIATION">협상</option><option value="WON">수주</option><option value="LOST">실주</option></select></label>
                        <label>수주확률(%)<input id="create-opportunity-probability" class="field-control" type="number" min="0" max="100" value="10" required></label>
                    </div>
                    <p id="create-opportunity-probability-hint" class="field-hint">진행 단계는 0~99% 범위입니다.</p>
                    <div class="form-grid two-fields">
                        <label>예상매출<input id="create-opportunity-revenue" class="field-control" type="number" min="0" step="0.01" required></label>
                        <label>종료 예정일<input id="create-opportunity-close-date" class="field-control" type="date" required></label>
                    </div>
                    <label>영업 담당자 <span class="required-mark">*</span><select id="create-opportunity-owner" class="field-control" required></select></label>
                    <label>설명<textarea id="create-opportunity-description" class="field-control textarea-control" maxlength="2000" rows="4"></textarea></label>
                    <div class="form-actions"><button class="button button-primary" type="submit">등록</button><button id="opportunity-create-reset" class="button button-secondary-light" type="button">초기화</button></div>
                </form>
                <p id="opportunity-create-message" class="form-message"></p>
                <div class="policy-note"><strong>단계·확률 규칙</strong><p>수주는 100%, 실주는 0%로 저장하며 두 단계는 종결 상태입니다. 종결 후 다른 단계로 되돌리는 요청은 차단합니다.</p></div>
            </aside>
        </section>

        <%@ include file="../fragments/footer.jspf" %>
    </main>
</div>
<div id="toast-region" class="toast-region" aria-live="polite"></div>
<script src="<c:url value='/js/common.js'/>"></script>
<script src="<c:url value='/js/sales-common.js'/>"></script>
<script src="<c:url value='/js/opportunities.js'/>"></script>
</body>
</html>

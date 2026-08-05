<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <title>WorkBridge | 고객 상세</title>
    <%@ include file="../fragments/head.jspf" %>
</head>
<body data-context-path="${pageContext.request.contextPath}"
      data-customer-id="${customerId}"
      data-can-write-customer="${canWriteCustomer}"
      data-can-read-opportunity="${canReadOpportunity}"
      data-can-write-opportunity="${canWriteOpportunity}"
      data-can-read-activity="${canReadActivity}"
      data-can-write-activity="${canWriteActivity}">
<div class="app-shell">
    <%@ include file="../fragments/header.jspf" %>
    <%@ include file="../fragments/sidebar.jspf" %>

    <main class="content">
        <section class="page-heading">
            <div>
                <p class="eyebrow">CUSTOMER 360 · DETAIL</p>
                <h1 id="customer-detail-title">고객 상세 조회</h1>
                <p id="customer-detail-subtitle">고객 기본정보와 영업기회, 활동 이력을 한 화면에서 확인합니다.</p>
            </div>
            <a class="button button-secondary-light" href="<c:url value='/web/customers'/>">고객 목록</a>
        </section>

        <section class="metric-grid compact-metric-grid" aria-label="고객 요약">
            <article class="metric-card"><span class="metric-label">Business No.</span><strong id="customer-summary-business-no" class="metric-code">-</strong><p>중복 등록 방지 유니크 키</p></article>
            <article class="metric-card"><span class="metric-label">Customer status</span><strong id="customer-summary-status" class="metric-text">-</strong><p id="customer-summary-active">사용 여부 확인 중</p></article>
            <article class="metric-card"><span class="metric-label">Sales owner</span><strong id="customer-summary-owner" class="metric-text">-</strong><p id="customer-summary-owner-no">담당 사번</p></article>
            <article class="metric-card"><span class="metric-label">Open opportunities</span><strong id="customer-summary-open-opportunities">-</strong><p id="customer-summary-pipeline">파이프라인 계산 중</p></article>
        </section>

        <section class="detail-grid customer-detail-grid">
            <article class="panel">
                <div class="panel-heading">
                    <div><p class="eyebrow">CUSTOMER MASTER</p><h2>기본정보</h2></div>
                    <span id="customer-version-badge" class="version-badge">v-</span>
                </div>
                <form id="customer-update-form" class="detail-form">
                    <div class="form-grid two-fields">
                        <label>사업자번호
                            <input id="detail-business-no" class="field-control" maxlength="20" required>
                        </label>
                        <label>고객명
                            <input id="detail-customer-name" class="field-control" maxlength="200" required>
                        </label>
                    </div>
                    <div class="form-grid two-fields">
                        <label>대표자명
                            <input id="detail-representative-name" class="field-control" maxlength="100">
                        </label>
                        <label>고객 담당자
                            <input id="detail-contact-name" class="field-control" maxlength="100">
                        </label>
                    </div>
                    <div class="form-grid two-fields">
                        <label>연락처
                            <input id="detail-contact-phone" class="field-control" maxlength="30">
                        </label>
                        <label>이메일
                            <input id="detail-contact-email" class="field-control" type="email" maxlength="200">
                        </label>
                    </div>
                    <div class="form-grid three-fields">
                        <label>설립일
                            <input id="detail-established-date" class="field-control" type="date">
                        </label>
                        <label>영업 담당자
                            <select id="detail-assigned-sales" class="field-control" required></select>
                        </label>
                        <label>고객 상태
                            <select id="detail-customer-status" class="field-control">
                                <option value="PROSPECT">잠재 고객</option>
                                <option value="ACTIVE">거래 중</option>
                                <option value="INACTIVE">비활성</option>
                            </select>
                        </label>
                    </div>
                    <label class="checkbox-line"><input id="detail-customer-active" type="checkbox"> 사용 상태</label>
                    <div class="form-actions">
                        <button class="button button-primary" type="submit">변경사항 저장</button>
                        <button id="customer-detail-reload" class="button button-secondary-light" type="button">다시 불러오기</button>
                    </div>
                </form>
                <p id="customer-update-message" class="form-message"></p>
            </article>

            <article class="panel governance-panel">
                <div class="panel-heading"><div><p class="eyebrow">DATA GOVERNANCE</p><h2>정합성 기준</h2></div><span class="chip chip-neutral">Server enforced</span></div>
                <ol class="flow-list compact-flow">
                    <li><span>1</span><div><strong>사업자번호 정규화</strong><p>하이픈을 제거한 숫자 10자리로 저장하고 UNIQUE KEY로 중복을 차단합니다.</p></div></li>
                    <li><span>2</span><div><strong>담당자 소유권</strong><p>SALES는 자신의 고객만 수정하며 ADMIN만 담당자를 변경할 수 있습니다.</p></div></li>
                    <li><span>3</span><div><strong>낙관적 잠금</strong><p>수정 시 version을 비교해 오래된 화면의 덮어쓰기를 409로 차단합니다.</p></div></li>
                    <li><span>4</span><div><strong>감사 로그</strong><p>변경 전·후 핵심값을 저장하되 연락처와 사업자번호는 마스킹합니다.</p></div></li>
                </ol>
                <dl class="detail-list detail-meta-list">
                    <div><dt>생성일시</dt><dd id="customer-created-at">-</dd></div>
                    <div><dt>수정일시</dt><dd id="customer-updated-at">-</dd></div>
                </dl>
            </article>
        </section>

        <section id="customer-opportunity-section" class="panel section-panel">
            <div class="panel-heading">
                <div><p class="eyebrow">SALES PIPELINE</p><h2>영업기회</h2><p class="panel-description">고객에 연결된 영업기회를 페이징으로 조회합니다.</p></div>
                <span id="customer-opportunity-count" class="result-count">조회 중</span>
            </div>
            <div class="split-panel-grid">
                <div>
                    <div class="data-table-wrap">
                        <table class="data-table opportunity-table compact-table">
                            <thead><tr><th>기회번호·명</th><th>단계</th><th>예상매출</th><th>종료 예정</th><th>담당자</th></tr></thead>
                            <tbody id="customer-opportunity-body"><tr><td colspan="5" class="table-empty">영업기회를 불러오는 중입니다.</td></tr></tbody>
                        </table>
                    </div>
                    <div id="customer-opportunity-pagination" class="pagination"></div>
                </div>
                <form id="customer-opportunity-create-form" class="inline-editor-form">
                    <h3>영업기회 등록</h3>
                    <label>영업기회명<input id="customer-opportunity-name" class="field-control" maxlength="200" required></label>
                    <div class="form-grid two-fields">
                        <label>단계
                            <select id="customer-opportunity-stage" class="field-control">
                                <option value="LEAD">리드</option><option value="QUALIFIED">유효 기회</option><option value="PROPOSAL">제안</option><option value="NEGOTIATION">협상</option><option value="WON">수주</option><option value="LOST">실주</option>
                            </select>
                        </label>
                        <label>수주확률(%)<input id="customer-opportunity-probability" class="field-control" type="number" min="0" max="100" value="10" required></label>
                    </div>
                    <p id="customer-opportunity-probability-hint" class="field-hint">진행 단계는 0~99% 범위입니다.</p>
                    <div class="form-grid two-fields">
                        <label>예상매출<input id="customer-opportunity-revenue" class="field-control" type="number" min="0" step="0.01" required></label>
                        <label>종료 예정일<input id="customer-opportunity-close-date" class="field-control" type="date" required></label>
                    </div>
                    <label>영업 담당자<select id="customer-opportunity-owner" class="field-control" required></select></label>
                    <label>설명<textarea id="customer-opportunity-description" class="field-control textarea-control" maxlength="2000" rows="3"></textarea></label>
                    <button class="button button-primary" type="submit">영업기회 등록</button>
                    <p id="customer-opportunity-message" class="form-message"></p>
                </form>
            </div>
        </section>

        <section id="customer-activity-section" class="panel section-panel">
            <div class="panel-heading">
                <div><p class="eyebrow">ACTIVITY TIMELINE</p><h2>영업활동</h2><p class="panel-description">방문·통화·회의·이메일 이력을 최근순으로 확인합니다.</p></div>
                <span id="customer-activity-count" class="result-count">조회 중</span>
            </div>
            <div class="split-panel-grid activity-split-grid">
                <div>
                    <div id="customer-activity-list" class="activity-timeline"><p class="table-empty">영업활동을 불러오는 중입니다.</p></div>
                    <div id="customer-activity-pagination" class="pagination"></div>
                </div>
                <form id="customer-activity-create-form" class="inline-editor-form">
                    <h3>영업활동 등록</h3>
                    <div class="form-grid two-fields">
                        <label>활동 유형
                            <select id="customer-activity-type" class="field-control"><option value="VISIT">방문</option><option value="CALL">통화</option><option value="MEETING">회의</option><option value="EMAIL">이메일</option><option value="OTHER">기타</option></select>
                        </label>
                        <label>활동 일시<input id="customer-activity-at" class="field-control" type="datetime-local" required></label>
                    </div>
                    <label>연결 영업기회<select id="customer-activity-opportunity" class="field-control"><option value="">고객 공통 활동</option></select></label>
                    <label>담당 영업사원<select id="customer-activity-employee" class="field-control" required></select></label>
                    <label>제목<input id="customer-activity-subject" class="field-control" maxlength="200" required></label>
                    <label>장소<input id="customer-activity-location" class="field-control" maxlength="300"></label>
                    <label>내용<textarea id="customer-activity-content" class="field-control textarea-control" maxlength="4000" rows="4"></textarea></label>
                    <button class="button button-primary" type="submit">활동 저장</button>
                    <p id="customer-activity-message" class="form-message"></p>
                </form>
            </div>
        </section>

        <%@ include file="../fragments/footer.jspf" %>
    </main>
</div>
<div id="toast-region" class="toast-region" aria-live="polite"></div>
<script src="<c:url value='/js/common.js'/>"></script>
<script src="<c:url value='/js/sales-common.js'/>"></script>
<script src="<c:url value='/js/customer-detail.js'/>"></script>
</body>
</html>

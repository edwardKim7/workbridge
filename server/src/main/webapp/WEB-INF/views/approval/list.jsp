<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <title>WorkBridge | 전자결재</title>
    <%@ include file="../fragments/head.jspf" %>
</head>
<body data-context-path="${pageContext.request.contextPath}">
<div class="app-shell">
    <%@ include file="../fragments/header.jspf" %>
    <%@ include file="../fragments/sidebar.jspf" %>

    <main class="content">
        <section class="page-heading">
            <div>
                <p class="eyebrow">APPROVAL WORKFLOW</p>
                <h1>전자결재</h1>
                <p>초안·요청·승인·반려 상태와 처리 이력을 한 흐름으로 관리합니다.</p>
            </div>
            <span class="chip">조건부 UPDATE 중복 처리 차단</span>
        </section>

        <section class="filter-panel">
            <form id="approval-search-form" class="filter-grid approval-filter-grid">
                <label>
                    조회 범위
                    <select id="approval-view" class="field-control">
                        <option value="MY_REQUESTS">내 요청</option>
                        <option value="MY_APPROVALS">내 결재</option>
                        <option value="ALL">전체</option>
                    </select>
                </label>
                <label>
                    상태
                    <select id="approval-status" class="field-control">
                        <option value="">전체</option>
                        <option>DRAFT</option>
                        <option>REQUESTED</option>
                        <option>APPROVED</option>
                        <option>REJECTED</option>
                        <option>CANCELED</option>
                    </select>
                </label>
                <label>
                    유형
                    <select id="approval-type" class="field-control">
                        <option value="">전체</option>
                        <option value="SALES_EXPENSE">영업비용</option>
                        <option value="VISIT_PLAN">방문 계획</option>
                        <option value="DISCOUNT_EXCEPTION">할인율 예외</option>
                    </select>
                </label>
                <label>
                    검색
                    <input id="approval-keyword"
                           class="field-control"
                           placeholder="결재번호·제목·요청자">
                </label>
                <button class="button button-primary">검색</button>
            </form>
        </section>

        <section class="workspace-grid approval-workspace">
            <article class="panel table-panel">
                <div class="panel-heading">
                    <div>
                        <p class="eyebrow">REQUEST LIST</p>
                        <h2>결재 목록</h2>
                    </div>
                    <span id="approval-count" class="result-count">조회 중</span>
                </div>
                <div class="data-table-wrap">
                    <table class="data-table">
                        <thead>
                        <tr>
                            <th>결재번호·제목</th>
                            <th>유형</th>
                            <th>요청자</th>
                            <th>승인자</th>
                            <th>상태</th>
                            <th>요청일</th>
                        </tr>
                        </thead>
                        <tbody id="approval-body"></tbody>
                    </table>
                </div>
                <div id="approval-pagination" class="pagination"></div>
            </article>

            <aside id="approval-create-panel" class="panel editor-panel">
                <div class="panel-heading">
                    <div>
                        <p class="eyebrow">NEW DRAFT</p>
                        <h2>결재 초안 등록</h2>
                    </div>
                </div>

                <form id="approval-create-form">
                    <label>
                        영업기회
                        <select id="approval-business-id" class="field-control" required></select>
                    </label>
                    <label>
                        결재 유형
                        <select id="approval-create-type" class="field-control">
                            <option value="SALES_EXPENSE">영업비용 결재</option>
                            <option value="VISIT_PLAN">고객 방문 계획</option>
                            <option value="DISCOUNT_EXCEPTION">할인율 예외</option>
                        </select>
                    </label>
                    <label>
                        제목
                        <input id="approval-title" class="field-control" maxlength="200" required>
                    </label>
                    <label>
                        승인자
                        <select id="approval-approver" class="field-control" required></select>
                    </label>

                    <div class="form-grid two-fields">
                        <label>
                            금액
                            <input id="approval-amount" type="number" min="0" class="field-control">
                        </label>
                        <label>
                            비용 분류
                            <input id="approval-expense-category"
                                   class="field-control"
                                   maxlength="100">
                        </label>
                    </div>

                    <div class="form-grid two-fields">
                        <label>
                            방문 예정일
                            <input id="approval-visit-date" type="date" class="field-control">
                        </label>
                        <label>
                            예외 할인율
                            <input id="approval-discount-rate"
                                   type="number"
                                   min="0"
                                   max="100"
                                   step="0.01"
                                   class="field-control">
                        </label>
                    </div>

                    <label>
                        설명
                        <textarea id="approval-description"
                                  class="field-control textarea-control"
                                  rows="4"
                                  maxlength="2000"></textarea>
                    </label>
                    <button class="button button-primary" type="submit">초안 등록</button>
                </form>
                <p id="approval-form-message" class="form-message"></p>
            </aside>
        </section>

        <%@ include file="../fragments/footer.jspf" %>
    </main>
</div>

<div id="toast-region" class="toast-region"></div>
<script src="<c:url value='/js/common.js'/>"></script>
<script src="<c:url value='/js/approvals.js'/>"></script>
</body>
</html>

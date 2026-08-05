<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <title>WorkBridge | 고객 관리</title>
    <%@ include file="../fragments/head.jspf" %>
</head>
<body data-context-path="${pageContext.request.contextPath}"
      data-can-write-customer="${canWriteCustomer}">
<div class="app-shell">
    <%@ include file="../fragments/header.jspf" %>
    <%@ include file="../fragments/sidebar.jspf" %>

    <main class="content">
        <section class="page-heading">
            <div>
                <p class="eyebrow">SALES SUPPORT · CUSTOMER MASTER</p>
                <h1>고객 관리</h1>
                <p>고객명·사업자번호·담당자 조건으로 검색하고, 담당 영업사원과 고객 상태를 함께 관리합니다.</p>
            </div>
            <a class="button button-secondary-light" href="<c:url value='/web/opportunities'/>">영업기회 보기</a>
        </section>

        <section class="filter-panel" aria-label="고객 검색 조건">
            <form id="customer-search-form" class="filter-grid customer-filter-grid">
                <label>고객명
                    <input id="search-customer-name" class="field-control" maxlength="200" placeholder="고객명 일부">
                </label>
                <label>사업자번호
                    <input id="search-business-no" class="field-control" maxlength="20" placeholder="000-00-00000">
                </label>
                <label>고객 담당자
                    <input id="search-contact-name" class="field-control" maxlength="100" placeholder="담당자명">
                </label>
                <label>영업 담당자
                    <select id="search-assigned-sales" class="field-control"><option value="">전체</option></select>
                </label>
                <label>고객 상태
                    <select id="search-customer-status" class="field-control">
                        <option value="">전체</option>
                        <option value="PROSPECT">잠재 고객</option>
                        <option value="ACTIVE">거래 중</option>
                        <option value="INACTIVE">비활성</option>
                    </select>
                </label>
                <label>사용 여부
                    <select id="search-active" class="field-control">
                        <option value="">전체</option>
                        <option value="true">사용</option>
                        <option value="false">미사용</option>
                    </select>
                </label>
                <button class="button button-primary" type="submit">검색</button>
            </form>
        </section>

        <section class="workspace-grid sales-workspace-grid">
            <article class="panel table-panel">
                <div class="panel-heading">
                    <div><p class="eyebrow">CUSTOMER LIST</p><h2>검색 결과</h2></div>
                    <span id="customer-result-count" class="result-count">조회 중</span>
                </div>
                <div class="data-table-wrap">
                    <table class="data-table customer-table">
                        <thead>
                        <tr>
                            <th>고객</th>
                            <th>사업자번호</th>
                            <th>고객 담당자</th>
                            <th>영업 담당자</th>
                            <th>상태</th>
                            <th>최종 수정</th>
                        </tr>
                        </thead>
                        <tbody id="customer-table-body">
                        <tr><td colspan="6" class="table-empty">고객 정보를 불러오는 중입니다.</td></tr>
                        </tbody>
                    </table>
                </div>
                <div id="customer-pagination" class="pagination" aria-label="고객 목록 페이지"></div>
            </article>

            <aside class="panel editor-panel" id="customer-create-panel">
                <div class="panel-heading">
                    <div><p class="eyebrow">NEW CUSTOMER</p><h2>고객 등록</h2></div>
                    <span class="chip">담당자 검증</span>
                </div>
                <form id="customer-create-form">
                    <label>사업자번호 <span class="required-mark">*</span>
                        <input id="create-business-no" class="field-control" maxlength="20" required placeholder="000-00-00000">
                    </label>
                    <label>고객명 <span class="required-mark">*</span>
                        <input id="create-customer-name" class="field-control" maxlength="200" required>
                    </label>
                    <div class="form-grid two-fields">
                        <label>대표자명
                            <input id="create-representative-name" class="field-control" maxlength="100">
                        </label>
                        <label>고객 담당자
                            <input id="create-contact-name" class="field-control" maxlength="100">
                        </label>
                    </div>
                    <div class="form-grid two-fields">
                        <label>연락처
                            <input id="create-contact-phone" class="field-control" maxlength="30">
                        </label>
                        <label>설립일
                            <input id="create-established-date" class="field-control" type="date">
                        </label>
                    </div>
                    <label>이메일
                        <input id="create-contact-email" class="field-control" type="email" maxlength="200">
                    </label>
                    <div class="form-grid two-fields">
                        <label>영업 담당자 <span class="required-mark">*</span>
                            <select id="create-assigned-sales" class="field-control" required></select>
                        </label>
                        <label>고객 상태
                            <select id="create-customer-status" class="field-control">
                                <option value="PROSPECT">잠재 고객</option>
                                <option value="ACTIVE">거래 중</option>
                                <option value="INACTIVE">비활성</option>
                            </select>
                        </label>
                    </div>
                    <label class="checkbox-line"><input id="create-customer-active" type="checkbox" checked> 사용 상태로 등록</label>
                    <div class="form-actions">
                        <button class="button button-primary" type="submit">등록</button>
                        <button id="customer-create-reset" class="button button-secondary-light" type="button">초기화</button>
                    </div>
                </form>
                <p id="customer-create-message" class="form-message"></p>
                <div class="policy-note">
                    <strong>업무 소유권 규칙</strong>
                    <p>영업 담당자는 자신에게만 고객을 배정할 수 있고, 관리자는 활성 SALES 역할 직원에게 재배정할 수 있습니다.</p>
                </div>
            </aside>
        </section>

        <%@ include file="../fragments/footer.jspf" %>
    </main>
</div>
<div id="toast-region" class="toast-region" aria-live="polite"></div>
<script src="<c:url value='/js/common.js'/>"></script>
<script src="<c:url value='/js/sales-common.js'/>"></script>
<script src="<c:url value='/js/customers.js'/>"></script>
</body>
</html>

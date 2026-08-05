<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <title>WorkBridge | 데이터 이관</title>
    <%@ include file="../fragments/head.jspf" %>
</head>
<body data-context-path="${pageContext.request.contextPath}">
<div class="app-shell">
    <%@ include file="../fragments/header.jspf" %>
    <%@ include file="../fragments/sidebar.jspf" %>

    <main class="content">
        <section class="page-heading">
            <div>
                <p class="eyebrow">CUSTOMER DATA MIGRATION</p>
                <h1>CSV 데이터 이관</h1>
                <p>Staging Table에서 행 단위 검증 후 정상 데이터만 하나의 반영 트랜잭션으로 처리합니다.</p>
            </div>
            <div class="form-actions">
                <a class="button button-secondary-light"
                   href="<c:url value='/sample-data/customers-valid.csv'/>">정상 CSV</a>
                <a class="button button-secondary-light"
                   href="<c:url value='/sample-data/customers-invalid.csv'/>">오류 CSV</a>
            </div>
        </section>

        <section class="panel upload-panel">
            <form id="migration-form" class="upload-form">
                <label>
                    고객 CSV
                    <input id="migration-file" type="file" accept=".csv,text/csv" required>
                </label>
                <button id="migration-submit" class="button button-primary">업로드·검증·반영</button>
            </form>
            <p class="field-hint">UTF-8, 최대 5MB, 제공된 8개 헤더 순서를 사용합니다.</p>
        </section>

        <section class="panel table-panel section-panel">
            <div class="panel-heading">
                <h2>배치 이력</h2>
                <span id="migration-count" class="result-count"></span>
            </div>
            <div class="data-table-wrap">
                <table class="data-table">
                    <thead>
                    <tr>
                        <th>배치번호</th>
                        <th>파일</th>
                        <th>상태</th>
                        <th>전체</th>
                        <th>성공</th>
                        <th>실패</th>
                        <th>실행자</th>
                        <th>결과</th>
                    </tr>
                    </thead>
                    <tbody id="migration-body"></tbody>
                </table>
            </div>
            <div id="migration-pagination" class="pagination"></div>
        </section>

        <section id="migration-errors-panel" class="panel section-panel hidden">
            <div class="panel-heading">
                <h2>행별 오류</h2>
                <a id="migration-error-download" class="button button-secondary-light">오류 CSV</a>
            </div>
            <div class="data-table-wrap">
                <table class="data-table">
                    <thead>
                    <tr>
                        <th>행</th>
                        <th>필드</th>
                        <th>코드</th>
                        <th>사유</th>
                        <th>원본값</th>
                    </tr>
                    </thead>
                    <tbody id="migration-errors"></tbody>
                </table>
            </div>
        </section>

        <%@ include file="../fragments/footer.jspf" %>
    </main>
</div>

<div id="toast-region" class="toast-region"></div>
<script src="<c:url value='/js/common.js'/>"></script>
<script src="<c:url value='/js/migrations.js'/>"></script>
</body>
</html>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <title>WorkBridge | 결재 상세</title>
    <%@ include file="../fragments/head.jspf" %>
</head>
<body data-context-path="${pageContext.request.contextPath}" data-approval-id="${approvalId}">
<div class="app-shell">
    <%@ include file="../fragments/header.jspf" %>
    <%@ include file="../fragments/sidebar.jspf" %>

    <main class="content">
        <section class="page-heading">
            <div>
                <p class="eyebrow">APPROVAL DETAIL</p>
                <h1 id="approval-detail-title">결재 상세</h1>
                <p id="approval-detail-subtitle">처리 상태와 이력을 조회합니다.</p>
            </div>
            <a class="button button-secondary-light" href="<c:url value='/web/approvals'/>">목록</a>
        </section>

        <section class="detail-grid">
            <article class="panel">
                <div class="panel-heading">
                    <div>
                        <p class="eyebrow">REQUEST DATA</p>
                        <h2>결재 정보</h2>
                    </div>
                    <span id="approval-detail-status" class="status-badge">-</span>
                </div>

                <dl id="approval-detail-list" class="detail-list"></dl>
                <div id="approval-actions" class="form-actions"></div>
                <div id="approval-opinion-wrap" class="inline-editor-form">
                    <label>
                        처리 의견
                        <textarea id="approval-opinion"
                                  class="field-control"
                                  maxlength="1000"
                                  rows="4"></textarea>
                    </label>
                </div>
            </article>

            <aside class="panel">
                <div class="panel-heading">
                    <div>
                        <p class="eyebrow">PROCESS HISTORY</p>
                        <h2>처리 이력</h2>
                    </div>
                </div>
                <div id="approval-history" class="activity-timeline"></div>
            </aside>
        </section>

        <%@ include file="../fragments/footer.jspf" %>
    </main>
</div>

<div id="toast-region" class="toast-region"></div>
<script src="<c:url value='/js/common.js'/>"></script>
<script src="<c:url value='/js/approval-detail.js'/>"></script>
</body>
</html>

'use strict';

document.addEventListener('DOMContentLoaded', async () => {
    try {
        const [dashboardPayload, mePayload, salesPayload] = await Promise.all([
            WorkBridge.request('/api/v1/dashboard'),
            WorkBridge.request('/api/v1/auth/me'),
            WorkBridge.request('/api/v1/sales/dashboard')
        ]);
        renderOrganizationSummary(dashboardPayload.data);
        renderCurrentRoles(mePayload.data.roles || []);
        renderSalesSummary(salesPayload.data);
    } catch (error) {
        WorkBridge.showToast(WorkBridge.errorMessage(error), 'danger', error.traceId);
    }
});

function renderOrganizationSummary(summary) {
    document.getElementById('active-employees').textContent = summary.activeEmployees;
    document.getElementById('employee-total').textContent = `전체 ${summary.totalEmployees}명`;
    document.getElementById('active-departments').textContent = summary.activeDepartments;
    document.getElementById('department-total').textContent = `전체 ${summary.totalDepartments}개`;
    document.getElementById('locked-employees').textContent = summary.lockedEmployees;
    document.getElementById('active-roles').textContent = summary.activeRoles;
}

function renderCurrentRoles(roles) {
    const roleList = document.getElementById('current-role-list');
    roleList.innerHTML = roles
        .map(role => `<span class="role-chip">${WorkBridge.escapeHtml(role.roleName)} <small>${WorkBridge.escapeHtml(role.roleCode)}</small></span>`)
        .join('');
}

function renderSalesSummary(summary) {
    document.getElementById('sales-dashboard-scope').textContent = summary.scope === 'MY' ? '내 담당 범위' : '전체 범위';
    document.getElementById('sales-total-customers').textContent = Number(summary.totalCustomers || 0).toLocaleString('ko-KR');
    document.getElementById('sales-customer-detail').textContent = `거래 중 ${summary.activeCustomers} · 잠재 ${summary.prospectCustomers}`;
    document.getElementById('sales-open-opportunities').textContent = Number(summary.openOpportunities || 0).toLocaleString('ko-KR');
    document.getElementById('sales-close-detail').textContent = `이번 달 종료 예정 ${summary.closingThisMonth} · 수주 ${summary.wonOpportunities} · 실주 ${summary.lostOpportunities}`;
    document.getElementById('sales-open-pipeline').textContent = WorkBridge.formatCurrency(summary.openPipelineAmount);
    document.getElementById('sales-weighted-pipeline').textContent = WorkBridge.formatCurrency(summary.weightedPipelineAmount);

    const container = document.getElementById('sales-stage-counts');
    container.innerHTML = (summary.stageCounts || []).map(item => `
        <article class="stage-count-card">
            <span>${WorkBridge.escapeHtml(WorkBridgeSales.opportunityStageLabel(item.stage))}</span>
            <strong>${Number(item.count || 0).toLocaleString('ko-KR')}</strong>
        </article>
    `).join('');
}

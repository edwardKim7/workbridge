'use strict';

const opportunityState = {
    page: 0,
    size: 15,
    canWrite: document.body.dataset.canWriteOpportunity === 'true',
    customers: [],
    salesEmployees: [],
    currentUser: null,
    writableCustomers: []
};

document.addEventListener('DOMContentLoaded', async () => {
    bindOpportunityEvents();
    configureOpportunityWorkspace();
    try {
        const [customerPayload, employeePayload, mePayload] = await Promise.all([
            WorkBridge.request('/api/v1/customers/options'),
            WorkBridge.request('/api/v1/sales/employees/options'),
            WorkBridge.request('/api/v1/auth/me')
        ]);
        opportunityState.customers = customerPayload.data || [];
        opportunityState.salesEmployees = employeePayload.data || [];
        opportunityState.currentUser = mePayload.data;
        renderOpportunityReferenceOptions();
        resetOpportunityCreateForm();
        configureOpportunityCreateAvailability();
        applyOpportunityQueryParameters();
        await loadOpportunities();
    } catch (error) {
        WorkBridge.showToast(WorkBridge.errorMessage(error), 'danger', error.traceId);
    }
});

function bindOpportunityEvents() {
    document.getElementById('opportunity-search-form')?.addEventListener('submit', event => {
        event.preventDefault();
        opportunityState.page = 0;
        loadOpportunities();
    });
    document.getElementById('opportunity-create-form')?.addEventListener('submit', createOpportunity);
    document.getElementById('opportunity-create-reset')?.addEventListener('click', resetOpportunityCreateForm);
    document.getElementById('create-opportunity-stage')?.addEventListener('change', updateCreateStageRule);
}

function configureOpportunityWorkspace() {
    if (opportunityState.canWrite) {
        return;
    }
    document.querySelector('.opportunity-workspace-grid .editor-panel')?.classList.add('hidden');
    document.querySelector('.opportunity-workspace-grid')?.classList.add('read-only-workspace');
}

function renderOpportunityReferenceOptions() {
    opportunityState.writableCustomers = isSalesOnly()
        ? opportunityState.customers.filter(customer => String(customer.assignedSalesId) === String(opportunityState.currentUser.employeeId))
        : opportunityState.customers;
    WorkBridgeSales.populateSelect(document.getElementById('create-opportunity-customer'), opportunityState.writableCustomers, {
        placeholder: opportunityState.writableCustomers.length ? '고객을 선택하세요.' : '등록 가능한 고객이 없습니다.',
        valueKey: 'customerId',
        label: customer => `${customer.customerName} · ${customer.formattedBusinessRegistrationNo || customer.businessRegistrationNo}`
    });
    WorkBridgeSales.populateSelect(document.getElementById('search-opportunity-owner'), opportunityState.salesEmployees, {
        placeholder: '전체',
        label: employeeLabel
    });
    WorkBridgeSales.populateSelect(document.getElementById('create-opportunity-owner'), opportunityState.salesEmployees, {
        placeholder: '담당자를 선택하세요.',
        label: employeeLabel
    });

    if (isSalesOnly()) {
        const ownerSelect = document.getElementById('create-opportunity-owner');
        ownerSelect.value = String(opportunityState.currentUser.employeeId);
        ownerSelect.disabled = true;
    }
}

function configureOpportunityCreateAvailability() {
    if (!opportunityState.canWrite || opportunityState.writableCustomers.length) {
        return;
    }
    WorkBridgeSales.setFormEnabled(document.getElementById('opportunity-create-form'), false);
    WorkBridge.setFormMessage(
        document.getElementById('opportunity-create-message'),
        '현재 계정에 배정된 활성 고객이 없어 영업기회를 등록할 수 없습니다.'
    );
}

async function loadOpportunities() {
    const from = document.getElementById('search-close-from').value;
    const to = document.getElementById('search-close-to').value;
    if (from && to && from > to) {
        WorkBridge.showToast('종료 예정 시작일은 종료일보다 늦을 수 없습니다.', 'danger');
        return;
    }

    const query = WorkBridge.buildQuery({
        keyword: document.getElementById('search-opportunity-keyword').value.trim(),
        stage: document.getElementById('search-opportunity-stage').value,
        status: document.getElementById('search-opportunity-status').value,
        ownerEmployeeId: document.getElementById('search-opportunity-owner').value,
        closeDateFrom: from,
        closeDateTo: to,
        page: opportunityState.page,
        size: opportunityState.size
    });
    const body = document.getElementById('opportunity-table-body');
    body.innerHTML = '<tr><td colspan="7" class="table-empty">영업기회를 조회하는 중입니다.</td></tr>';

    try {
        const payload = await WorkBridge.request(`/api/v1/opportunities${query}`);
        renderOpportunityRows(payload.data.items || []);
        document.getElementById('opportunity-result-count').textContent = `${Number(payload.data.totalElements || 0).toLocaleString('ko-KR')}건`;
        WorkBridge.renderPagination(
            document.getElementById('opportunity-pagination'),
            payload.data.page,
            payload.data.totalPages,
            page => {
                opportunityState.page = page;
                loadOpportunities();
            }
        );
    } catch (error) {
        body.innerHTML = `<tr><td colspan="7" class="table-empty">${WorkBridge.escapeHtml(WorkBridge.errorMessage(error))}</td></tr>`;
        WorkBridge.showToast(WorkBridge.errorMessage(error), 'danger', error.traceId);
    }
}

function renderOpportunityRows(items) {
    const body = document.getElementById('opportunity-table-body');
    if (!items.length) {
        body.innerHTML = '<tr><td colspan="7" class="table-empty">조회된 영업기회가 없습니다.</td></tr>';
        return;
    }
    body.innerHTML = items.map(item => `
        <tr class="clickable-row" data-opportunity-id="${item.opportunityId}" tabindex="0">
            <td>
                <strong class="table-primary">${WorkBridge.escapeHtml(item.opportunityName)}</strong>
                <small class="table-secondary">${WorkBridge.escapeHtml(item.opportunityNo)} · ${WorkBridgeSales.opportunityStatusLabel(item.status)}</small>
            </td>
            <td>
                <strong class="table-primary">${WorkBridge.escapeHtml(item.customerName)}</strong>
                <small class="table-secondary">${WorkBridge.escapeHtml(item.formattedBusinessRegistrationNo || item.businessRegistrationNo)}</small>
            </td>
            <td><span class="status-badge ${WorkBridgeSales.statusBadgeClass(item.stage)}">${WorkBridge.escapeHtml(WorkBridgeSales.opportunityStageLabel(item.stage))}</span></td>
            <td>${WorkBridge.formatCurrency(item.expectedRevenue)}</td>
            <td><strong>${Number(item.probability)}%</strong></td>
            <td>${WorkBridge.formatDate(item.expectedCloseDate)}</td>
            <td>
                <strong class="table-primary">${WorkBridge.escapeHtml(item.ownerEmployeeName || '-')}</strong>
                <small class="table-secondary">${WorkBridge.escapeHtml(item.ownerEmployeeNo || '-')}</small>
            </td>
        </tr>
    `).join('');

    body.querySelectorAll('[data-opportunity-id]').forEach(row => {
        const move = () => {
            window.location.href = `${WorkBridge.contextPath}/web/opportunities/${row.dataset.opportunityId}`;
        };
        row.addEventListener('click', move);
        row.addEventListener('keydown', event => {
            if (event.key === 'Enter' || event.key === ' ') {
                event.preventDefault();
                move();
            }
        });
    });
}

async function createOpportunity(event) {
    event.preventDefault();
    if (!opportunityState.canWrite) {
        return;
    }
    const form = event.currentTarget;
    if (!form.reportValidity()) {
        return;
    }
    const message = document.getElementById('opportunity-create-message');
    const submitButton = form.querySelector('button[type="submit"]');
    setBusy(submitButton, true, '등록');
    WorkBridge.setFormMessage(message, '영업기회를 저장하는 중입니다.');

    const requestBody = {
        customerId: numberOrNull(document.getElementById('create-opportunity-customer').value),
        opportunityName: document.getElementById('create-opportunity-name').value.trim(),
        stage: document.getElementById('create-opportunity-stage').value,
        expectedRevenue: Number(document.getElementById('create-opportunity-revenue').value),
        expectedCloseDate: document.getElementById('create-opportunity-close-date').value || null,
        probability: Number(document.getElementById('create-opportunity-probability').value),
        ownerEmployeeId: numberOrNull(document.getElementById('create-opportunity-owner').value),
        description: emptyToNull('create-opportunity-description')
    };

    try {
        const payload = await WorkBridge.request('/api/v1/opportunities', {
            method: 'POST',
            body: requestBody
        });
        WorkBridge.setFormMessage(message, payload.message, 'success');
        WorkBridge.showToast(payload.message, 'success', payload.traceId);
        window.location.href = `${WorkBridge.contextPath}/web/opportunities/${payload.data.opportunityId}`;
    } catch (error) {
        WorkBridge.setFormMessage(message, WorkBridge.errorMessage(error), 'danger');
        WorkBridge.showToast(WorkBridge.errorMessage(error), 'danger', error.traceId);
    } finally {
        setBusy(submitButton, false, '등록');
    }
}

function resetOpportunityCreateForm() {
    const form = document.getElementById('opportunity-create-form');
    if (!form) {
        return;
    }
    const selectedCustomer = document.getElementById('create-opportunity-customer').value;
    form.reset();
    document.getElementById('create-opportunity-stage').value = 'LEAD';
    document.getElementById('create-opportunity-probability').value = '10';
    document.getElementById('create-opportunity-close-date').value = dateAfterDays(30);
    if (selectedCustomer && opportunityState.customers.some(item => String(item.customerId) === selectedCustomer)) {
        document.getElementById('create-opportunity-customer').value = selectedCustomer;
    }
    if (isSalesOnly()) {
        document.getElementById('create-opportunity-owner').value = String(opportunityState.currentUser.employeeId);
    }
    updateCreateStageRule();
    WorkBridge.setFormMessage(document.getElementById('opportunity-create-message'), '');
}

function updateCreateStageRule() {
    const stage = document.getElementById('create-opportunity-stage').value;
    const probability = document.getElementById('create-opportunity-probability');
    const hint = document.getElementById('create-opportunity-probability-hint');
    applyStageProbability(stage, probability, hint);
}

function applyStageProbability(stage, probabilityInput, hintElement) {
    probabilityInput.disabled = false;
    if (stage === 'WON') {
        probabilityInput.value = '100';
        probabilityInput.disabled = true;
    } else if (stage === 'LOST') {
        probabilityInput.value = '0';
        probabilityInput.disabled = true;
    } else {
        const current = Number(probabilityInput.value);
        if (!Number.isFinite(current) || current < 0 || current > 99) {
            probabilityInput.value = stage === 'NEGOTIATION' ? '70' : stage === 'PROPOSAL' ? '50' : stage === 'QUALIFIED' ? '30' : '10';
        }
    }
    hintElement.textContent = WorkBridgeSales.opportunityProbabilityHint(stage);
}

function applyOpportunityQueryParameters() {
    const query = new URLSearchParams(window.location.search);
    const customerId = query.get('customerId');
    const createSelect = document.getElementById('create-opportunity-customer');
    const selectableCustomer = customerId
        && Array.from(createSelect.options).some(option => option.value === customerId);
    if (selectableCustomer) {
        createSelect.value = customerId;
    }
    if (query.get('create') === 'true'
        && opportunityState.canWrite
        && (!customerId || selectableCustomer)) {
        document.getElementById('create-opportunity-name').focus();
    }
}

function dateAfterDays(days) {
    const date = new Date();
    date.setDate(date.getDate() + days);
    const local = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
    return local.toISOString().slice(0, 10);
}

function employeeLabel(employee) {
    const department = employee.departmentName ? ` · ${employee.departmentName}` : '';
    return `${employee.employeeName} · ${employee.employeeNo}${department}`;
}

function isSalesOnly() {
    return WorkBridgeSales.hasRole(opportunityState.currentUser, 'SALES')
        && !WorkBridgeSales.hasRole(opportunityState.currentUser, 'ADMIN');
}

function emptyToNull(elementId) {
    const value = document.getElementById(elementId).value.trim();
    return value || null;
}

function numberOrNull(value) {
    return value === '' || value === null || value === undefined ? null : Number(value);
}

function setBusy(button, busy, normalLabel) {
    if (!button) {
        return;
    }
    button.disabled = busy;
    button.textContent = busy ? '처리 중…' : normalLabel;
}
